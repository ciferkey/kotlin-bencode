import com.github.kittinunf.result.Result
import com.matthewbrunelle.blog.bencode.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.math.BigInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BencodeTest {

    @Test
    fun testDecodeString() {
        val result = decode("4:spam")
        assertEquals(BencodeString("spam"), result.get())
    }

    @Test
    fun testDecodeInteger() {
        val result = decode("i3e")
        assertEquals(BencodeInteger(BigInteger("3")), result.get())
    }

    @Test
    fun testDecodeList() {
        val result = decode("l4:spam4:eggse")

        val expected = BencodeList(mutableListOf(
                BencodeString("spam"),
                BencodeString("eggs")
        ))

        assertEquals(expected, result.get())
    }

    @Test
    fun testDecodeDict() {
        val result = decode("d3:cow3:moo4:spam4:eggse")

        val expected = BencodeDict(mutableMapOf(
                Pair(BencodeString("cow"), BencodeString("moo")),
                Pair(BencodeString("spam"), BencodeString("eggs"))
        ))

        assertEquals(expected, result.get())
    }

    @Test
    fun testDecodeEmpty() {
        val result = decode("")
        assertTrue(result is Result.Failure)
    }

    @Test
    fun testDecodeUnknownMarker() {
        val result = decode("t")
        assertTrue(result is Result.Failure)
    }

    @Test
    fun testDecodeFile() {

        val s = File("src/test/resources/alice.torrent").readText(Charsets.UTF_8)

        val result = decode(s)
        println(result)
    }
}