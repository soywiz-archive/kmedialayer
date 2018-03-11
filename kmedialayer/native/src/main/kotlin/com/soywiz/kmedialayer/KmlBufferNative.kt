package com.soywiz.kmedialayer

import kotlinx.cinterop.*

private fun roundUp(n: Int, m: Int) = if (n >= 0) ((n + m - 1) / m) * m else (n / m) * m

actual class KmlBufferBase private constructor(val data: ByteArray) : KmlBuffer {
    val dataByte: ByteArray = data.uncheckedCast()
    val dataShort: ShortArray = data.uncheckedCast()
    val dataInt: IntArray = data.uncheckedCast()
    val dataFloat: FloatArray = data.uncheckedCast()
    actual override val baseBuffer: KmlBufferBase = this
    actual val size: Int = data.size
    actual constructor(size: Int) : this(ByteArray(roundUp(size, 8)))

    actual inline fun getByte(index: Int): Byte = dataByte[index]
    actual inline fun setByte(index: Int, value: Byte): Unit { dataByte[index] = value }
    actual inline fun getShort(index: Int): Short = dataShort[index]
    actual inline fun setShort(index: Int, value: Short): Unit { dataShort[index] = value }
    actual inline fun getInt(index: Int): Int = dataInt[index]
    actual inline fun setInt(index: Int, value: Int): Unit { dataInt[index] = value }
    actual inline fun getFloat(index: Int): Float = dataFloat[index]
    actual inline fun setFloat(index: Int, value: Float): Unit { dataFloat[index] = value }
}
