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
