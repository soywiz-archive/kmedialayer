@file:Suppress("unused", "RedundantUnitReturnType")

package com.soywiz.kmedialayer

interface KmlBuffer { val baseBuffer: KmlBufferBase }
expect class KmlBufferBase : KmlBuffer {
    override val baseBuffer: KmlBufferBase
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

class KmlByteBuffer(override val baseBuffer: KmlBufferBase) : KmlBuffer, Iterable<Byte> {
    companion object {
        const val ELEMENT_SIZE = 1
        operator fun invoke(array: ByteArray): KmlByteBuffer = array.toByteBuffer()
    }
    val size: Int = baseBuffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBufferBase(size * ELEMENT_SIZE))
    operator fun get(index: Int): Byte = baseBuffer.getByte(index)
    operator fun set(index: Int, value: Byte): Unit = run { baseBuffer.setByte(index, value) }
    override fun iterator(): Iterator<Byte> = object : Iterator<Byte> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Byte = get(pos++)
    }
    override fun toString(): String = "KmlByteBuffer(${this.toList()})"
}
fun KmlBuffer.asByteBuffer(): KmlByteBuffer = KmlByteBuffer(this.baseBuffer)
fun ByteArray.toByteBuffer(): KmlByteBuffer = KmlByteBuffer(this.size).also { for (n in 0 until this.size) it[n] = this[n] }
fun KmlByteBuffer.toByteArray(): ByteArray = ByteArray(this.size).also { for (n in 0 until this.size) it[n] = this[n] }

class KmlShortBuffer(override val baseBuffer: KmlBufferBase) : KmlBuffer, Iterable<Short> {
    companion object {
        const val ELEMENT_SIZE = 2
        operator fun invoke(array: ShortArray): KmlShortBuffer = array.toShortBuffer()
    }
    val size: Int = baseBuffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBufferBase(size * ELEMENT_SIZE))
    operator fun get(index: Int): Short = baseBuffer.getShort(index)
    operator fun set(index: Int, value: Short): Unit = run { baseBuffer.setShort(index, value) }
    override fun iterator(): Iterator<Short> = object : Iterator<Short> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Short = get(pos++)
    }
    override fun toString(): String = "KmlShortBuffer(${this.toList()})"
}
fun KmlBuffer.asShortBuffer(): KmlShortBuffer = KmlShortBuffer(this.baseBuffer)
fun ShortArray.toShortBuffer(): KmlShortBuffer = KmlShortBuffer(this.size).also { for (n in 0 until this.size) it[n] = this[n] }
fun KmlShortBuffer.toShortArray(): ShortArray = ShortArray(this.size).also { for (n in 0 until this.size) it[n] = this[n] }

class KmlIntBuffer(override val baseBuffer: KmlBufferBase) : KmlBuffer, Iterable<Int> {
    companion object {
        const val ELEMENT_SIZE = 4
        operator fun invoke(array: IntArray): KmlIntBuffer = array.toIntBuffer()
    }
    val size: Int = baseBuffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBufferBase(size * ELEMENT_SIZE))
    operator fun get(index: Int): Int = baseBuffer.getInt(index)
    operator fun set(index: Int, value: Int): Unit = run { baseBuffer.setInt(index, value) }
    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Int = get(pos++)
    }
    override fun toString(): String = "KmlIntBuffer(${this.toList()})"
}
fun KmlBuffer.asIntBuffer(): KmlIntBuffer = KmlIntBuffer(this.baseBuffer)
fun IntArray.toIntBuffer(): KmlIntBuffer = KmlIntBuffer(this.size).also { for (n in 0 until this.size) it[n] = this[n] }
fun KmlIntBuffer.toIntArray(): IntArray = IntArray(this.size).also { for (n in 0 until this.size) it[n] = this[n] }

class KmlFloatBuffer(override val baseBuffer: KmlBufferBase) : KmlBuffer, Iterable<Float> {
    companion object {
        const val ELEMENT_SIZE = 4
        operator fun invoke(array: FloatArray): KmlFloatBuffer = array.toFloatBuffer()
    }
    val size: Int = baseBuffer.size / ELEMENT_SIZE
    constructor(size: Int) : this(KmlBufferBase(size * ELEMENT_SIZE))
    operator fun get(index: Int): Float = baseBuffer.getFloat(index)
    operator fun set(index: Int, value: Float): Unit = run { baseBuffer.setFloat(index, value) }
    override fun iterator(): Iterator<Float> = object : Iterator<Float> {
        var pos = 0
        override fun hasNext(): Boolean = pos < size
        override fun next(): Float = get(pos++)
    }
    override fun toString(): String = "KmlFloatBuffer(${this.toList()})"
}
fun KmlBuffer.asFloatBuffer(): KmlFloatBuffer = KmlFloatBuffer(this.baseBuffer)
fun FloatArray.toFloatBuffer(): KmlFloatBuffer = KmlFloatBuffer(this.size).also { for (n in 0 until this.size) it[n] = this[n] }
fun KmlFloatBuffer.toFloatArray(): FloatArray = FloatArray(this.size).also { for (n in 0 until this.size) it[n] = this[n] }
