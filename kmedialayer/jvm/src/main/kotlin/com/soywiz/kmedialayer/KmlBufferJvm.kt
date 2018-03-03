package com.soywiz.kmedialayer

import java.nio.*

actual class KmlBuffer private constructor(val nioBuffer: ByteBuffer) : KmlWithBuffer {
   actual override val buffer: KmlBuffer = this
   actual val size: Int = nioBuffer.limit()
   actual constructor(size: Int) : this(ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()))

    actual fun getByte(index: Int): Byte = nioBuffer.get(index * 1)
    actual fun setByte(index: Int, value: Byte): Unit = run { nioBuffer.put(index * 1, value) }
    actual fun getShort(index: Int): Short = nioBuffer.getShort(index * 2)
    actual fun setShort(index: Int, value: Short): Unit = run { nioBuffer.putShort(index * 2, value) }
    actual fun getInt(index: Int): Int = nioBuffer.getInt(index * 4)
    actual fun setInt(index: Int, value: Int): Unit = run { nioBuffer.putInt(index * 4, value) }
    actual fun getFloat(index: Int): Float = nioBuffer.getFloat(index * 4)
    actual fun setFloat(index: Int, value: Float): Unit = run { nioBuffer.putFloat(index * 4, value) }
}
@Suppress("USELESS_CAST") val KmlWithBuffer.nioBuffer: ByteBuffer get() = (buffer as KmlBuffer).nioBuffer
