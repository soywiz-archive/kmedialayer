object KmlGenBuffer {
    @JvmStatic
    fun main(args: Array<String>) {
        printToFile("kmedialayer/common/src/main/kotlin/com/soywiz/kmedialayer/KmlBuffer.kt") { generateCommon() }
        printToFile("kmedialayer/android/src/main/kotlin/com/soywiz/kmedialayer/KmlBufferAndroid.kt") { generateJvm() }
        printToFile("kmedialayer/jvm/src/main/kotlin/com/soywiz/kmedialayer/KmlBufferJvm.kt") { generateJvm() }
        printToFile("kmedialayer/js/src/main/kotlin/com/soywiz/kmedialayer/KmlBufferJs.kt") { generateJs() }
    }

    open class KmlType(val name: String, val size: Int) {
        open val bbMethodName: String = name
    }
    object KmlByte : KmlType("Byte", 1) {
        override val bbMethodName: String = ""
    }
    object KmlShort : KmlType("Short", 2)
    object KmlInt : KmlType("Int", 4)
    object KmlFloat : KmlType("Float", 4)
    val types = listOf(KmlByte, KmlShort, KmlInt, KmlFloat)

    fun Printer.generateCommon() {
        println("@file:Suppress(\"unused\", \"RedundantUnitReturnType\")")
        println("")
        println("package com.soywiz.kmedialayer")
        println("")
        println("interface KmlBuffer { val baseBuffer: KmlBufferBase }")
        println("expect class KmlBufferBase : KmlBuffer {")
        println("    override val baseBuffer: KmlBufferBase")
        println("    val size: Int")
        println("    constructor(size: Int)")
        println("")
        for (type in types) {
            println("    fun get${type.name}(index: Int): ${type.name}")
            println("    fun set${type.name}(index: Int, value: ${type.name}): Unit")
        }
        println("}")
        println("")
        for (type in types) {
            val name = type.name
            val bufferName = "Kml${name}Buffer"
            val arrayName = "${name}Array"

            println("class $bufferName(override val baseBuffer: KmlBufferBase) : KmlBuffer, Iterable<$name> {")
            println("    companion object {")
            println("        const val ELEMENT_SIZE = ${type.size}")
            println("        operator fun invoke(array: $arrayName): $bufferName = array.to${name}Buffer()")
            println("    }")
            println("    val size: Int = baseBuffer.size / ELEMENT_SIZE")
            println("    constructor(size: Int) : this(KmlBufferBase(size * ELEMENT_SIZE))")
            println("    operator fun get(index: Int): $name = baseBuffer.get$name(index)")
            println("    operator fun set(index: Int, value: $name): Unit = run { baseBuffer.set$name(index, value) }")
            println("    override fun iterator(): Iterator<$name> = object : Iterator<$name> {")
            println("        var pos = 0")
            println("        override fun hasNext(): Boolean = pos < size")
            println("        override fun next(): $name = get(pos++)")
            println("    }")
            println("    override fun toString(): String = \"$bufferName(\${this.toList()})\"")
            println("}")
            println("fun KmlBuffer.as${name}Buffer(): $bufferName = $bufferName(this.baseBuffer)")
            println("fun $arrayName.to${name}Buffer(): $bufferName = $bufferName(this.size).also { for (n in 0 until this.size) it[n] = this[n] }")
            println("fun $bufferName.to${name}Array(): $arrayName = $arrayName(this.size).also { for (n in 0 until this.size) it[n] = this[n] }")
            println("")
        }
    }

    fun Printer.generateJvm() {
        println("package com.soywiz.kmedialayer")
        println("")
        println("import java.nio.*")
        println("")
        println("actual class KmlBufferBase private constructor(val nioBuffer: ByteBuffer) : KmlBuffer {")
        println("    val byteBuffeer = nioBuffer")
        println("    val shortBuffer = nioBuffer.asShortBuffer()")
        println("    val intBuffer = nioBuffer.asIntBuffer()")
        println("    val floatBuffer = nioBuffer.asFloatBuffer()")
        println("    actual override val baseBuffer: KmlBufferBase = this")
        println("    actual val size: Int = nioBuffer.limit()")
        println("    actual constructor(size: Int) : this(ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()))")
        println("")
        for (type in types) {
            println("    actual fun get${type.name}(index: Int): ${type.name} = nioBuffer.get${type.bbMethodName}(index * ${type.size})")
            println("    actual fun set${type.name}(index: Int, value: ${type.name}): Unit = run { nioBuffer.put${type.bbMethodName}(index * ${type.size}, value) }")
        }
        println("}")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.nioBuffer: ByteBuffer get() = (baseBuffer as KmlBufferBase).nioBuffer")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.nioByteBuffer: ByteBuffer get() = (baseBuffer as KmlBufferBase).nioBuffer")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.nioShortBuffer: ShortBuffer get() = (baseBuffer as KmlBufferBase).shortBuffer")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.nioIntBuffer: IntBuffer get() = (baseBuffer as KmlBufferBase).intBuffer")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.nioFloatBuffer: FloatBuffer get() = (baseBuffer as KmlBufferBase).floatBuffer")
        println("")
    }

    fun Printer.generateJs() {
        println("package com.soywiz.kmedialayer")
        println("")
        println("import org.khronos.webgl.*")
        println("")
        println("private fun roundUp(n: Int, m: Int) = if (n >= 0) ((n + m - 1) / m) * m else (n / m) * m")
        println("")
        println("actual class KmlBufferBase private constructor(val arrayBuffer: ArrayBuffer) : KmlBuffer {")
        println("   val arrayByte = Int8Array(arrayBuffer)")
        println("   val arrayUByte = Uint8Array(arrayBuffer)")
        println("   val arrayShort = Int16Array(arrayBuffer)")
        println("   val arrayInt = Int32Array(arrayBuffer)")
        println("   val arrayFloat = Float32Array(arrayBuffer)")
        println("   actual override val baseBuffer: KmlBufferBase = this")
        println("   actual val size: Int = arrayBuffer.byteLength")
        println("   actual constructor(size: Int) : this(ArrayBuffer(roundUp(size, 8)))")
        println("")
        for (type in types) {
            println("    actual fun get${type.name}(index: Int): ${type.name} = array${type.name}[index]")
            println("    actual fun set${type.name}(index: Int, value: ${type.name}): Unit = run { array${type.name}[index] = value }")
        }
        println("}")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.arrayBuffer: ArrayBuffer get() = (baseBuffer as KmlBufferBase).arrayBuffer")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.arrayByte: Int8Array get() = (baseBuffer as KmlBufferBase).arrayByte")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.arrayUByte: Uint8Array get() = (baseBuffer as KmlBufferBase).arrayUByte")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.arrayShort: Int16Array get() = (baseBuffer as KmlBufferBase).arrayShort")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.arrayInt: Int32Array get() = (baseBuffer as KmlBufferBase).arrayInt")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.arrayFloat: Float32Array get() = (baseBuffer as KmlBufferBase).arrayFloat")
        println("")
    }
}