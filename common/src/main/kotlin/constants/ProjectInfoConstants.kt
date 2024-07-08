package constants

object ProjectInfoConstants {
    const val REPOSITORY_URL = "https://github.com/FreshKernel/kraft-sync"
    const val DISPLAY_NAME = "Kraft Sync"
    const val NORMALIZED_NAME = "kraft-sync"

    // At the moment, we don't have a website; this will make it easier to provide a link to it later
    const val WEBSITE_URL = REPOSITORY_URL

    const val LIBS_VERSIONS_TOML_FILE_URL =
        "https://raw.githubusercontent.com/FreshKernel/kraft-sync/main/gradle/libs.versions.toml"

    // TODO: This will always download the latest pre-release, will need to update the URL later
    const val LATEST_SYNC_SCRIPT_JAR_FILE_URL = "https://github.com/FreshKernel/kraft-sync/releases/download/latest/kraft-sync.jar"
}
