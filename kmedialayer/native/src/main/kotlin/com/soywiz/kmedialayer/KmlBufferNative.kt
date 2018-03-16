package com.soywiz.kmedialayer

import konan.*
import kotlinx.cinterop.*

private fun roundUp(n: Int, m: Int) = if (n >= 0) ((n + m - 1) / m) * m else (n / m) * m

actual class KmlBufferBase private constructor(val data: ByteArray) : KmlBuffer {
    actual override val baseBuffer: KmlBufferBase = this
    actual val size: Int = data.size
    actual constructor(size: Int) : this(ByteArray(roundUp(size, 8)))

    actual inline fun getByte(index: Int): Byte = data[index]
    actual inline fun setByte(index: Int, value: Byte): Unit { data[index] = value }
    actual inline fun getShort(index: Int): Short = data.shortAt(index * 2)
    actual inline fun setShort(index: Int, value: Short): Unit { data.setShortAt(index * 2, value) }
    actual inline fun getInt(index: Int): Int = data.intAt(index * 4)
    actual inline fun setInt(index: Int, value: Int): Unit { data.setIntAt(index * 4, value) }
    actual inline fun getFloat(index: Int): Float = data.floatAt(index * 4)
    actual inline fun setFloat(index: Int, value: Float): Unit { data.setFloatAt(index * 4, value) }
}
