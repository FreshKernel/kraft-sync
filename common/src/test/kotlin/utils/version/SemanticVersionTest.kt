package utils.version

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SemanticVersionTest {
    @Test
    fun `test parse result`() {
        val major = 22
        val minor = 50
        val patch = 11
        val preRelease = "alpha"
        val buildMetadata = "151e629ee3c41f3bf996d2c74364bed01afe47a8"

        val version =
            SemanticVersion(
                major = major,
                minor = minor,
                patch = patch,
                preRelease = preRelease,
                buildMetadata = buildMetadata,
            )
        assertEquals(version.major, major)
        assertEquals(version.minor, minor)
        assertEquals(version.patch, patch)
        assertEquals(version.preRelease, preRelease)
        assertEquals(version.buildMetadata, buildMetadata)

        val version2 =
            SemanticVersion
                .parse("$major.$minor.$patch-$preRelease+$buildMetadata")
                .getOrThrow()
        assertEquals(version2.major, major)
        assertEquals(version2.minor, minor)
        assertEquals(version2.patch, patch)
        assertEquals(version2.preRelease, preRelease)
        assertEquals(version2.buildMetadata, buildMetadata)
    }

    @Test
    fun `validate input`() {
        // Major, minor and patch
        assertThrows<IllegalArgumentException> {
            SemanticVersion(major = -1, minor = 1, patch = 1)
        }
        assertThrows<IllegalArgumentException> {
            SemanticVersion(major = 1, minor = -1, patch = 1)
        }
        assertThrows<IllegalArgumentException> {
            SemanticVersion(major = 1, minor = 1, patch = -1)
        }
        assertThrows<IllegalArgumentException> {
            SemanticVersion(major = -1, minor = -1, patch = -1)
        }

        // Pre-Release
        assertThrows<IllegalArgumentException> {
            SemanticVersion(major = 1, minor = 1, patch = 1, preRelease = "")
        }
        assertThrows<IllegalArgumentException> {
            SemanticVersion.parse("1.1.1-").getOrThrow()
        }

        // Build Metadata
        assertThrows<IllegalArgumentException> {
            SemanticVersion(major = 1, minor = 1, patch = 1, buildMetadata = "")
        }
    }

    @Test
    fun `test equal versions`() {
        assertEquals(
            SemanticVersion.parse("1.0.0").getOrThrow(),
            SemanticVersion.parse("1.0.0").getOrThrow(),
        )
    }

    @Test
    fun `test not equal versions`() {
        assertNotEquals(
            SemanticVersion.parse("9.0.0").getOrThrow(),
            SemanticVersion.parse("1.0.0").getOrThrow(),
        )
    }

    @Test
    fun `test major version comparison`() {
        assertEquals(
            SemanticVersion.parse("1.0.0").getOrThrow().major,
            SemanticVersion.parse("1.0.0").getOrThrow().major,
        )
        assertNotEquals(
            SemanticVersion.parse("2.0.0").getOrThrow().major,
            SemanticVersion.parse("3.0.0").getOrThrow().major,
        )

        assertTrue(
            SemanticVersion.parse("2.0.0").getOrThrow()
                > SemanticVersion.parse("1.10.0").getOrThrow(),
        )

        assertFalse(
            SemanticVersion.parse("1.0.0").getOrThrow()
                > SemanticVersion.parse("2.10.0").getOrThrow(),
        )
    }

    @Test
    fun `test minor version comparison`() {
        assertEquals(
            SemanticVersion.parse("1.3.0").getOrThrow().minor,
            SemanticVersion.parse("1.3.0").getOrThrow().minor,
        )
        assertNotEquals(
            SemanticVersion.parse("1.3.2").getOrThrow().minor,
            SemanticVersion.parse("1.5.1").getOrThrow().minor,
        )

        assertTrue(
            SemanticVersion.parse("1.5.0").getOrThrow()
                > SemanticVersion.parse("1.4.20").getOrThrow(),
        )
        assertFalse(
            SemanticVersion.parse("1.4.0").getOrThrow()
                > SemanticVersion.parse("1.5.20").getOrThrow(),
        )
    }

    @Test
    fun `test patch version comparison`() {
        assertEquals(
            SemanticVersion.parse("1.0.1").getOrThrow().patch,
            SemanticVersion.parse("1.0.1").getOrThrow().patch,
        )
        assertNotEquals(
            SemanticVersion.parse("1.0.2").getOrThrow().patch,
            SemanticVersion.parse("1.0.1").getOrThrow().patch,
        )

        assertTrue(
            SemanticVersion.parse("1.0.20").getOrThrow()
                > SemanticVersion.parse("1.0.19").getOrThrow(),
        )

        assertFalse(
            SemanticVersion.parse("1.0.19").getOrThrow()
                > SemanticVersion.parse("1.0.20").getOrThrow(),
        )
    }

    @Test
    fun `test pre release comparison`() {
        assertTrue(
            SemanticVersion.parse("1.0.0-beta").getOrThrow()
                >
                SemanticVersion.parse("1.0.0-alpha").getOrThrow(),
        )

        assertFalse(
            SemanticVersion.parse("1.0.0-beta").getOrThrow()
                >
                SemanticVersion.parse("1.0.0-beta").getOrThrow(),
        )

        assertEquals(
            SemanticVersion.parse("1.0.0-beta").getOrThrow(),
            SemanticVersion.parse("1.0.0-beta").getOrThrow(),
        )

        assertFalse(
            SemanticVersion.parse("1.0.0-alpha").getOrThrow()
                >
                SemanticVersion.parse("1.0.0-beta").getOrThrow(),
        )
    }

    @Test
    fun `test numeric pre release comparison`() {
        assertTrue(
            SemanticVersion.parse("1.0.0-alpha.2").getOrThrow()
                >
                SemanticVersion.parse("1.0.0-alpha.1").getOrThrow(),
        )

        assertFalse(
            SemanticVersion.parse("1.0.0-alpha.1").getOrThrow()
                >
                SemanticVersion.parse("1.0.0-alpha.2").getOrThrow(),
        )
    }

    @Test
    fun `test mixed pre release comparison`() {
        assertTrue(
            SemanticVersion.parse("1.0.0-alpha.4").getOrThrow()
                > SemanticVersion.parse("1.0.0-alpha").getOrThrow(),
        )
        assertFalse(
            SemanticVersion.parse("1.0.0-alpha").getOrThrow()
                > SemanticVersion.parse("1.0.0-alpha.4").getOrThrow(),
        )
    }

    @Test
    fun `test numeric with non numeric pre release comparison`() {
        assertTrue(
            SemanticVersion.parse("1.0.0-1").getOrThrow() >
                SemanticVersion.parse("1.0.0-alpha").getOrThrow(),
        )
        assertFalse(
            SemanticVersion.parse("1.0.0-alpha").getOrThrow() >
                SemanticVersion.parse("1.0.0-1").getOrThrow(),
        )
    }

    @Test
    fun `test null pre release with non null pre release`() {
        assertTrue(
            SemanticVersion.parse("1.0.0").getOrThrow()
                > SemanticVersion.parse("1.0.0-alpha").getOrThrow(),
        )
        assertFalse(
            SemanticVersion.parse("1.0.0-alpha").getOrThrow()
                > SemanticVersion.parse("1.0.0").getOrThrow(),
        )
    }

    // Build metadata does not affect the precedence of the version: https://semver.org/#spec-item-11

    @Test
    fun `test version with build metadata`() {
        val version1 = SemanticVersion.parse("1.0.0+001").getOrThrow()
        val version2 = SemanticVersion.parse("1.0.0+002").getOrThrow()
        assertEquals(0, version1.compareTo(version2))
    }

    @Test
    fun `test pre release version with build metadata`() {
        val version1 = SemanticVersion.parse("1.0.0-alpha+001").getOrThrow()
        val version2 = SemanticVersion.parse("1.0.0-alpha+002").getOrThrow()
        assertEquals(0, version1.compareTo(version2))
    }
}
