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
    fun testDecodeAndEncodeString() {
        val input = "4:spam"
        val decoded = decode(input)
        assertEquals(BencodeString("spam"), decoded.get())
        val encoded = decoded.get().encode()
        assertEquals(input, encoded)
    }

    @Test
    fun testDecodeAndEncodeInteger() {
        val input = "i3e"
        val decoded = decode(input)
        assertEquals(BencodeInteger(BigInteger("3")), decoded.get())
        val encoded = decoded.get().encode()
        assertEquals(input, encoded)
    }

    @Test
    fun testDecodeAndEncodeList() {
        val input = "l4:spam4:eggse"
        val decoded = decode(input)

        val expected = BencodeList(mutableListOf(
                BencodeString("spam"),
                BencodeString("eggs")
        ))

        assertEquals(expected, decoded.get())
        val encoded = decoded.get().encode()
        assertEquals(input, encoded)
    }

    @Test
    fun testDecodeAndEncodeDict() {
        val input = "d3:cow3:moo4:spam4:eggse"
        val decoded = decode(input)

        val expected = BencodeDict(mutableMapOf(
                Pair(BencodeString("cow"), BencodeString("moo")),
                Pair(BencodeString("spam"), BencodeString("eggs"))
        ))

        assertEquals(expected, decoded.get())
        val encoded = decoded.get().encode()
        assertEquals(input, encoded)
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
    fun testDecodeAndEncodeAliceFile() {

        val input = File("src/test/resources/alice.torrent").readText(Charsets.US_ASCII)

        val decoded = decode(input)
        val encoded = decoded.get().encode()
        assertEquals(input, encoded)
    }

    @Test
    fun testDecodeAndEncodeBunnyFile() {

        val input = File("src/test/resources/bunny.torrent").readText(Charsets.US_ASCII)

        val decoded = decode(input)
//        val encoded = decoded.get().encode()
//        assertEquals(input, encoded)
    }
}