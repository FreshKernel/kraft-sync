plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow.jar)
    alias(libs.plugins.ktlint)
}

group = "net.freshplatform"
version = libs.versions.project.get()

dependencies {
    implementation(projects.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.flatlaf.core)
    implementation(libs.flatlaf.extras)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )
}

application {
    mainClass = "MainKt"
    applicationDefaultJvmArgs =
        listOf(
            // Not needed on JDK 18 and above as UTF-8 as the default charset
            // (https://docs.oracle.com/en/java/javase/21/migrate/preparing-migration.html#GUID-6FB24439-342C-496E-9D99-5F752528C7B1)
            "-Dfile.encoding=UTF8",
        )
}

val distDirectory
    get() = project.rootDir.resolve("dist")

tasks.clean {
    doFirst {
        delete(distDirectory)
    }
}

// Shadow JAR for building the uber JAR file

tasks.shadowJar {
    // If you change the file name or destination directory, also update it from the README.md and other markdown files
    archiveFileName.set("${rootProject.name}.jar")
    description =
        "A script that allows to sync mods, resource packs, shaders, and more seamlessly before launching the game."
    minimize {
        // Exclude the entire FlatLaf dependency from minimization to fix `no ComponentUI class for: javax.swing.<component>`
        // Due to reflections, more details: https://github.com/JFormDesigner/FlatLaf/issues/648#issuecomment-1441547550
        exclude(dependency("${libs.flatlaf.core.get().module}:.*"))
    }

    doLast {
        val uberJarFile = archiveFile.get().asFile
        val uberJarFileSizeInMegabytes = String.format("%.2f", uberJarFile.length().toDouble() / (1024L * 1024L))

        logger.lifecycle(
            "📦 The size of the shadow JAR file (${uberJarFile.name}) is $uberJarFileSizeInMegabytes MB. Location: ${uberJarFile.path}",
        )
    }
}

// Proguard for minimizing the JAR file

val minimizedJar =
    tasks.register<BuildMinimizedJarTask>("minimizedJar") {
        dependsOn(tasks.shadowJar)

        val uberJarFile = tasks.shadowJar.flatMap { it.archiveFile }

        inputJarFile = uberJarFile
        outputJarFile =
            distDirectory
                .resolve("${uberJarFile.get().asFile.nameWithoutExtension}.jar")

        proguardConfigFile = project(projects.common.identityPath.path).file("proguard.pro")
        obfuscate = false
        compileClasspath = sourceSets.main.get().compileClasspath
    }

// Configure assemble task

tasks.assemble {
    // The `assemble` task already depends on `shadowJar`
    dependsOn(tasks.shadowJar, minimizedJar)
}

// Run tasks

val devWorkingDirectory = file("devWorkingDirectory")

val createTestDirectory =
    tasks.register("createTestDirectory") {
        doLast {
            if (devWorkingDirectory.exists()) return@doLast
            devWorkingDirectory.mkdirs()
        }
    }

private fun <T : Task?> registerExecuteJavaJarTask(
    taskName: String,
    buildJarFileTaskProvider: TaskProvider<T>,
    getJarFile: () -> RegularFile,
    additionalArgs: List<String> = emptyList(),
    overrideHeadless: Boolean? = null,
) {
    tasks.register<JavaExec>(taskName) {
        dependsOn(createTestDirectory, buildJarFileTaskProvider)
        classpath = files(getJarFile())
        workingDir = devWorkingDirectory
        args = additionalArgs
        group = tasks.run.get().group
        if (overrideHeadless != null) {
            systemProperty("java.awt.headless", overrideHeadless)
        }
    }
}

fun registerRunTasks() {
    val getUberJarFile = {
        tasks.shadowJar
            .get()
            .archiveFile
            .get()
    }
    registerExecuteJavaJarTask(
        taskName = "runJar",
        buildJarFileTaskProvider = tasks.shadowJar,
        getJarFile = getUberJarFile,
    )
    registerExecuteJavaJarTask(
        taskName = "runJarCli",
        buildJarFileTaskProvider = tasks.shadowJar,
        getJarFile = getUberJarFile,
        additionalArgs = listOf("nogui"),
    )

    val getMinimizedJarFile = {
        minimizedJar.get().outputJarFile.get()
    }
    registerExecuteJavaJarTask(
        taskName = "runMinimizedJar",
        buildJarFileTaskProvider = minimizedJar,
        getJarFile = getMinimizedJarFile,
    )
    registerExecuteJavaJarTask(
        taskName = "runMinimizedJarCli",
        buildJarFileTaskProvider = minimizedJar,
        getJarFile = getMinimizedJarFile,
        additionalArgs = listOf("nogui"),
    )

    // A task that will help simulate as if we were running the
    // application in a system that doesn't support mouse and keyboard.
    // Will be helpful to test the application on the current development machine instead of a server or virtual machine
    registerExecuteJavaJarTask(
        "runHeadlessJar",
        tasks.shadowJar,
        getUberJarFile,
        overrideHeadless = true,
    )
}

registerRunTasks()

// Configure runShadow

tasks.runShadow {
    workingDir = devWorkingDirectory
}
