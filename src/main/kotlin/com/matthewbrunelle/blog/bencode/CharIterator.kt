package com.matthewbrunelle.blog.bencode

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import com.google.common.collect.PeekingIterator

typealias CharIterator = PeekingIterator<Char>

/**
 * Reads [size] characters from the iterator.
 */
fun CharIterator.readN(size: Int): Result<String, Exception> {
    return Result.of<String, Exception> {
        val nextN = CharArray(size)
        for (i in 0 until size) {
            nextN[i] = this.next()
        }
        String(nextN)
    }.mapError {
        DecodeException("Was not able to read $size characters", it)
    }
}

/**
 * Reads characters from the [iterator] until it encounters the terminating character. Consumes the terminating character from the iterator but does not return it as part of the result.
 */
fun CharIterator.readUntil(terminator: Char): Result<String, Exception> {
    return this.readWhile { it != terminator }.flatMap { readString ->
        this.consume(terminator).map {
            readString
        }
    }.mapError {
        DecodeException("Failed reading until '$terminator'.")
    }
}

/**
 * Reads characters from [iterator] as long [predicate] is true for each character. Returns the resulting string.
 */
fun CharIterator.readWhile(predicate: (Char) -> Boolean): Result<String, Exception> {
    return Result.of<String, Exception> {
        val readString = StringBuilder()
        while (predicate.invoke(this.peek())) {
            readString.append(this.next())
        }
        readString.toString()
    }.mapError {
        DecodeException("Failed reading based on given predicate", it)
    }
}

/**
 * Reads [char] from [iterator] if it is the next character. Returns true wrapped in [Result] if successful.
 */
fun CharIterator.consume(char: Char): Result<Boolean, Exception> {
    return if (this.peek() == char) {
        this.next()
        Result.of(true)
    } else {
        Result.error(DecodeException("Could not consume '$char'"))
    }
}