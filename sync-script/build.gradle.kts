import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.io.path.exists
import kotlin.math.abs as kotlinMathAbs

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

// Shadow JAR for building the fat JAR file

tasks.shadowJar {
    // If you change the file name or destination directory, also update it from the README.md and other markdown files
    archiveFileName.set("${rootProject.name}.jar")
    destinationDirectory = distDirectory
    description =
        "A script that allows to sync mods, resource packs, shaders, and more seamlessly before launching the game."
    minimize {
        // Exclude the entire FlatLaf dependency from minimization to fix `no ComponentUI class for: javax.swing.<component>`
        // Due to reflections, more details: https://github.com/JFormDesigner/FlatLaf/issues/648#issuecomment-1441547550
        exclude(dependency("${libs.flatlaf.core.get().module}:.*"))
    }

    doLast {
        val fatJarFile = archiveFile.get().asFile
        val fatJarFileSizeInMegabytes = String.format("%.2f", fatJarFile.length().toDouble() / (1024L * 1024L))

        logger.lifecycle(
            "📦 The size of the shadow JAR file (${fatJarFile.name}) is $fatJarFileSizeInMegabytes MB. Location: ${fatJarFile.path}",
        )
    }
}

// Proguard for minimizing the JAR file

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath(
            libs.plugins.proguard
                .get()
                .toString(),
        ) {
            // On older versions of proguard, Android build tools will be included
            exclude("com.android.tools.build")
        }
    }
}

fun getMinimizedJarFile(
    fatJarFileNameWithoutExtension: String,
    fatJarFileDestinationDirectory: DirectoryProperty,
): Provider<RegularFile> = fatJarFileDestinationDirectory.file("$fatJarFileNameWithoutExtension.min.jar")

val minimizedJar =
    tasks.register<proguard.gradle.ProGuardTask>("minimizedJar") {
        dependsOn(tasks.shadowJar)

        val fatJarFile = tasks.shadowJar.flatMap { it.archiveFile }
        val fatJarFileDestinationDirectory = tasks.shadowJar.get().destinationDirectory

        val minimizedJarFile =
            getMinimizedJarFile(
                fatJarFileNameWithoutExtension = fatJarFile.get().asFile.nameWithoutExtension,
                fatJarFileDestinationDirectory = fatJarFileDestinationDirectory,
            )

        injars(fatJarFile)
        outjars(minimizedJarFile)

        val javaHome = System.getProperty("java.home")
        if (System.getProperty("java.version").startsWith("1.")) {
            // Before Java 9, runtime classes are packaged in a single JAR file.
            libraryjars(Paths.get(javaHome, "lib", "rt.jar").toString())
        } else {

            // Starting from Java 9, runtime classes are packaged in modular JMOD files.
            fun includeJavaModuleFromJdk(jModFileNameWithoutExtension: String) {
                val jModFilePath = Paths.get(javaHome, "jmods", "$jModFileNameWithoutExtension.jmod")
                if (!jModFilePath.exists()) {
                    throw NoSuchFileException("The '$jModFileNameWithoutExtension' at '$jModFilePath' doesn't exist.")
                }
                libraryjars(
                    mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"),
                    jModFilePath,
                )
            }

            val javaModules =
                listOf(
                    "java.base",
                    // Needed to support Java Swing/Desktop
                    "java.desktop",
                    // Needed to support Java system preferences
                    "java.prefs",
                    // Needed to support Java logging utils (needed by Okio)
                    "java.logging",
                )
            javaModules.forEach { includeJavaModuleFromJdk(jModFileNameWithoutExtension = it) }
        }

        // Includes the main source set's compile classpath for Proguard.
        // Notice that Shadow JAR already includes Kotlin standard library and dependencies, yet this
        // is essential for resolving Kotlin and other library warnings without using '-dontwarn kotlin.**'
        injars(sourceSets.main.get().compileClasspath)

        printmapping(
            fatJarFileDestinationDirectory.get().file("${minimizedJarFile.get().asFile.nameWithoutExtension}.map"),
        )

        // Disabling obfuscation makes the JAR file size a bit larger, and the debugging process a bit less easy
//        dontobfuscate()
        // Kotlinx serialization breaks when using Proguard optimizations
        dontoptimize()

        configuration(file("proguard.pro"))

        doFirst {
            JarFile(fatJarFile.get().asFile).use { jarFile ->
                val generatedRulesFiles =
                    jarFile
                        .entries()
                        .asSequence()
                        .filter { it.name.startsWith("META-INF/proguard") && !it.isDirectory }
                        .map { entry ->
                            jarFile.getInputStream(entry).bufferedReader().use { reader ->
                                Pair(reader.readText(), entry)
                            }
                        }.toList()

                val buildProguardDirectory =
                    layout.buildDirectory
                        .dir("proguard")
                        .get()
                        .asFile
                if (!buildProguardDirectory.exists()) {
                    buildProguardDirectory.mkdir()
                }
                generatedRulesFiles.forEach { (rulesContent, rulesFileEntry) ->
                    val rulesFileNameWithExtension = rulesFileEntry.name.substringAfterLast("/")
                    val generatedProguardFile = File(buildProguardDirectory, "generated-$rulesFileNameWithExtension")
                    if (!generatedProguardFile.exists()) {
                        generatedProguardFile.createNewFile()
                    }
                    generatedProguardFile.bufferedWriter().use { bufferedWriter ->
                        bufferedWriter.appendLine("# Generated file from ($rulesFileEntry) - manual changes will be overwritten")
                        bufferedWriter.appendLine()

                        bufferedWriter.appendLine(rulesContent)
                    }

                    configuration(generatedProguardFile)
                }
            }
        }

        doLast {
            val original = fatJarFile.get().asFile
            val minimized = minimizedJarFile.get().asFile
            val minimizedFileSizeInMegabytes = String.format("%.2f", minimized.length().toDouble() / (1024L * 1024L))

            val percentageDifference =
                ((minimized.length() - original.length()).toDouble() / original.length()) * 100
            val formattedPercentageDifference = String.format("%.2f%%", kotlinMathAbs(percentageDifference))

            logger.lifecycle(
                "📦 The size of the Proguard minimized JAR file (${minimized.name}) is $minimizedFileSizeInMegabytes MB." +
                    " The size has been reduced \uD83D\uDCC9 by $formattedPercentageDifference. Location: ${minimized.path}",
            )
        }
    }

minimizedJar.configure {
    dependsOn(tasks.shadowJar)
}

// Configure assemble task

tasks.assemble {
    // The `assemble` task is already depending on `shadowJar`
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
    jarFile: RegularFile,
    additionalArgs: List<String> = emptyList(),
    overrideHeadless: Boolean? = null,
) {
    tasks.register<JavaExec>(taskName) {
        dependsOn(createTestDirectory, buildJarFileTaskProvider)
        classpath = files(jarFile)
        workingDir = devWorkingDirectory
        args = additionalArgs
        group = tasks.run.get().group
        if (overrideHeadless != null) {
            systemProperty("java.awt.headless", overrideHeadless)
        }
    }
}

fun registerRunTasks() {
    val fatJarFile =
        tasks.shadowJar
            .get()
            .archiveFile
            .get()
    registerExecuteJavaJarTask(
        "runJar",
        tasks.shadowJar,
        fatJarFile,
    )
    registerExecuteJavaJarTask(
        "runJarCli",
        tasks.shadowJar,
        fatJarFile,
        listOf("nogui"),
    )

    val minimizedJarFile =
        getMinimizedJarFile(
            fatJarFileNameWithoutExtension =
                fatJarFile.asFile.nameWithoutExtension,
            fatJarFileDestinationDirectory = tasks.shadowJar.get().destinationDirectory,
        ).get()
    registerExecuteJavaJarTask("runMinimizedJar", minimizedJar, minimizedJarFile)
    registerExecuteJavaJarTask("runMinimizedJarCli", minimizedJar, minimizedJarFile, listOf("nogui"))

    // A task that will help simulate as if we were running the
    // application in a system that doesn't support mouse and keyboard.
    // Will be helpful to test the application on the current development machine instead of a server or virtual machine
    registerExecuteJavaJarTask(
        "runHeadlessJar",
        tasks.shadowJar,
        fatJarFile,
        overrideHeadless = true,
    )
}

registerRunTasks()

// Configure runShadow

tasks.runShadow {
    workingDir = devWorkingDirectory
}
