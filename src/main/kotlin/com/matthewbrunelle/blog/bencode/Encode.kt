package com.matthewbrunelle.blog.bencode

fun encode(bencode: Bencode): String {

    return when (bencode) {
        is BencodeString -> encodeString(bencode)
        is BencodeInteger -> encodeInteger(bencode)
        is BencodeList -> encodeList(bencode)
        is BencodeDict -> encodeDict(bencode)
    }
}

fun encodeString(bencode: BencodeString): String {
    return bencode.s.length.toString() + bencode.s
}

fun encodeInteger(bencode: BencodeInteger): String {
    return "i" + bencode.i.toString() + "e"
}

fun encodeList(bencode: BencodeList): String {
    return bencode.l.map { encode(it) }.joinToString(":", prefix = LIST_MARKER.toString(), postfix = TERMINATOR.toString())
}

fun encodeDict(bencode: BencodeDict): String {
    return bencode.d.entries.map { encode(it.key) + ":" + encode(it.value) }.joinToString(":", prefix = LIST_MARKER.toString(), postfix = TERMINATOR.toString())
}