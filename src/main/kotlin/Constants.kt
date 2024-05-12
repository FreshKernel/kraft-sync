import java.io.File

object Constants {
    /**
     * The file name for the script icon that placed in the resources
     * */
    const val SCRIPT_ICON_FILE_NAME = "apple.png"

    /**
     * Should we by default use GUI for the (error messages, download loading indicator etc...) as default value?
     * */
    const val USE_GUI_DEFAULT = true

    /**
     * The argument name that will be used in the launch args in order to not use the gui
     * will use `nogui` because it's the same as
     * [minecraft java server](https://minecraft.fandom.com/wiki/Tutorials/Setting_up_a_server)
     * */
    const val DISABLE_GUI_ARG_NAME = "nogui"

    /**
     * Should we tell the launcher to launch the game anyway when error during the sync process?
     * read [ScriptConfig.launchOnError] for more info
     * */
    const val LAUNCH_ON_ERROR = false

    /**
     * Files of the minecraft instance (e.g.,`.minecraft`) where it has the mods, resource-packs and shaders etc...
     * */
    sealed class MinecraftInstanceFiles(val file: File) {
        data object Mods : MinecraftInstanceFiles(File("mods"))

        data object ResourcePacks : MinecraftInstanceFiles(File("resourcepacks"))

        data object ShaderPacks : MinecraftInstanceFiles(File("shaderpacks"))

        data object Config : MinecraftInstanceFiles(File("config"))

        /**
         * A txt file contains the minecraft options/settings or data like the key bindings, sound settings
         * enabled resource-packs, language and the minecraft video settings and more
         * */
        data object Options : MinecraftInstanceFiles(File("options.txt"))

        // TODO: Also might add support for `servers.essential.dat` in the future.

        /**
         * A file that contains all the information about the list of servers
         * */
        data object ServersDat : MinecraftInstanceFiles(File("servers.dat"))

        /**
         * This folder will be created and managed by the script, it's specific to it
         * */
        data object MinecraftSyncData : MinecraftInstanceFiles(File("minecraft-sync-data")) {
            /**
             * The script need a config file that contains required data and other optional to configure it
             * */
            data object ScriptConfig : MinecraftInstanceFiles(File(MinecraftSyncData.file, "config.json"))
        }
    }
}
