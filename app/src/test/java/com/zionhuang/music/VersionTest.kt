package com.zionhuang.music

import com.zionhuang.music.update.Version
import org.junit.Test
import org.junit.Assert.*


class VersionTest {
    @Test
    fun testToString(){
        assertEquals(Version(0, 1, 0).toString(), "0.1.0")
        assertEquals(Version(1, 3, 0, "beta").toString(), "1.3.0-beta")
    }

    @Test
    fun testCompare() {
        assertTrue(Version(0, 1, 0) < Version(0, 1, 1))
        assertTrue(Version(0, 1, 1) < Version(0, 2, 0))
        assertTrue(Version(0, 1, 1) < Version(1, 0, 0))
        assertTrue(Version(0, 1, 0, "beta") < Version(0, 1, 0))
        assertTrue(Version(0, 1, 0, "alpha") < Version(0, 1, 0, "beta"))
    }
}