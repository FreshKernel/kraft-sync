// To fix: 'The Kotlin Gradle plugin was loaded multiple times in different subprojects,
// which is not supported and may break the build.'
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.shadow.jar) apply false
}
