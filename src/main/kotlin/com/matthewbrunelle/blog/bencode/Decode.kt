package com.matthewbrunelle.blog.bencode

import com.github.kittinunf.result.*
import com.google.common.collect.Iterators

class DecodeException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, ex: Exception) : super(message, ex)
}

class Decoder(val input: String) {
    val iterator = Iterators.peekingIterator(input.iterator())

    fun decode(): Result<Bencode, Exception> {
        if (iterator.hasNext()) {
            val marker = iterator.peek()

            return when (marker) {
                in '0'..'9' -> decodeString()
                INTEGER_MARKER -> decodeInteger()
                LIST_MARKER -> decodeList()
                DICT_MARKER -> decodeDict()
                else -> Result.error(DecodeException("Unknown identifier '$marker"))
            }
        }
        return Result.error(DecodeException("Nothing to decode"))
    }


    private fun decodeString(): Result<BencodeString, Exception> {
        return iterator.readWhile {it.isDigit()}
                .flatMap {length ->
                    iterator.consume(SEPARATOR).flatMap {
                        iterator.readN(length.toInt()).map {
                            BencodeString(it)
                        }
                    }
                }
    }

    private fun decodeInteger(): Result<BencodeInteger, Exception> {

        // TODO: "All encodings with a leading zero, such as i03e, are invalid, other than i0e, which of course corresponds to 0."

        return iterator.consume(INTEGER_MARKER).flatMap {
            iterator.readUntil(TERMINATOR).flatMap {
                Result.of<BencodeInteger, Exception> {
                    BencodeInteger(it.toBigInteger())
                }
            }
        }
    }

    private fun decodeList(): Result<BencodeList, Exception> {
        return iterator.consume(LIST_MARKER).flatMap {
            Result.of<BencodeList, Exception> {
                val items = mutableListOf<Bencode>()
                while (!iterator.consume(TERMINATOR).getOrElse(false)) {
                    decode().map {
                        items.add(it)
                    }
                }
                BencodeList(items)
            }.mapError {
                DecodeException("Failed decoding list. No TERMINATOR $TERMINATOR.")
            }
        }
    }

    private fun decodeDict(): Result<BencodeDict, Exception> {
        return iterator.consume(DICT_MARKER).flatMap {
            Result.of<BencodeDict, Exception> {
                val items = mutableMapOf<Bencode, Bencode>()
                while (!iterator.consume(TERMINATOR).getOrElse(false)) {
                    decode().fanout {
                        decode()
                    }.success {
                        items[it.first] = it.second
                    }
                }
                BencodeDict(items)
            }.mapError {
                DecodeException("Failed decoding list. No TERMINATOR $TERMINATOR.")
            }
        }
    }
}