package constants

object DotMinecraftFileNames {
    const val MODS_DIRECTORY = "mods"
    const val RESOURCE_PACKS_DIRECTORY = "resourcepacks"
    const val SHADER_PACKS_DIRECTORY = "shaderpacks"
    const val CONFIG_DIRECTORY = "config"
    const val SAVES_DIRECTORY = "saves"

    /**
     * A directory that is specific to the sync script where it stores its data.
     * */
    const val SYNC_SCRIPT_DIRECTORY = "${ProjectInfoConstants.NORMALIZED_NAME}-data"

    /**
     * A txt file contains the minecraft options/settings or data like the key bindings, sound settings
     * enabled resource-packs, language and the minecraft video settings, and more
     * */
    const val OPTIONS_FILE = "options.txt"

    /**
     * A file that contains all the information about the list of servers
     * */
    const val SERVERS_FILE = "servers.dat"

    // 'servers.essential.dat' is not supported yet
}
