package com.zionhuang.music.update

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
) {
    private val preReleaseWeight: Int get() = if (preRelease == null) Int.MAX_VALUE else preRelease[0].code

    operator fun compareTo(v2: Version): Int {
        if (major != v2.major) return major - v2.major
        if (minor != v2.minor) return minor - v2.minor
        if (patch != v2.patch) return patch - v2.patch
        if (preReleaseWeight != v2.preReleaseWeight) return preReleaseWeight - v2.preReleaseWeight
        return 0
    }

    override fun toString(): String = if (preRelease != null) "$major.$minor.$patch-$preRelease" else "$major.$minor.$patch"

    companion object {
        fun parse(s: String): Version {
            val (major, minor, patch) = s.split('-')[0].split('.').map { it.toInt() }
            val preRelease = s.split('-').getOrNull(1)
            return Version(major, minor, patch, preRelease)
        }
    }
}
