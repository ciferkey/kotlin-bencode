package com.matthewbrunelle.blog.bencode

import java.math.BigInteger

const val LIST_MARKER = 'l'
const val INTEGER_MARKER = 'i'
const val DICT_MARKER = 'd'
const val TERMINATOR = 'e'
const val SEPERATOR = ':'

sealed class Bencode {
    abstract fun encode(): String
}

data class BencodeString(val s: String) : Bencode() {
    override fun encode(): String {
        return s.length.toString() + s
    }
}

data class BencodeInteger(val i: BigInteger) : Bencode() {
    override fun encode(): String {
        return LIST_MARKER + i.toString() + TERMINATOR
    }
}

data class BencodeList(val l: List<Bencode>) : Bencode() {
    override fun encode(): String {
        return l.map { it.encode() }
                .joinToString(SEPERATOR.toString(),
                        prefix = LIST_MARKER.toString(),
                        postfix = TERMINATOR.toString()
                )
    }
}

data class BencodeDict(val d: Map<Bencode, Bencode>) : Bencode() {
    override fun encode(): String {
        return d.entries.map { it.key.encode() + ":" + it.value.encode() }
                .joinToString(SEPERATOR.toString(),
                        prefix = LIST_MARKER.toString(),
                        postfix = TERMINATOR.toString()
                )
    }
}