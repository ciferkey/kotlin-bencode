package com.matthewbrunelle.blog.bencode

import java.math.BigInteger

sealed class Bencode
data class BencodeString(val s: String) : Bencode()
data class BencodeInteger(val i: BigInteger) : Bencode()
data class BencodeList(val l: List<Bencode>) : Bencode()
data class BencodeDict(val d: Map<Bencode, Bencode>) : Bencode()

const val LIST_MARKER = 'l'
const val INTEGER_MARKER = 'i'
const val DICT_MARKER = 'd'
const val TERMINATOR = 'e'
const val SEPERATOR = ':'