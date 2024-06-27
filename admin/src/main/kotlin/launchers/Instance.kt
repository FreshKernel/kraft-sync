package launchers

import java.nio.file.Path

/**
 * An abstraction of Instance/Profile for all Minecraft launchers that
 * will be only used internally.
 *
 * Does not contain the data such as mods as some launchers store them in a different place.
 *
 * This will be used for listing the instances of a launcher to provide
 * them as dropdown options for the text input field that request the instance directory path.
 * */
data class Instance(
    val launcherInstanceDirectoryPath: Path,
    val instanceName: String,
)
