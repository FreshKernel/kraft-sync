package utils

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import java.io.IOException
import kotlin.test.Test

class CommandLineTest {
    @Test
    fun `test commandLine when using incorrect command`() {
        assertThrows<IOException> {
            commandLine("java-incorrect", "--version", reasonOfRunningTheCommand = null).getOrThrow()
        }
        assertThrows<IOException> {
            commandLineNonBlocking("java-incorrect", "--version").getOrThrow()
        }
    }

    @Test
    fun `test commandLine when using correct command`() {
        assertDoesNotThrow {
            commandLine("java", "--version", reasonOfRunningTheCommand = null).getOrThrow()
        }
        assertDoesNotThrow {
            commandLineNonBlocking("java", "--version").getOrThrow()
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `test powerShellCommandLine when using incorrect command`() {
        assertThrows<IOException> {
            powerShellCommandLine("Get-Help-Incorrect", reasonOfRunningTheCommand = null).getOrThrow()
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `test powerShellCommandLine when using correct command`() {
        assertDoesNotThrow {
            powerShellCommandLine("Get-Help", reasonOfRunningTheCommand = null).getOrThrow()
        }
    }
}
