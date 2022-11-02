import com.zionhuang.kugou.KuGou
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class Test {
    @Test
    fun test() = runBlocking {
        val candidates = KuGou.getLyricsCandidate("千年以後 (After A Thousand Years)", "陳零九", 285)
        assertTrue(candidates != null)
        val lyrics = KuGou.getLyrics("楊丞琳", "點水", 259).getOrThrow()
        println(lyrics)
        assertTrue(lyrics != null)
    }
}