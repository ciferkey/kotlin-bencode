package com.matthewbrunelle.blog.bencode

import com.github.kittinunf.result.*
import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import kotlin.reflect.KFunction1

const val LIST_MARKER = 'l'
const val INTEGER_MARKER = 'i'
const val DICT_MARKER = 'd'
const val TERMINATOR = 'e'
const val SEPERATOR = ':'

class DecodeException(s: String) : Exception(s)

fun decode(s: String): Result<Bencode, Exception> {
    val iterator = Iterators.peekingIterator(s.iterator())
    return decode(iterator)
}

fun decode(iterator: PeekingIterator<Char>): Result<Bencode, Exception> {
    if (iterator.hasNext()) {
        val marker = iterator.peek()

        return when (marker) {
            in '1'..'9' -> decodeString(iterator)
            INTEGER_MARKER -> decodeInteger(iterator)
            LIST_MARKER -> decodeList(iterator)
            DICT_MARKER -> decodeDict(iterator)
            else -> Result.error(DecodeException("Unknown identifier '$marker"))
        }
    }
    return Result.error(DecodeException("Nothing to decode"))
}

fun decodeString(iterator: PeekingIterator<Char>): Result<BencodeString, Exception> {
    return try {
        val length = Integer.valueOf(readWhile(iterator, Char::isDigit))
        return consume(iterator, SEPERATOR).flatMap {
            readN(iterator, length).map {
                BencodeString(it)
            }
        }
    } catch (e: NumberFormatException) {
        Result.error(e)
    }
}

fun decodeInteger(iterator: PeekingIterator<Char>): Result<BencodeInteger, Exception> {

    // TODO: "All encodings with a leading zero, such as i03e, are invalid, other than i0e, which of course corresponds to 0."

    return consume(iterator, INTEGER_MARKER).flatMap {
        readUntil(iterator, TERMINATOR).flatMap {
            try {
                val integer = it.toBigInteger()
                Result.of(BencodeInteger(integer))
            } catch (e: NumberFormatException) {
                Result.error(e)
            }
        }
    }
}

fun decodeList(iterator: PeekingIterator<Char>): Result<BencodeList, Exception> {
    val consumedMarker = consume(iterator, LIST_MARKER)

    if (! consumedMarker.getOrElse(false)) {
        return Result.error(consumedMarker.component2() ?: Exception())
    }

    val items = mutableListOf<Bencode>()
    while (iterator.hasNext()) {
        if (consume(iterator, TERMINATOR).getOrElse(false)) {
            return Result.of(BencodeList(items))
        }
        decode(iterator).success {
            items.add(it)
        }
    }
    return Result.error(DecodeException("Failed decoding list. No TERMINATOR $TERMINATOR. Read: $items"))

}

fun decodeDict(iterator: PeekingIterator<Char>): Result<BencodeDict, Exception> {
    val consumedMarker = consume(iterator, DICT_MARKER)
    if (! consumedMarker.getOrElse(false)) {
        return Result.error(consumedMarker.component2() ?: Exception())
    }

    val items = mutableMapOf<Bencode, Bencode>()
    while (iterator.hasNext()) {
        if (consume(iterator, TERMINATOR).getOrElse(false)) {
            return Result.of(BencodeDict(items))
        }
        decode(iterator).fanout {
            decode(iterator)
        }.success {
            items[it.first] = it.second
        }
    }
    return Result.error(DecodeException("Failed decoding list. No TERMINATOR $TERMINATOR. Read: $items"))
}

fun readN(iterator: PeekingIterator<Char>, size: Int): Result<String, Exception> {
    val s = StringBuilder()
    repeat(size) {
        if (iterator.hasNext()) {
            if (it != s.length) {
                println("$it ${s.length}")
            }
            s.append(iterator.next())
        } else {
            return Result.error(DecodeException("Was not able to read $size characters"))
        }
    }
    return Result.of(s.toString())
}

fun readUntil(iterator: PeekingIterator<Char>, terminator: Char): Result<String, Exception> {
    val s = StringBuilder()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (terminator == next) {
            return Result.of(s.toString())
        }
        s.append(next)
    }
    return Result.error(DecodeException("Failed reading until '$terminator'. Read: '$s'"))
}


fun readWhile(iterator: PeekingIterator<Char>, predicate: KFunction1<Char, Boolean>): String {
    val s = StringBuilder()
    do {
        s.append(iterator.next())
    } while (predicate.invoke(iterator.peek()))
    return s.toString()
}

fun consume(iterator: PeekingIterator<Char>, char: Char): Result<Boolean, Exception> {
    return if (iterator.peek() == char) {
        iterator.next()
        Result.of(true)
    } else {
        Result.error(DecodeException("Could not consume '$char'"))
    }
}