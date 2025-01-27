plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

group = "org.freshkernel"
version = libs.versions.project.get()

dependencies {
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
