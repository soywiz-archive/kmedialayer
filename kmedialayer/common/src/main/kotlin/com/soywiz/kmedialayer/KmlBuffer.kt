package com.soywiz.kmedialayer

interface KmlWithBuffer { val buffer: KmlBuffer }
expect class KmlBuffer : KmlWithBuffer {
    override val buffer: KmlBuffer
    val size: Int
    constructor(size: Int)

    fun getByte(index: Int): Byte
    fun setByte(index: Int, value: Byte): Unit
    fun getShort(index: Int): Short
    fun setShort(index: Int, value: Short): Unit
    fun getInt(index: Int): Int
    fun setInt(index: Int, value: Int): Unit
    fun getFloat(index: Int): Float
    fun setFloat(index: Int, value: Float): Unit
}

class KmlByteBuffer(override val buffer: KmlBuffer) : KmlWithBuffer, Iterable<Byte> {
    companion object { const val ELEMENT_SIZE = 1 }
    val size: Int = buffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBuffer(size * ELEMENT_SIZE))
    operator fun get(index: Int): Byte = buffer.getByte(index)
    operator fun set(index: Int, value: Byte): Unit = run { buffer.setByte(index, value) }
    override fun iterator(): Iterator<Byte> = object : Iterator<Byte> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Byte = get(pos++)
    }
    override fun toString(): String = "KmlByteBuffer(${this.toList()})"
}
fun KmlWithBuffer.asByteBuffer() = KmlByteBuffer(this.buffer)

class KmlShortBuffer(override val buffer: KmlBuffer) : KmlWithBuffer, Iterable<Short> {
    companion object { const val ELEMENT_SIZE = 2 }
    val size: Int = buffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBuffer(size * ELEMENT_SIZE))
    operator fun get(index: Int): Short = buffer.getShort(index)
    operator fun set(index: Int, value: Short): Unit = run { buffer.setShort(index, value) }
    override fun iterator(): Iterator<Short> = object : Iterator<Short> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Short = get(pos++)
    }
    override fun toString(): String = "KmlShortBuffer(${this.toList()})"
}
fun KmlWithBuffer.asShortBuffer() = KmlShortBuffer(this.buffer)

class KmlIntBuffer(override val buffer: KmlBuffer) : KmlWithBuffer, Iterable<Int> {
    companion object { const val ELEMENT_SIZE = 4 }
    val size: Int = buffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBuffer(size * ELEMENT_SIZE))
    operator fun get(index: Int): Int = buffer.getInt(index)
    operator fun set(index: Int, value: Int): Unit = run { buffer.setInt(index, value) }
    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Int = get(pos++)
    }
    override fun toString(): String = "KmlIntBuffer(${this.toList()})"
}
fun KmlWithBuffer.asIntBuffer() = KmlIntBuffer(this.buffer)

class KmlFloatBuffer(override val buffer: KmlBuffer) : KmlWithBuffer, Iterable<Float> {
    companion object { const val ELEMENT_SIZE = 4 }
    val size: Int = buffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBuffer(size * ELEMENT_SIZE))
    operator fun get(index: Int): Float = buffer.getFloat(index)
    operator fun set(index: Int, value: Float): Unit = run { buffer.setFloat(index, value) }
    override fun iterator(): Iterator<Float> = object : Iterator<Float> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Float = get(pos++)
    }
    override fun toString(): String = "KmlFloatBuffer(${this.toList()})"
}
fun KmlWithBuffer.asFloatBuffer() = KmlFloatBuffer(this.buffer)
