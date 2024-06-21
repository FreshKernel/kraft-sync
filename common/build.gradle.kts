plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)
}

group = "net.freshplatform"
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
        val buildConfigDirectory = project.layout.buildDirectory.dir("generated")

        classFullyQualifiedName.set("generated.BuildConfig")
        generatedOutputDirectory.set(buildConfigDirectory)
        fieldsToGenerate.put("PROJECT_VERSION", libs.versions.project.get())
    }

sourceSets.main.configure {
    kotlin.srcDirs(generateBuildConfig.flatMap { it.generatedOutputDirectory })
}

tasks.compileKotlin.configure {
    dependsOn(generateBuildConfig)
}
