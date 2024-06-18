package utils

object SystemInfoProvider {
    // Properties

    fun getOperatingSystemName(): String? = System.getProperty(SystemConstants.Properties.OPERATING_SYSTEM_NAME)

    fun getUserHomeDirectoryPath(): String = System.getProperty(SystemConstants.Properties.USER_HOME_DIRECTORY)

    fun getCurrentWorkingDirectoryPath(): String = System.getProperty(SystemConstants.Properties.CURRENT_WORKING_DIRECTORY)

    fun getJavaVersion(): String = System.getProperty(SystemConstants.Properties.JAVA_VERSION)

    fun getJavaHome(): String = System.getProperty(SystemConstants.Properties.JAVA_HOME)

    // Environment variables

    fun getCurrentLinuxDesktopEnvironmentName(): String =
        System.getenv(SystemConstants.EnvironmentVariables.CURRENT_LINUX_DESKTOP_ENVIRONMENT)

    fun getKdeFullSession(): String? = System.getenv(SystemConstants.EnvironmentVariables.KDE_FULL_SESSION)

    /**
     * Some constants might be specific to Java or JVM like the [SystemConstants.Properties]
     * */
    private object SystemConstants {
        object Properties {
            const val OPERATING_SYSTEM_NAME = "os.name"
            const val USER_HOME_DIRECTORY = "user.home"
            const val CURRENT_WORKING_DIRECTORY = "user.dir"
            const val JAVA_HOME = "java.home"
            const val JAVA_VERSION = "java.version"
        }

        object EnvironmentVariables {
            const val CURRENT_LINUX_DESKTOP_ENVIRONMENT = "XDG_CURRENT_DESKTOP"
            const val KDE_FULL_SESSION = "KDE_FULL_SESSION"
        }
    }
}
