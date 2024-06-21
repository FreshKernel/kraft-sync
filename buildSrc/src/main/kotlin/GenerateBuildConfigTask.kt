import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

/**
 * A task to extract data from the build script into the source code to use it, such as the version of the project.
 * */
open class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    val fieldsToGenerate: MapProperty<String, Any> = project.objects.mapProperty()

    @get:Input
    val classFullyQualifiedName: Property<String> = project.objects.property<String>()

    @get:OutputDirectory
    val generatedOutputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun execute() {
        val directory = generatedOutputDirectory.get().asFile
        directory.deleteRecursively()
        directory.mkdirs()

        val packageNameParts = classFullyQualifiedName.get().split(".")
        val className = packageNameParts.last()
        val generatedFile = directory.resolve("$className.kt")
        val generatedFileContent =
            buildString {
                if (packageNameParts.size > 1) {
                    appendLine("package ${packageNameParts.dropLast(1).joinToString(".")}")
                }

                appendLine()
                appendLine("// GENERATED FILE, Manual changes will be overwritten")
                appendLine("object $className {")
                for ((key, value) in fieldsToGenerate.get().entries.sortedBy { it.key }) {
                    appendLine("    const val $key = ${if (value is String) "\"$value\"" else value.toString()}")
                }
                appendLine("}")
            }
        generatedFile.writeText(generatedFileContent)
    }
}
