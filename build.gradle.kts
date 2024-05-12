import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow.jar)
    // TODO: Restore KTLint later
//    alias(libs.plugins.ktlint)
    application
}

group = "net.freshplatform"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test)
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)
    implementation(libs.kotlinx.serialization.json)
    // TODO: This is only needed for reading mods/.index/mod-example.toml for migration to the script sync info
    // and might be separated in the future
    implementation(libs.tomlj)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

tasks.jar {
    // Disable the normal jar task as we will use the one from Shadow plugin
    enabled = false
}

tasks.withType(ShadowJar::class) {
    // If you change this, also change it from GitHub workflows and in the scripts folder
    archiveFileName.set("minecraft-sync.jar")
    description =
        "A script written in Kotlin/JVM that allow you sync the mods, resource-packs, shaders and more " +
        "seamlessly before launching the game"
    minimize()
}

application {
    mainClass = "MainKt"
}

tasks.getByName("build").dependsOn(tasks.shadowJar)

// TODO: I might remove shaky jar

tasks.register("shakyJar") {
    enabled = false
    val jarFileName = project.tasks.shadowJar.get().archiveFileName.get()
    val jarFileNameWithoutExtension = File(jarFileName).nameWithoutExtension
    doLast {
        exec {
            val command =
                "java -jar ./scripts/shakyboi.jar --app ./build/libs/$jarFileName --root ${application.mainClass.get()}" +
                    " --output ./build/libs/$jarFileNameWithoutExtension-shaky.jar"
            commandLine(command.split("\\s".toRegex()))
        }
    }
}

tasks.getByName("build").dependsOn("shakyJar")
