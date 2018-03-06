package com.soywiz.kmedialayer

import org.khronos.webgl.*

private fun roundUp(n: Int, m: Int) = if (n >= 0) ((n + m - 1) / m) * m else (n / m) * m

actual class KmlBufferBase private constructor(val arrayBuffer: ArrayBuffer) : KmlBuffer {
   val arrayByte = Int8Array(arrayBuffer)
   val arrayUByte = Uint8Array(arrayBuffer)
   val arrayShort = Int16Array(arrayBuffer)
   val arrayInt = Int32Array(arrayBuffer)
   val arrayFloat = Float32Array(arrayBuffer)
   actual override val baseBuffer: KmlBufferBase = this
   actual val size: Int = arrayBuffer.byteLength
   actual constructor(size: Int) : this(ArrayBuffer(roundUp(size, 8)))

    actual fun getByte(index: Int): Byte = arrayByte[index]
    actual fun setByte(index: Int, value: Byte): Unit = run { arrayByte[index] = value }
    actual fun getShort(index: Int): Short = arrayShort[index]
    actual fun setShort(index: Int, value: Short): Unit = run { arrayShort[index] = value }
    actual fun getInt(index: Int): Int = arrayInt[index]
    actual fun setInt(index: Int, value: Int): Unit = run { arrayInt[index] = value }
    actual fun getFloat(index: Int): Float = arrayFloat[index]
    actual fun setFloat(index: Int, value: Float): Unit = run { arrayFloat[index] = value }
}
@Suppress("USELESS_CAST") val KmlBuffer.arrayBuffer: ArrayBuffer get() = (baseBuffer as KmlBufferBase).arrayBuffer
@Suppress("USELESS_CAST") val KmlBuffer.arrayByte: Int8Array get() = (baseBuffer as KmlBufferBase).arrayByte
@Suppress("USELESS_CAST") val KmlBuffer.arrayUByte: Uint8Array get() = (baseBuffer as KmlBufferBase).arrayUByte
@Suppress("USELESS_CAST") val KmlBuffer.arrayShort: Int16Array get() = (baseBuffer as KmlBufferBase).arrayShort
@Suppress("USELESS_CAST") val KmlBuffer.arrayInt: Int32Array get() = (baseBuffer as KmlBufferBase).arrayInt
@Suppress("USELESS_CAST") val KmlBuffer.arrayFloat: Float32Array get() = (baseBuffer as KmlBufferBase).arrayFloat
