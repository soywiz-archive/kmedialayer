object KmlBufferGenerate {
    @JvmStatic
    fun main(args: Array<String>) {
        printToFile("kmedialayer/common/src/main/kotlin/com/soywiz/kmedialayer/KmlBuffer.kt") { generateCommon() }
        printToFile("kmedialayer/jvm/src/main/kotlin/com/soywiz/kmedialayer/KmlBufferJvm.kt") { generateJvm() }
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
        println("package com.soywiz.kmedialayer")
        println("")
        println("interface KmlWithBuffer { val buffer: KmlBuffer }")
        println("expect class KmlBuffer : KmlWithBuffer {")
        println("    override val buffer: KmlBuffer")
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
            println("class Kml${type.name}Buffer(override val buffer: KmlBuffer) : KmlWithBuffer, Iterable<${type.name}> {")
            println("    companion object { const val ELEMENT_SIZE = ${type.size} }")
            println("    val size: Int = buffer.size / ELEMENT_SIZE")
            println("    constructor(size: Int) : this(KmlBuffer(size * ELEMENT_SIZE))")
            println("    operator fun get(index: Int): ${type.name} = buffer.get${type.name}(index)")
            println("    operator fun set(index: Int, value: ${type.name}): Unit = run { buffer.set${type.name}(index, value) }")
            println("    override fun iterator(): Iterator<${type.name}> = object : Iterator<${type.name}> {")
            println("        var pos = 0")
            println("        override fun hasNext(): Boolean = pos < size")
            println("        override fun next(): ${type.name} = get(pos++)")
            println("    }")
            println("    override fun toString(): String = \"Kml${type.name}Buffer(\${this.toList()})\"")
            println("}")
            println("fun KmlWithBuffer.as${type.name}Buffer() = Kml${type.name}Buffer(this.buffer)")
            println("")
        }
    }

    fun Printer.generateJvm() {
        println("package com.soywiz.kmedialayer")
        println("")
        println("import java.nio.*")
        println("")
        println("actual class KmlBuffer private constructor(val nioBuffer: ByteBuffer) : KmlWithBuffer {")
        println("   actual override val buffer: KmlBuffer = this")
        println("   actual val size: Int = nioBuffer.limit()")
        println("   actual constructor(size: Int) : this(ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()))")
        println("")
        for (type in types) {
            println("    actual fun get${type.name}(index: Int): ${type.name} = nioBuffer.get${type.bbMethodName}(index * ${type.size})")
            println("    actual fun set${type.name}(index: Int, value: ${type.name}): Unit = run { nioBuffer.put${type.bbMethodName}(index * ${type.size}, value) }")
        }
        println("}")
        println("@Suppress(\"USELESS_CAST\") val KmlWithBuffer.nioBuffer: ByteBuffer get() = (buffer as KmlBuffer).nioBuffer")
        println("")
    }
}