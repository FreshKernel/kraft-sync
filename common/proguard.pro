# This file contain the rules that's either specific to the project or custom rules that's not from the dependencies

# Proguard Kotlin Example https://github.com/Guardsquare/proguard/blob/master/examples/application-kotlin/proguard.pro

-keepattributes *Annotation*

# -keep class kotlin.Metadata { *; }

# Entry point to the app.
-keep class MainKt { *; }

# The following rules are already included in Proguard Gradle task configurations:
# https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
# https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro
# https://square.github.io/okhttp/features/r8_proguard/

# It's not recommended to use -ignorewarnings, see https://github.com/square/okio/issues/1298#issuecomment-1652182091 for more details
# If the warning has import from the JDK, try to update libraryjars to include the required module

# Ignore all the warnings from Kotlinx Coroutines as they will show after ProGuard 7.5.0 including rules
-dontwarn kotlinx.coroutines.**

# FlatLaf

# Exclude the entire FlatLaf dependency from minimization to fix 'no ComponentUI class for: javax.swing.<component>'
# as FlatLaf and Swing use reflections and dynamic class loading
-keep class com.formdev.flatlaf.** { *; }
-dontwarn com.formdev.flatlaf.**
-dontnote com.formdev.flatlaf.**
