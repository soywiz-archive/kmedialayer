object KmlGenBuffer {
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
        println("   actual override val baseBuffer: KmlBufferBase = this")
        println("   actual val size: Int = nioBuffer.limit()")
        println("   actual constructor(size: Int) : this(ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()))")
        println("")
        for (type in types) {
            println("    actual fun get${type.name}(index: Int): ${type.name} = nioBuffer.get${type.bbMethodName}(index * ${type.size})")
            println("    actual fun set${type.name}(index: Int, value: ${type.name}): Unit = run { nioBuffer.put${type.bbMethodName}(index * ${type.size}, value) }")
        }
        println("}")
        println("@Suppress(\"USELESS_CAST\") val KmlBuffer.nioBuffer: ByteBuffer get() = (baseBuffer as KmlBufferBase).nioBuffer")
        println("")
    }
}