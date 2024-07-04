package utils.version

/**
 * A simple semantic version parser
 *
 * See also: [Semantic Versioning](https://semver.org/)
 * */
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
    val buildMetadata: String? = null,
) : Comparable<SemanticVersion> {
    init {
        require(major >= 0) { "Major version must be a non-negative integer." }
        require(minor >= 0) { "Major version must be a non-negative integer." }
        require(patch >= 0) { "Major version must be a non-negative integer." }
        if (preRelease != null) require(preRelease.matches(preReleasePattern)) { "Pre-release version is not valid" }
        if (buildMetadata != null) require(buildMetadata.matches(buildMetadataPattern)) { "Build metadata is not valid" }
    }

    override fun compareTo(other: SemanticVersion): Int =
        when {
            // If major versions are different, the one with higher major version is greater
            major != other.major -> major.compareTo(other.major)
            // If major versions are the same, compare minor versions
            minor != other.minor -> minor.compareTo(other.minor)
            // If major and minor versions are the same, compare patch versions
            patch != other.patch -> patch.compareTo(other.patch)
            // If major, minor, and patch versions are the same for both versions, and they don't have pre-release tags, they are equal
            preRelease == null && other.preRelease == null -> 0
            // If only this version has no pre-release tag, it is considered greater (i.e., a stable version is greater than a pre-release version)
            preRelease == null -> 1
            // If only the other version has no pre-release tag, this version is considered less
            other.preRelease == null -> -1
            // If both versions have pre-release tags, compare them
            else ->
                comparePreRelease(
                    versionA = preRelease,
                    versionB = other.preRelease,
                )
            // Build metadata does not affect the precedence of the version: https://semver.org/#spec-item-11
        }

    override fun toString(): String =
        buildString {
            append("$major.$minor.$patch")
            preRelease?.let { append("-$it") }
            buildMetadata?.let { append("+$it") }
        }

    companion object {
        private val preReleasePattern: Regex =
            Regex("""(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*""")

        private val buildMetadataPattern: Regex = Regex("""[0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*""")

        fun parse(version: String): Result<SemanticVersion> =
            try {
                val (versionPart, buildMetadata) = version.split('+', limit = 2) + listOf(null)
                requireNotNull(versionPart)

                val (versionPartWithoutPreReleaseSuffix, preRelease) = versionPart.split('-', limit = 2) + listOf(null)

                requireNotNull(versionPartWithoutPreReleaseSuffix)
                val (major, minor, patch) = versionPartWithoutPreReleaseSuffix.split('.', limit = 3)

                Result.success(
                    SemanticVersion(
                        major = major.toInt(),
                        minor = minor.toInt(),
                        patch = patch.toInt(),
                        preRelease = preRelease,
                        buildMetadata = buildMetadata,
                    ),
                )
            } catch (e: Exception) {
                Result.failure(e)
            }

        /**
         * Compare two pre-release versions (e.g., "alpha.1" and "beta.2")
         * @return Will be used in [Comparable.compareTo]
         * */
        private fun comparePreRelease(
            versionA: String,
            versionB: String,
        ): Int {
            val aParts = versionA.split('.')
            val bParts = versionB.split('.')

            // Find the minimum length of the two parts lists to avoid out-of-bounds errors
            val lowestPartsSize = minOf(aParts.size, bParts.size)

            for (i in 0 until lowestPartsSize) {
                val partsComparison =
                    comparePreReleasePart(
                        partA = aParts[i],
                        partB = bParts[i],
                    )
                if (partsComparison != 0) return partsComparison
            }

            // If all compared parts are equal, compare the lengths of the pre-release tags
            // The shorter pre-release tag is considered lesser (e.g., "alpha" < "alpha.1")
            return aParts.size - bParts.size
        }

        /**
         * Compare individual parts of pre-release versions (e.g., "alpha" vs "beta" or "1" vs "2")
         * */
        private fun comparePreReleasePart(
            partA: String,
            partB: String,
        ): Int {
            val aAsNumber: Int? = partA.toIntOrNull()
            val bAsNumber: Int? = partB.toIntOrNull()

            return when {
                // If both parts are numeric, compare them as integers
                aAsNumber != null && bAsNumber != null -> aAsNumber - bAsNumber
                // If the only first part is numeric, it's considered greater,
                // it's considered greater as numeric parts are more specific
                aAsNumber != null -> 1
                // If only the second part is numeric, the first part is considered lesser
                bAsNumber != null -> -1
                // If neither part is numeric, compare them lexicographically (alphabetically)
                else -> partA.compareTo(partB)
            }
        }
    }
}
