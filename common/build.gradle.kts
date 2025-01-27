plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.freshkernel"
version = libs.versions.project.get()

dependencies {
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

val generateBuildConfig =
    tasks.register<GenerateBuildConfigTask>("generateBuildConfig") {
        // To allow overriding the current project version
        val projectVersion: String? by project
        val developmentMode: String? by project

        val buildConfigDirectory = project.layout.buildDirectory.dir("generated")

        classFullyQualifiedName.set("generated.BuildConfig")
        generatedOutputDirectory.set(buildConfigDirectory)
        fieldsToGenerate.put("PROJECT_VERSION", projectVersion ?: libs.versions.project.get())
        fieldsToGenerate.put("DEVELOPMENT_MODE", developmentMode?.toBooleanStrictOrNull() ?: false)
    }

sourceSets.main.configure {
    kotlin.srcDirs(generateBuildConfig.flatMap { it.generatedOutputDirectory })
}

tasks.compileKotlin.configure {
    dependsOn(generateBuildConfig)
}
