import com.github.kittinunf.result.Result
import com.google.common.collect.Iterators
import com.matthewbrunelle.blog.bencode.consume
import com.matthewbrunelle.blog.bencode.readN
import com.matthewbrunelle.blog.bencode.readUntil
import com.matthewbrunelle.blog.bencode.readWhile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CharIteratorTest {

    @Test
    fun testReadNLess() {
        val input  = Iterators.peekingIterator("1234567890".iterator())
        assertEquals(Result.of("12345"), input.readN(5))
    }

    @Test
    fun testReadNMore() {
        val input  = Iterators.peekingIterator("1234567890".iterator())
        assertTrue(input.readN(11) is Result.Failure)
    }

    @Test
    fun testReadUntil() {
        val input  = Iterators.peekingIterator("1234567890".iterator())
        assertEquals(Result.of("1234"), input.readUntil('5'))
    }

    @Test
    fun testReadUntilNoTerminator() {
        val input  = Iterators.peekingIterator("1234567890".iterator())
        assertTrue(input.readUntil('a') is Result.Failure)
    }

    @Test
    fun testReadWhile() {
        val input  = Iterators.peekingIterator("12345abcde".iterator())
        assertEquals(Result.of("12345"), input.readWhile { it.isDigit() })
    }

    @Test
    fun testReadCheckFirst() {
        val input  = Iterators.peekingIterator("a12345abcde".iterator())
        assertEquals(Result.of(""), input.readWhile { it.isDigit() })
    }

    @Test
    fun testConsume() {
        val input  = Iterators.peekingIterator("1234567890".iterator())
        assertEquals(Result.of(true), input.consume('1'))
    }

    @Test
    fun testConsumeNoMatch() {
        val input  = Iterators.peekingIterator("1234567890".iterator())
        assertTrue(input.consume('a') is Result.Failure)
    }
}