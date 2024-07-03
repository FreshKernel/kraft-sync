import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property
import proguard.gradle.ProGuardTask
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.io.path.exists
import kotlin.math.abs as kotlinMathAbs

open class BuildMinimizedJarTask : DefaultTask() {
    @get:InputFile
    val inputJarFile: RegularFileProperty = project.objects.fileProperty()

    @get:OutputFile
    val outputJarFile: RegularFileProperty = project.objects.fileProperty()

    @get:InputFile
    val proguardConfigFile: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    val obfuscate: Property<Boolean> = project.objects.property<Boolean>()

    @get:InputFiles
    val compileClasspath: ConfigurableFileCollection = project.objects.fileCollection()

    @TaskAction
    fun execute() {
        val buildProguardDirectory =
            project.layout.buildDirectory
                .dir("proguard")
                .get()
                .asFile

        if (!buildProguardDirectory.exists()) {
            buildProguardDirectory.mkdir()
        }

        val generatedProguardConfigurationFiles = mutableListOf<File>()
        JarFile(inputJarFile.get().asFile).use { jarFile ->
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

                generatedProguardConfigurationFiles.add(generatedProguardFile)
            }
        }

        val proguardTask = project.tasks.create<ProGuardTask>("proguard")
        proguardTask.apply {
            injars(inputJarFile)
            outjars(outputJarFile)

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
            injars(compileClasspath)

            printmapping(
                outputJarFile
                    .get()
                    .asFile.parentFile
                    .resolve("${outputJarFile.get().asFile.nameWithoutExtension}.map"),
            )

            if (!obfuscate.get()) {
                // Disabling obfuscation makes the JAR file size a bit larger, and the debugging process a bit less easy
                dontobfuscate()
            }

            // Kotlinx serialization breaks when using Proguard optimizations
            dontoptimize()
            printconfiguration(buildProguardDirectory.resolve("proguard-configuration.pro"))

            configuration(proguardConfigFile)

            generatedProguardConfigurationFiles.forEach { configuration(it) }
        }

        // Execute the Proguard task

        // A workaround for executing ProGuard without getting the notes by disabling the logging
        // when the `-i` or `--info` is not set

        if (project.gradle.startParameter.logLevel != LogLevel.INFO) {
            suppressOutputAndExecute {
                proguardTask.actions.forEach { it.execute(proguardTask) }
            }
        } else {
            proguardTask.actions.forEach { it.execute(proguardTask) }
        }

        logResultMessage()
    }

    private fun logResultMessage() {
        val original = inputJarFile.get().asFile
        val minimized = outputJarFile.get().asFile
        val minimizedFileSizeInMegabytes = String.format("%.2f", minimized.length().toDouble() / (1024L * 1024L))

        val percentageDifference =
            ((minimized.length() - original.length()).toDouble() / original.length()) * 100
        val formattedPercentageDifference = String.format("%.2f%%", kotlinMathAbs(percentageDifference))

        logger.lifecycle(
            "📦 The size of the Proguard minimized JAR file (${minimized.name}) is $minimizedFileSizeInMegabytes MB." +
                " The size has been reduced \uD83D\uDCC9 by $formattedPercentageDifference. Location: ${minimized.path}",
        )
    }

    private fun suppressOutputAndExecute(action: () -> Unit) {
        val oldStandardOut = System.out
        val oldStandardErr = System.err
        val noOpOutputStream =
            object : OutputStream() {
                override fun write(b: Int) {
                    // Do nothing
                }
            }

        try {
            System.setOut(PrintStream(noOpOutputStream))
            System.setErr(PrintStream(noOpOutputStream))

            action()
        } finally {
            System.setOut(oldStandardOut)
            System.setErr(oldStandardErr)
        }
    }
}
