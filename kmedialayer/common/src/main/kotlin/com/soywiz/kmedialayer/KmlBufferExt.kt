package com.soywiz.kmedialayer

fun KmlBuffer.toAsciiString(): String {
    var out = ""
    for (n in 0 until baseBuffer.size) {
        val b = baseBuffer.getByte(n)
        if (b == 0.toByte()) break
        out += b.toChar()
    }
    return out
}

fun KmlBuffer.putAsciiString(str: String): KmlBuffer {
    var n = 0
    for (c in str) {
        if (baseBuffer.size >= n) baseBuffer.setByte(n++, c.toByte())
    }
    if (baseBuffer.size >= n) baseBuffer.setByte(n++, 0.toByte())
    return this
}
