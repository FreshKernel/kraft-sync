import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty

class ProguardT : DefaultTask() {
    val mainJar: RegularFileProperty = project.objects.fileProperty()
}
