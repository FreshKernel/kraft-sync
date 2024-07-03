plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.proguard) {
        // On older versions of proguard, Android build tools will be included
        exclude("com.android.tools.build")
    }
}
