package constants

object MinecraftInstanceNames {
    const val MODS_FOLDER = "mods"
    const val RESOURCE_PACKS_FOLDER = "resourcepacks"
    const val SHADER_PACKS_FOLDER = "shaderpacks"
    const val CONFIG_FOLDER = "config"
    const val SAVES_FOLDER = "saves"

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
