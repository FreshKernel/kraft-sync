plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow.jar)
    alias(libs.plugins.ktlint)
}

group = "org.freshkernel"
version = libs.versions.project.get()

dependencies {
    implementation(projects.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.flatlaf.core)
    implementation(libs.flatlaf.extras)
    implementation(libs.ktoml.core)

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
}

val distDirectory
    get() = project.rootDir.resolve("dist")

tasks.shadowJar {
    val scriptJarFileNameWithoutExtension = rootProject.name
    // If you change the file name or destination directory, also update it from the README.md and other markdown files
    archiveFileName.set("$scriptJarFileNameWithoutExtension-admin.jar")
    description = "A admin utility program for $scriptJarFileNameWithoutExtension script."
    minimize {
        // Exclude the entire FlatLaf dependency from minimization to fix `no ComponentUI class for: javax.swing.<component>`
        // Due to reflections, more details: https://github.com/JFormDesigner/FlatLaf/issues/648#issuecomment-1441547550
        exclude(dependency("${libs.flatlaf.core.get().module}:.*"))
    }

    doLast {
        val jarFile = archiveFile.get().asFile
        val jarFileSizeInMegabytes = String.format("%.2f", jarFile.length().toDouble() / (1024L * 1024L))

        logger.lifecycle("📦 The size of the shadow JAR file (${jarFile.name}) is $jarFileSizeInMegabytes MB. Location: ${jarFile.path}")
    }
}

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
        additionalJdkModules =
            listOf(
                // For Java Clipboard
                "java.datatransfer",
            )
    }

tasks.assemble {
    // The `assemble` task already depends on `shadowJar`
    dependsOn(tasks.shadowJar, minimizedJar)
}
