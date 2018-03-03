package com.soywiz.kmedialayer

class KmlGlException(message: String) : RuntimeException(message)

fun KmlGl.getShaderiv(program: Int, type: Int): Int = KmlIntBuffer(1).apply { getShaderiv(program, type, this) }[0]
fun KmlGl.getProgramiv(program: Int, type: Int): Int = KmlIntBuffer(1).apply { getProgramiv(program, type, this) }[0]
fun KmlGl.getIntegerv(pname: Int): Int = KmlIntBuffer(1).apply { getIntegerv(pname, this) }[0]

private inline fun KmlGl.getInfoLog(
    obj: Int,
    getiv: (Int, Int) -> Int,
    getInfoLog: (Int, Int, KmlBuffer?, KmlBuffer?) -> Unit
): String {
    val size = getiv(obj, INFO_LOG_LENGTH)
    val sizev = KmlIntBuffer(1)
    val mbuffer = KmlByteBuffer(size)
    getInfoLog(obj, size, sizev, mbuffer)
    return mbuffer.toAsciiString()
}

fun KmlGl.getShaderInfoLog(shader: Int): String = getInfoLog(shader, ::getShaderiv, ::getShaderInfoLog)
fun KmlGl.getProgramInfoLog(shader: Int): String = getInfoLog(shader, ::getProgramiv, ::getProgramInfoLog)

fun KmlGl.compileShaderAndCheck(shader: Int) {
    compileShader(shader)
    if (getShaderiv(shader, COMPILE_STATUS) != TRUE) {
        throw KmlGlException(getShaderInfoLog(shader))
    }
}

fun KmlGl.linkProgramAndCheck(program: Int) {
    linkProgram(program)
    if (getProgramiv(program, LINK_STATUS) != TRUE) {
        throw KmlGlException(getProgramInfoLog(program))
    }
}
