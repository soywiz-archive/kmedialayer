object KmlGenGl {
    @JvmStatic
    fun main(args: Array<String>) {
        printToFile("kmedialayer/common/src/main/kotlin/com/soywiz/kmedialayer/KmlGl.kt") { generateCommon() }
        printToFile("kmedialayer/jvm/src/main/kotlin/com/soywiz/kmedialayer/KmlGlJvm.kt") { generateJvm() }
        printToFile("kmedialayer/js/src/main/kotlin/com/soywiz/kmedialayer/KmlGlJsCanvas.kt") { generateJs() }
        //printToConsole { generateCommon() }
        //printToConsole { generateJvm() }
    }

    fun Printer.generateCommon() {
        println("// WARNING: File autogenerated DO NOT modify")
        println("// https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h")
        println("@file:Suppress(\"unused\", \"RedundantUnitReturnType\")")
        println("")
        println("package com.soywiz.kmedialayer")
        println("")
        println("abstract class KmlGl {")
        //println("    companion object {")
        for (const in OpenglDesc.constants.values) {
            println("    val ${const.unprefixedName}: Int get() = ${"0x%04X".format(const.value)}")
        }
        //println("    }")
        println("")
        for (func in OpenglDesc.functions.values) {
            println("    abstract fun ${func.unprefixedName}(${func.args.joinToString(", ") { it.name + ": " + it.type.ktname }}): ${func.rettype.ktname}")
        }
        println("}")
        println("")
    }

    fun Printer.generateJvm() {
        println("// WARNING: File autogenerated DO NOT modify")
        println("// https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h")
        println("@file:Suppress(\"unused\", \"RedundantUnitReturnType\")")
        println("")
        println("package com.soywiz.kmedialayer")
        println("")
        println("import org.lwjgl.opengl.GL11.*")
        println("import org.lwjgl.opengl.GL13.*")
        println("import org.lwjgl.opengl.GL14.*")
        println("import org.lwjgl.opengl.GL15.*")
        println("import org.lwjgl.opengl.GL20.*")
        println("import org.lwjgl.opengl.GL30.*")
        println("import org.lwjgl.opengl.GL41.*")
        println("import java.nio.*")
        println("")
        println("class JvmKmlGl : KmlGl() {")
        for (func in OpenglDesc.functions.values) {
            val call = "${func.name}(${func.args.joinToString(", ") { it.type.toJVM(it.name) }})"
            println("    override fun ${func.unprefixedName}(${func.args.joinToString(", ") { it.name + ": " + it.type.ktname }}): ${func.rettype.ktname} = $call")
        }
        println("}")
        println("")
    }

    fun Printer.generateJs() {
        println("// WARNING: File autogenerated DO NOT modify")
        println("// https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h")
        println("@file:Suppress(\"unused\", \"RedundantUnitReturnType\")")
        println("")
        println("package com.soywiz.kmedialayer")
        println("")
        println("import org.w3c.dom.*")
        println("import org.khronos.webgl.*")
        println("")
        println("class KmlGlJsCanvas(val canvas: HTMLCanvasElement) : KmlGl() {")
        println("    val gl = canvas.getContext(\"webgl\") as WebGLRenderingContext")
        println("    private val items = arrayOfNulls<Any>(8 * 1024)")
        println("    private val freeList = (1 until items.size).reversed().toMutableList()")
        println("    private fun <T> T.alloc(): Int = run { val index = freeList.removeAt(freeList.size - 1); items[index] = this; index }")
        println("    private fun <T> Int.get(): T = items[this] as T")
        println("    private fun <T> Int.free(): T = run { val out = items[this] as T; freeList += this; items[this] = null; out }")
        println("")
        for (func in OpenglDesc.functions.values) {
            val call = func.jsBody ?: func.rettype.toJSReturn("gl.${func.jsName}(${func.args.joinToString(", ") { it.type.toJSParam(it.name) }})")
            println("    override fun ${func.unprefixedName}(${func.args.joinToString(", ") { it.name + ": " + it.type.ktname }}): ${func.rettype.ktname} = $call")
        }
        println("}")
        println("")
    }
}

object OpenglDesc {
    data class FunctionName(val name: String, val jsName: String = name)
    data class Constant(val name: String, val value: Int) {
        val unprefixedName = name.removePrefix("GL_")
    }

    data class Argument(val name: String, val type: GlType)
    data class Function(val fname: FunctionName, val rettype: GlType, val args: List<Argument>, val jsBody: String? = null) {
        val name = fname.name
        val unprefixedName = name.removePrefix("gl").decapitalize()
        val jsName get() = fname.jsName.removePrefix("gl").decapitalize()
    }

    val constants = LinkedHashMap<String, Constant>()
    val functions = LinkedHashMap<String, Function>()

    fun constant(name: String, value: Int) {
        constants[name] = Constant(name, value)
    }

    fun function(rettype: GlType, name: FunctionName, vararg args: Pair<String, GlType>, jsBody: String? = null) {
        functions[name.name] = Function(name, rettype, args.map { Argument(it.first, it.second) }.toList(), jsBody)
    }

    fun function(rettype: GlType, name: String, vararg args: Pair<String, GlType>, jsBody: String? = null) =
        function(rettype, FunctionName(name), *args, jsBody = jsBody)

    open class GlType(val ktname: String) {
        open fun toJVM(param: String): String = param
        open fun toJSParam(param: String): String = param
        open fun toJSReturn(param: String): String = param
    }

    object GlVoid : GlType("Unit")
    object GlInt : GlType("Int")
    object GlSize : GlType("Long")
    object GlFloat : GlType("Float")
    object GlBool : GlType("Boolean")
    object GlString : GlType("String")

    open class GlTypeToInt : GlType("Int") {
        override fun toJSParam(param: String): String = "$param.get()"
        override fun toJSReturn(param: String): String = "$param.alloc()"
    }

    object GlProgram : GlTypeToInt()
    object GlShader : GlTypeToInt()
    object GlLocation : GlTypeToInt()
    object GlBuffer : GlTypeToInt()
    object GlTexture : GlTypeToInt()

    open class GlTypePtr(ktname: String) : GlType(ktname) {
        override fun toJVM(param: String): String = "$param.nioBuffer"
    }

    object GlVoidPtr : GlTypePtr("KmlBuffer")
    object GlIntPtr : GlTypePtr("KmlBuffer")
    object GlCharPtr : GlTypePtr("KmlBuffer")
    object GlFloatPtr : GlTypePtr("KmlBuffer")
    object GlBoolPtr : GlTypePtr("KmlBuffer")

    init {
        constant("GL_DEPTH_BUFFER_BIT", 0x00000100)
        constant("GL_STENCIL_BUFFER_BIT", 0x00000400)
        constant("GL_COLOR_BUFFER_BIT", 0x00004000)
        constant("GL_FALSE", 0)
        constant("GL_TRUE", 1)
        constant("GL_POINTS", 0x0000)
        constant("GL_LINES", 0x0001)
        constant("GL_LINE_LOOP", 0x0002)
        constant("GL_LINE_STRIP", 0x0003)
        constant("GL_TRIANGLES", 0x0004)
        constant("GL_TRIANGLE_STRIP", 0x0005)
        constant("GL_TRIANGLE_FAN", 0x0006)
        constant("GL_ZERO", 0)
        constant("GL_ONE", 1)
        constant("GL_SRC_COLOR", 0x0300)
        constant("GL_ONE_MINUS_SRC_COLOR", 0x0301)
        constant("GL_SRC_ALPHA", 0x0302)
        constant("GL_ONE_MINUS_SRC_ALPHA", 0x0303)
        constant("GL_DST_ALPHA", 0x0304)
        constant("GL_ONE_MINUS_DST_ALPHA", 0x0305)
        constant("GL_DST_COLOR", 0x0306)
        constant("GL_ONE_MINUS_DST_COLOR", 0x0307)
        constant("GL_SRC_ALPHA_SATURATE", 0x0308)
        constant("GL_FUNC_ADD", 0x8006)
        constant("GL_BLEND_EQUATION", 0x8009)
        constant("GL_BLEND_EQUATION_RGB", 0x8009)
        constant("GL_BLEND_EQUATION_ALPHA", 0x883D)
        constant("GL_FUNC_SUBTRACT", 0x800A)
        constant("GL_FUNC_REVERSE_SUBTRACT", 0x800B)
        constant("GL_BLEND_DST_RGB", 0x80C8)
        constant("GL_BLEND_SRC_RGB", 0x80C9)
        constant("GL_BLEND_DST_ALPHA", 0x80CA)
        constant("GL_BLEND_SRC_ALPHA", 0x80CB)
        constant("GL_CONSTANT_COLOR", 0x8001)
        constant("GL_ONE_MINUS_CONSTANT_COLOR", 0x8002)
        constant("GL_CONSTANT_ALPHA", 0x8003)
        constant("GL_ONE_MINUS_CONSTANT_ALPHA", 0x8004)
        constant("GL_BLEND_COLOR", 0x8005)
        constant("GL_ARRAY_BUFFER", 0x8892)
        constant("GL_ELEMENT_ARRAY_BUFFER", 0x8893)
        constant("GL_ARRAY_BUFFER_BINDING", 0x8894)
        constant("GL_ELEMENT_ARRAY_BUFFER_BINDING", 0x8895)
        constant("GL_STREAM_DRAW", 0x88E0)
        constant("GL_STATIC_DRAW", 0x88E4)
        constant("GL_DYNAMIC_DRAW", 0x88E8)
        constant("GL_BUFFER_SIZE", 0x8764)
        constant("GL_BUFFER_USAGE", 0x8765)
        constant("GL_CURRENT_VERTEX_ATTRIB", 0x8626)
        constant("GL_FRONT", 0x0404)
        constant("GL_BACK", 0x0405)
        constant("GL_FRONT_AND_BACK", 0x0408)
        constant("GL_TEXTURE_2D", 0x0DE1)
        constant("GL_CULL_FACE", 0x0B44)
        constant("GL_BLEND", 0x0BE2)
        constant("GL_DITHER", 0x0BD0)
        constant("GL_STENCIL_TEST", 0x0B90)
        constant("GL_DEPTH_TEST", 0x0B71)
        constant("GL_SCISSOR_TEST", 0x0C11)
        constant("GL_POLYGON_OFFSET_FILL", 0x8037)
        constant("GL_SAMPLE_ALPHA_TO_COVERAGE", 0x809E)
        constant("GL_SAMPLE_COVERAGE", 0x80A0)
        constant("GL_NO_ERROR", 0)
        constant("GL_INVALID_ENUM", 0x0500)
        constant("GL_INVALID_VALUE", 0x0501)
        constant("GL_INVALID_OPERATION", 0x0502)
        constant("GL_OUT_OF_MEMORY", 0x0505)
        constant("GL_CW", 0x0900)
        constant("GL_CCW", 0x0901)
        constant("GL_LINE_WIDTH", 0x0B21)
        constant("GL_ALIASED_POINT_SIZE_RANGE", 0x846D)
        constant("GL_ALIASED_LINE_WIDTH_RANGE", 0x846E)
        constant("GL_CULL_FACE_MODE", 0x0B45)
        constant("GL_FRONT_FACE", 0x0B46)
        constant("GL_DEPTH_RANGE", 0x0B70)
        constant("GL_DEPTH_WRITEMASK", 0x0B72)
        constant("GL_DEPTH_CLEAR_VALUE", 0x0B73)
        constant("GL_DEPTH_FUNC", 0x0B74)
        constant("GL_STENCIL_CLEAR_VALUE", 0x0B91)
        constant("GL_STENCIL_FUNC", 0x0B92)
        constant("GL_STENCIL_FAIL", 0x0B94)
        constant("GL_STENCIL_PASS_DEPTH_FAIL", 0x0B95)
        constant("GL_STENCIL_PASS_DEPTH_PASS", 0x0B96)
        constant("GL_STENCIL_REF", 0x0B97)
        constant("GL_STENCIL_VALUE_MASK", 0x0B93)
        constant("GL_STENCIL_WRITEMASK", 0x0B98)
        constant("GL_STENCIL_BACK_FUNC", 0x8800)
        constant("GL_STENCIL_BACK_FAIL", 0x8801)
        constant("GL_STENCIL_BACK_PASS_DEPTH_FAIL", 0x8802)
        constant("GL_STENCIL_BACK_PASS_DEPTH_PASS", 0x8803)
        constant("GL_STENCIL_BACK_REF", 0x8CA3)
        constant("GL_STENCIL_BACK_VALUE_MASK", 0x8CA4)
        constant("GL_STENCIL_BACK_WRITEMASK", 0x8CA5)
        constant("GL_VIEWPORT", 0x0BA2)
        constant("GL_SCISSOR_BOX", 0x0C10)
        constant("GL_COLOR_CLEAR_VALUE", 0x0C22)
        constant("GL_COLOR_WRITEMASK", 0x0C23)
        constant("GL_UNPACK_ALIGNMENT", 0x0CF5)
        constant("GL_PACK_ALIGNMENT", 0x0D05)
        constant("GL_MAX_TEXTURE_SIZE", 0x0D33)
        constant("GL_MAX_VIEWPORT_DIMS", 0x0D3A)
        constant("GL_SUBPIXEL_BITS", 0x0D50)
        constant("GL_RED_BITS", 0x0D52)
        constant("GL_GREEN_BITS", 0x0D53)
        constant("GL_BLUE_BITS", 0x0D54)
        constant("GL_ALPHA_BITS", 0x0D55)
        constant("GL_DEPTH_BITS", 0x0D56)
        constant("GL_STENCIL_BITS", 0x0D57)
        constant("GL_POLYGON_OFFSET_UNITS", 0x2A00)
        constant("GL_POLYGON_OFFSET_FACTOR", 0x8038)
        constant("GL_TEXTURE_BINDING_2D", 0x8069)
        constant("GL_SAMPLE_BUFFERS", 0x80A8)
        constant("GL_SAMPLES", 0x80A9)
        constant("GL_SAMPLE_COVERAGE_VALUE", 0x80AA)
        constant("GL_SAMPLE_COVERAGE_INVERT", 0x80AB)
        constant("GL_NUM_COMPRESSED_TEXTURE_FORMATS", 0x86A2)
        constant("GL_COMPRESSED_TEXTURE_FORMATS", 0x86A3)
        constant("GL_DONT_CARE", 0x1100)
        constant("GL_FASTEST", 0x1101)
        constant("GL_NICEST", 0x1102)
        constant("GL_GENERATE_MIPMAP_HINT", 0x8192)
        constant("GL_BYTE", 0x1400)
        constant("GL_UNSIGNED_BYTE", 0x1401)
        constant("GL_SHORT", 0x1402)
        constant("GL_UNSIGNED_SHORT", 0x1403)
        constant("GL_INT", 0x1404)
        constant("GL_UNSIGNED_INT", 0x1405)
        constant("GL_FLOAT", 0x1406)
        constant("GL_FIXED", 0x140C)
        constant("GL_DEPTH_COMPONENT", 0x1902)
        constant("GL_ALPHA", 0x1906)
        constant("GL_RGB", 0x1907)
        constant("GL_RGBA", 0x1908)
        constant("GL_LUMINANCE", 0x1909)
        constant("GL_LUMINANCE_ALPHA", 0x190A)
        constant("GL_UNSIGNED_SHORT_4_4_4_4", 0x8033)
        constant("GL_UNSIGNED_SHORT_5_5_5_1", 0x8034)
        constant("GL_UNSIGNED_SHORT_5_6_5", 0x8363)
        constant("GL_FRAGMENT_SHADER", 0x8B30)
        constant("GL_VERTEX_SHADER", 0x8B31)
        constant("GL_MAX_VERTEX_ATTRIBS", 0x8869)
        constant("GL_MAX_VERTEX_UNIFORM_VECTORS", 0x8DFB)
        constant("GL_MAX_VARYING_VECTORS", 0x8DFC)
        constant("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS", 0x8B4D)
        constant("GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS", 0x8B4C)
        constant("GL_MAX_TEXTURE_IMAGE_UNITS", 0x8872)
        constant("GL_MAX_FRAGMENT_UNIFORM_VECTORS", 0x8DFD)
        constant("GL_SHADER_TYPE", 0x8B4F)
        constant("GL_DELETE_STATUS", 0x8B80)
        constant("GL_LINK_STATUS", 0x8B82)
        constant("GL_VALIDATE_STATUS", 0x8B83)
        constant("GL_ATTACHED_SHADERS", 0x8B85)
        constant("GL_ACTIVE_UNIFORMS", 0x8B86)
        constant("GL_ACTIVE_UNIFORM_MAX_LENGTH", 0x8B87)
        constant("GL_ACTIVE_ATTRIBUTES", 0x8B89)
        constant("GL_ACTIVE_ATTRIBUTE_MAX_LENGTH", 0x8B8A)
        constant("GL_SHADING_LANGUAGE_VERSION", 0x8B8C)
        constant("GL_CURRENT_PROGRAM", 0x8B8D)
        constant("GL_NEVER", 0x0200)
        constant("GL_LESS", 0x0201)
        constant("GL_EQUAL", 0x0202)
        constant("GL_LEQUAL", 0x0203)
        constant("GL_GREATER", 0x0204)
        constant("GL_NOTEQUAL", 0x0205)
        constant("GL_GEQUAL", 0x0206)
        constant("GL_ALWAYS", 0x0207)
        constant("GL_KEEP", 0x1E00)
        constant("GL_REPLACE", 0x1E01)
        constant("GL_INCR", 0x1E02)
        constant("GL_DECR", 0x1E03)
        constant("GL_INVERT", 0x150A)
        constant("GL_INCR_WRAP", 0x8507)
        constant("GL_DECR_WRAP", 0x8508)
        constant("GL_VENDOR", 0x1F00)
        constant("GL_RENDERER", 0x1F01)
        constant("GL_VERSION", 0x1F02)
        constant("GL_EXTENSIONS", 0x1F03)
        constant("GL_NEAREST", 0x2600)
        constant("GL_LINEAR", 0x2601)
        constant("GL_NEAREST_MIPMAP_NEAREST", 0x2700)
        constant("GL_LINEAR_MIPMAP_NEAREST", 0x2701)
        constant("GL_NEAREST_MIPMAP_LINEAR", 0x2702)
        constant("GL_LINEAR_MIPMAP_LINEAR", 0x2703)
        constant("GL_TEXTURE_MAG_FILTER", 0x2800)
        constant("GL_TEXTURE_MIN_FILTER", 0x2801)
        constant("GL_TEXTURE_WRAP_S", 0x2802)
        constant("GL_TEXTURE_WRAP_T", 0x2803)
        constant("GL_TEXTURE", 0x1702)
        constant("GL_TEXTURE_CUBE_MAP", 0x8513)
        constant("GL_TEXTURE_BINDING_CUBE_MAP", 0x8514)
        constant("GL_TEXTURE_CUBE_MAP_POSITIVE_X", 0x8515)
        constant("GL_TEXTURE_CUBE_MAP_NEGATIVE_X", 0x8516)
        constant("GL_TEXTURE_CUBE_MAP_POSITIVE_Y", 0x8517)
        constant("GL_TEXTURE_CUBE_MAP_NEGATIVE_Y", 0x8518)
        constant("GL_TEXTURE_CUBE_MAP_POSITIVE_Z", 0x8519)
        constant("GL_TEXTURE_CUBE_MAP_NEGATIVE_Z", 0x851A)
        constant("GL_MAX_CUBE_MAP_TEXTURE_SIZE", 0x851C)
        constant("GL_TEXTURE0", 0x84C0)
        constant("GL_TEXTURE1", 0x84C1)
        constant("GL_TEXTURE2", 0x84C2)
        constant("GL_TEXTURE3", 0x84C3)
        constant("GL_TEXTURE4", 0x84C4)
        constant("GL_TEXTURE5", 0x84C5)
        constant("GL_TEXTURE6", 0x84C6)
        constant("GL_TEXTURE7", 0x84C7)
        constant("GL_TEXTURE8", 0x84C8)
        constant("GL_TEXTURE9", 0x84C9)
        constant("GL_TEXTURE10", 0x84CA)
        constant("GL_TEXTURE11", 0x84CB)
        constant("GL_TEXTURE12", 0x84CC)
        constant("GL_TEXTURE13", 0x84CD)
        constant("GL_TEXTURE14", 0x84CE)
        constant("GL_TEXTURE15", 0x84CF)
        constant("GL_TEXTURE16", 0x84D0)
        constant("GL_TEXTURE17", 0x84D1)
        constant("GL_TEXTURE18", 0x84D2)
        constant("GL_TEXTURE19", 0x84D3)
        constant("GL_TEXTURE20", 0x84D4)
        constant("GL_TEXTURE21", 0x84D5)
        constant("GL_TEXTURE22", 0x84D6)
        constant("GL_TEXTURE23", 0x84D7)
        constant("GL_TEXTURE24", 0x84D8)
        constant("GL_TEXTURE25", 0x84D9)
        constant("GL_TEXTURE26", 0x84DA)
        constant("GL_TEXTURE27", 0x84DB)
        constant("GL_TEXTURE28", 0x84DC)
        constant("GL_TEXTURE29", 0x84DD)
        constant("GL_TEXTURE30", 0x84DE)
        constant("GL_TEXTURE31", 0x84DF)
        constant("GL_ACTIVE_TEXTURE", 0x84E0)
        constant("GL_REPEAT", 0x2901)
        constant("GL_CLAMP_TO_EDGE", 0x812F)
        constant("GL_MIRRORED_REPEAT", 0x8370)
        constant("GL_FLOAT_VEC2", 0x8B50)
        constant("GL_FLOAT_VEC3", 0x8B51)
        constant("GL_FLOAT_VEC4", 0x8B52)
        constant("GL_INT_VEC2", 0x8B53)
        constant("GL_INT_VEC3", 0x8B54)
        constant("GL_INT_VEC4", 0x8B55)
        constant("GL_BOOL", 0x8B56)
        constant("GL_BOOL_VEC2", 0x8B57)
        constant("GL_BOOL_VEC3", 0x8B58)
        constant("GL_BOOL_VEC4", 0x8B59)
        constant("GL_FLOAT_MAT2", 0x8B5A)
        constant("GL_FLOAT_MAT3", 0x8B5B)
        constant("GL_FLOAT_MAT4", 0x8B5C)
        constant("GL_SAMPLER_2D", 0x8B5E)
        constant("GL_SAMPLER_CUBE", 0x8B60)
        constant("GL_VERTEX_ATTRIB_ARRAY_ENABLED", 0x8622)
        constant("GL_VERTEX_ATTRIB_ARRAY_SIZE", 0x8623)
        constant("GL_VERTEX_ATTRIB_ARRAY_STRIDE", 0x8624)
        constant("GL_VERTEX_ATTRIB_ARRAY_TYPE", 0x8625)
        constant("GL_VERTEX_ATTRIB_ARRAY_NORMALIZED", 0x886A)
        constant("GL_VERTEX_ATTRIB_ARRAY_POINTER", 0x8645)
        constant("GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING", 0x889F)
        constant("GL_IMPLEMENTATION_COLOR_READ_TYPE", 0x8B9A)
        constant("GL_IMPLEMENTATION_COLOR_READ_FORMAT", 0x8B9B)
        constant("GL_COMPILE_STATUS", 0x8B81)
        constant("GL_INFO_LOG_LENGTH", 0x8B84)
        constant("GL_SHADER_SOURCE_LENGTH", 0x8B88)
        constant("GL_SHADER_COMPILER", 0x8DFA)
        constant("GL_SHADER_BINARY_FORMATS", 0x8DF8)
        constant("GL_NUM_SHADER_BINARY_FORMATS", 0x8DF9)
        constant("GL_LOW_FLOAT", 0x8DF0)
        constant("GL_MEDIUM_FLOAT", 0x8DF1)
        constant("GL_HIGH_FLOAT", 0x8DF2)
        constant("GL_LOW_INT", 0x8DF3)
        constant("GL_MEDIUM_INT", 0x8DF4)
        constant("GL_HIGH_INT", 0x8DF5)
        constant("GL_FRAMEBUFFER", 0x8D40)
        constant("GL_RENDERBUFFER", 0x8D41)
        constant("GL_RGBA4", 0x8056)
        constant("GL_RGB5_A1", 0x8057)
        constant("GL_RGB565", 0x8D62)
        constant("GL_DEPTH_COMPONENT16", 0x81A5)
        constant("GL_STENCIL_INDEX8", 0x8D48)
        constant("GL_RENDERBUFFER_WIDTH", 0x8D42)
        constant("GL_RENDERBUFFER_HEIGHT", 0x8D43)
        constant("GL_RENDERBUFFER_INTERNAL_FORMAT", 0x8D44)
        constant("GL_RENDERBUFFER_RED_SIZE", 0x8D50)
        constant("GL_RENDERBUFFER_GREEN_SIZE", 0x8D51)
        constant("GL_RENDERBUFFER_BLUE_SIZE", 0x8D52)
        constant("GL_RENDERBUFFER_ALPHA_SIZE", 0x8D53)
        constant("GL_RENDERBUFFER_DEPTH_SIZE", 0x8D54)
        constant("GL_RENDERBUFFER_STENCIL_SIZE", 0x8D55)
        constant("GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE", 0x8CD0)
        constant("GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME", 0x8CD1)
        constant("GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL", 0x8CD2)
        constant("GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE", 0x8CD3)
        constant("GL_COLOR_ATTACHMENT0", 0x8CE0)
        constant("GL_DEPTH_ATTACHMENT", 0x8D00)
        constant("GL_STENCIL_ATTACHMENT", 0x8D20)
        constant("GL_NONE", 0)
        constant("GL_FRAMEBUFFER_COMPLETE", 0x8CD5)
        constant("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT", 0x8CD6)
        constant("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT", 0x8CD7)
        constant("GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS", 0x8CD9)
        constant("GL_FRAMEBUFFER_UNSUPPORTED", 0x8CDD)
        constant("GL_FRAMEBUFFER_BINDING", 0x8CA6)
        constant("GL_RENDERBUFFER_BINDING", 0x8CA7)
        constant("GL_MAX_RENDERBUFFER_SIZE", 0x84E8)
        constant("GL_INVALID_FRAMEBUFFER_OPERATION", 0x0506)

        function(GlVoid, "glActiveTexture", "texture" to GlTexture)
        function(GlVoid, "glAttachShader", "program" to GlProgram, "shader" to GlShader)
        function(GlVoid, "glBindAttribLocation", "program" to GlProgram, "index" to GlInt, "name" to GlString)
        function(GlVoid, "glBindBuffer", "target" to GlInt, "buffer" to GlBuffer)
        function(GlVoid, "glBindFramebuffer", "target" to GlInt, "framebuffer" to GlInt)
        function(GlVoid, "glBindRenderbuffer", "target" to GlInt, "renderbuffer" to GlInt)
        function(GlVoid, "glBindTexture", "target" to GlInt, "texture" to GlTexture)
        function(GlVoid, "glBlendColor", "red" to GlFloat, "green" to GlFloat, "blue" to GlFloat, "alpha" to GlFloat)
        function(GlVoid, "glBlendEquation", "mode" to GlInt)
        function(GlVoid, "glBlendEquationSeparate", "modeRGB" to GlInt, "modeAlpha" to GlInt)
        function(GlVoid, "glBlendFunc", "sfactor" to GlInt, "dfactor" to GlInt)
        function(
            GlVoid,
            "glBlendFuncSeparate",
            "sfactorRGB" to GlInt,
            "dfactorRGB" to GlInt,
            "sfactorAlpha" to GlInt,
            "dfactorAlpha" to GlInt
        )
        function(GlVoid, "glBufferData", "target" to GlInt, "size" to GlSize, "data" to GlVoidPtr, "usage" to GlInt)
        function(
            GlVoid,
            "glBufferSubData",
            "target" to GlInt,
            "offset" to GlSize,
            "size" to GlSize,
            "data" to GlVoidPtr
        )
        function(GlInt, "glCheckFramebufferStatus", "target" to GlInt)
        function(GlVoid, "glClear", "mask" to GlInt)
        function(GlVoid, "glClearColor", "red" to GlFloat, "green" to GlFloat, "blue" to GlFloat, "alpha" to GlFloat)

        function(GlVoid, FunctionName("glClearDepthf", jsName = "glClearDepth"), "d" to GlFloat)

        function(GlVoid, "glClearStencil", "s" to GlInt)
        function(GlVoid, "glColorMask", "red" to GlBool, "green" to GlBool, "blue" to GlBool, "alpha" to GlBool)
        function(GlVoid, "glCompileShader", "shader" to GlShader)
        function(
            GlVoid,
            "glCompressedTexImage2D",
            "target" to GlInt,
            "level" to GlInt,
            "internalformat" to GlInt,
            "width" to GlInt,
            "height" to GlInt,
            "border" to GlInt,
            "imageSize" to GlInt,
            "data" to GlVoidPtr
        )

        function(
            GlVoid,
            "glCompressedTexSubImage2D",
            "target" to GlInt,
            "level" to GlInt,
            "xoffset" to GlInt,
            "yoffset" to GlInt,
            "width" to GlInt,
            "height" to GlInt,
            "format" to GlInt,
            "imageSize" to GlInt,
            "data" to GlVoidPtr
        )

        function(
            GlVoid,
            "glCopyTexImage2D",
            "target" to GlInt,
            "level" to GlInt,
            "internalformat" to GlInt,
            "x" to GlInt,
            "y" to GlInt,
            "width" to GlInt,
            "height" to GlInt,
            "border" to GlInt
        )

        function(
            GlVoid,
            "glCopyTexSubImage2D",
            "target" to GlInt,
            "level" to GlInt,
            "xoffset" to GlInt,
            "yoffset" to GlInt,
            "x" to GlInt,
            "y" to GlInt,
            "width" to GlInt,
            "height" to GlInt
        )

        function(GlProgram, "glCreateProgram")
        function(GlShader, "glCreateShader", "type" to GlInt)
        function(GlVoid, "glCullFace", "mode" to GlInt)
        function(GlVoid, "glDeleteBuffers", "n" to GlInt, "buffers" to GlIntPtr)
        function(GlVoid, "glDeleteFramebuffers", "n" to GlInt, "framebuffers" to GlIntPtr)
        function(GlVoid, "glDeleteProgram", "program" to GlProgram)
        function(GlVoid, "glDeleteRenderbuffers", "n" to GlInt, "renderbuffers" to GlIntPtr)
        function(GlVoid, "glDeleteShader", "shader" to GlShader)
        function(GlVoid, "glDeleteTextures", "n" to GlInt, "textures" to GlIntPtr)
        function(GlVoid, "glDepthFunc", "func" to GlInt)
        function(GlVoid, "glDepthMask", "flag" to GlBool)
        function(GlVoid, FunctionName("glDepthRangef", jsName = "glDepthRange"), "n" to GlFloat, "f" to GlFloat)
        function(GlVoid, "glDetachShader", "program" to GlProgram, "shader" to GlShader)
        function(GlVoid, "glDisable", "cap" to GlInt)
        function(GlVoid, "glDisableVertexAttribArray", "index" to GlInt)
        function(GlVoid, "glDrawArrays", "mode" to GlInt, "first" to GlInt, "count" to GlInt)
        function(GlVoid, "glDrawElements", "mode" to GlInt, "count" to GlInt, "type" to GlInt, "indices" to GlVoidPtr)
        function(GlVoid, "glEnable", "cap" to GlInt)
        function(GlVoid, "glEnableVertexAttribArray", "index" to GlInt)
        function(GlVoid, "glFinish")
        function(GlVoid, "glFlush")
        function(
            GlVoid,
            "glFramebufferRenderbuffer",
            "target" to GlInt,
            "attachment" to GlInt,
            "renderbuffertarget" to GlInt,
            "renderbuffer" to GlInt
        )
        function(
            GlVoid,
            "glFramebufferTexture2D",
            "target" to GlInt,
            "attachment" to GlInt,
            "textarget" to GlInt,
            "texture" to GlTexture,
            "level" to GlInt
        )
        function(GlVoid, "glFrontFace", "mode" to GlInt)
        function(GlVoid, "glGenBuffers", "n" to GlInt, "buffers" to GlIntPtr, jsBody = "run { for (p in 0 until n) buffers.arrayInt[p] = gl.createBuffer().alloc() }")
        function(GlVoid, "glGenerateMipmap", "target" to GlInt)
        function(GlVoid, "glGenFramebuffers", "n" to GlInt, "framebuffers" to GlIntPtr)
        function(GlVoid, "glGenRenderbuffers", "n" to GlInt, "renderbuffers" to GlIntPtr)
        function(GlVoid, "glGenTextures", "n" to GlInt, "textures" to GlIntPtr)
        function(
            GlVoid,
            "glGetActiveAttrib",
            "program" to GlProgram,
            "index" to GlInt,
            "bufSize" to GlInt,
            "length" to GlIntPtr,
            "size" to GlIntPtr,
            "type" to GlIntPtr,
            "name" to GlCharPtr
        )

        function(
            GlVoid,
            "glGetActiveUniform",
            "program" to GlProgram,
            "index" to GlInt,
            "bufSize" to GlInt,
            "length" to GlIntPtr,
            "size" to GlIntPtr,
            "type" to GlIntPtr,
            "name" to GlCharPtr
        )

        function(
            GlVoid,
            "glGetAttachedShaders",
            "program" to GlProgram,
            "maxCount" to GlInt,
            "count" to GlIntPtr,
            "shaders" to GlIntPtr
        )
        function(GlInt, "glGetAttribLocation", "program" to GlProgram, "name" to GlString)
        function(GlVoid, "glGetBooleanv", "pname" to GlInt, "data" to GlBoolPtr)
        function(GlVoid, "glGetBufferParameteriv", "target" to GlInt, "pname" to GlInt, "params" to GlIntPtr)
        function(GlInt, "glGetError")
        function(GlVoid, "glGetFloatv", "pname" to GlInt, "data" to GlFloatPtr)
        function(
            GlVoid,
            "glGetFramebufferAttachmentParameteriv",
            "target" to GlInt,
            "attachment" to GlInt,
            "pname" to GlInt,
            "params" to GlIntPtr
        )
        function(GlVoid, "glGetIntegerv", "pname" to GlInt, "data" to GlIntPtr)
        function(GlVoid, "glGetProgramiv", "program" to GlProgram, "pname" to GlInt, "params" to GlIntPtr)
        function(
            GlVoid,
            "glGetProgramInfoLog",
            "program" to GlProgram,
            "bufSize" to GlInt,
            "length" to GlIntPtr,
            "infoLog" to GlCharPtr
        )
        function(GlVoid, "glGetRenderbufferParameteriv", "target" to GlInt, "pname" to GlInt, "params" to GlIntPtr)
        function(GlVoid, "glGetShaderiv", "shader" to GlShader, "pname" to GlInt, "params" to GlIntPtr)
        function(
            GlVoid,
            "glGetShaderInfoLog",
            "shader" to GlShader,
            "bufSize" to GlInt,
            "length" to GlIntPtr,
            "infoLog" to GlCharPtr
        )
        function(
            GlVoid,
            "glGetShaderPrecisionFormat",
            "shadertype" to GlInt,
            "precisiontype" to GlInt,
            "range" to GlIntPtr,
            "precision" to GlIntPtr
        )
        function(
            GlVoid,
            "glGetShaderSource",
            "shader" to GlShader,
            "bufSize" to GlInt,
            "length" to GlIntPtr,
            "source" to GlCharPtr
        )
        function(GlString, "glGetString", "name" to GlInt, jsBody = "gl.getParameter(name) as String")
        function(GlVoid, "glGetTexParameterfv", "target" to GlInt, "pname" to GlInt, "params" to GlFloatPtr)
        function(GlVoid, "glGetTexParameteriv", "target" to GlInt, "pname" to GlInt, "params" to GlIntPtr)
        function(GlVoid, "glGetUniformfv", "program" to GlProgram, "location" to GlLocation, "params" to GlFloatPtr)
        function(GlVoid, "glGetUniformiv", "program" to GlProgram, "location" to GlLocation, "params" to GlIntPtr)
        function(GlInt, "glGetUniformLocation", "program" to GlProgram, "name" to GlString)
        function(GlVoid, "glGetVertexAttribfv", "index" to GlInt, "pname" to GlInt, "params" to GlFloatPtr)
        function(GlVoid, "glGetVertexAttribiv", "index" to GlInt, "pname" to GlInt, "params" to GlIntPtr)
        function(GlVoid, "glGetVertexAttribPointerv", "index" to GlInt, "pname" to GlInt, "pointer" to GlVoidPtr)
        function(GlVoid, "glHint", "target" to GlInt, "mode" to GlInt)
        function(GlBool, "glIsBuffer", "buffer" to GlBuffer)
        function(GlBool, "glIsEnabled", "cap" to GlInt)
        function(GlBool, "glIsFramebuffer", "framebuffer" to GlInt)
        function(GlBool, "glIsProgram", "program" to GlProgram)
        function(GlBool, "glIsRenderbuffer", "renderbuffer" to GlInt)
        function(GlBool, "glIsShader", "shader" to GlShader)
        function(GlBool, "glIsTexture", "texture" to GlTexture)
        function(GlVoid, "glLineWidth", "width" to GlFloat)
        function(GlVoid, "glLinkProgram", "program" to GlProgram)
        function(GlVoid, "glPixelStorei", "pname" to GlInt, "param" to GlInt)
        function(GlVoid, "glPolygonOffset", "factor" to GlFloat, "units" to GlFloat)
        function(
            GlVoid,
            "glReadPixels",
            "x" to GlInt,
            "y" to GlInt,
            "width" to GlInt,
            "height" to GlInt,
            "format" to GlInt,
            "type" to GlInt,
            "pixels" to GlVoidPtr
        )
        function(GlVoid, "glReleaseShaderCompiler")
        function(
            GlVoid,
            "glRenderbufferStorage",
            "target" to GlInt,
            "internalformat" to GlInt,
            "width" to GlInt,
            "height" to GlInt
        )
        function(GlVoid, "glSampleCoverage", "value" to GlFloat, "invert" to GlBool)
        function(GlVoid, "glScissor", "x" to GlInt, "y" to GlInt, "width" to GlInt, "height" to GlInt)
        function(
            GlVoid,
            "glShaderBinary",
            "count" to GlInt,
            "shaders" to GlIntPtr,
            "binaryformat" to GlInt,
            "binary" to GlVoidPtr,
            "length" to GlInt
        )

        // SPECIAL
        function(
            GlVoid,
            "glShaderSource",
            "shader" to GlShader,
            "string" to GlString
            //"count" to GlInt,
            //"string" to GlArrayString,
            //"length" to GlIntPtr
        )
        function(GlVoid, "glStencilFunc", "func" to GlInt, "ref" to GlInt, "mask" to GlInt)
        function(GlVoid, "glStencilFuncSeparate", "face" to GlInt, "func" to GlInt, "ref" to GlInt, "mask" to GlInt)
        function(GlVoid, "glStencilMask", "mask" to GlInt)
        function(GlVoid, "glStencilMaskSeparate", "face" to GlInt, "mask" to GlInt)
        function(GlVoid, "glStencilOp", "fail" to GlInt, "zfail" to GlInt, "zpass" to GlInt)
        function(GlVoid, "glStencilOpSeparate", "face" to GlInt, "sfail" to GlInt, "dpfail" to GlInt, "dppass" to GlInt)
        function(
            GlVoid,
            "glTexImage2D",
            "target" to GlInt,
            "level" to GlInt,
            "internalformat" to GlInt,
            "width" to GlInt,
            "height" to GlInt,
            "border" to GlInt,
            "format" to GlInt,
            "type" to GlInt,
            "pixels" to GlVoidPtr
        )

        function(GlVoid, "glTexParameterf", "target" to GlInt, "pname" to GlInt, "param" to GlFloat)
        function(GlVoid, "glTexParameterfv", "target" to GlInt, "pname" to GlInt, "params" to GlFloatPtr)
        function(GlVoid, "glTexParameteri", "target" to GlInt, "pname" to GlInt, "param" to GlInt)
        function(GlVoid, "glTexParameteriv", "target" to GlInt, "pname" to GlInt, "params" to GlIntPtr)
        function(
            GlVoid,
            "glTexSubImage2D",
            "target" to GlInt,
            "level" to GlInt,
            "xoffset" to GlInt,
            "yoffset" to GlInt,
            "width" to GlInt,
            "height" to GlInt,
            "format" to GlInt,
            "type" to GlInt,
            "pixels" to GlVoidPtr
        )

        function(GlVoid, "glUniform1f", "location" to GlLocation, "v0" to GlFloat)
        function(GlVoid, "glUniform1fv", "location" to GlLocation, "count" to GlInt, "value" to GlFloatPtr)
        function(GlVoid, "glUniform1i", "location" to GlLocation, "v0" to GlInt)
        function(GlVoid, "glUniform1iv", "location" to GlLocation, "count" to GlInt, "value" to GlIntPtr)
        function(GlVoid, "glUniform2f", "location" to GlLocation, "v0" to GlFloat, "v1" to GlFloat)
        function(GlVoid, "glUniform2fv", "location" to GlLocation, "count" to GlInt, "value" to GlFloatPtr)
        function(GlVoid, "glUniform2i", "location" to GlLocation, "v0" to GlInt, "v1" to GlInt)
        function(GlVoid, "glUniform2iv", "location" to GlLocation, "count" to GlInt, "value" to GlIntPtr)
        function(GlVoid, "glUniform3f", "location" to GlLocation, "v0" to GlFloat, "v1" to GlFloat, "v2" to GlFloat)
        function(GlVoid, "glUniform3fv", "location" to GlLocation, "count" to GlInt, "value" to GlFloatPtr)
        function(GlVoid, "glUniform3i", "location" to GlLocation, "v0" to GlInt, "v1" to GlInt, "v2" to GlInt)
        function(GlVoid, "glUniform3iv", "location" to GlLocation, "count" to GlInt, "value" to GlIntPtr)
        function(
            GlVoid,
            "glUniform4f",
            "location" to GlLocation,
            "v0" to GlFloat,
            "v1" to GlFloat,
            "v2" to GlFloat,
            "v3" to GlFloat
        )
        function(GlVoid, "glUniform4fv", "location" to GlLocation, "count" to GlInt, "value" to GlFloatPtr)
        function(GlVoid, "glUniform4i", "location" to GlLocation, "v0" to GlInt, "v1" to GlInt, "v2" to GlInt, "v3" to GlInt)
        function(GlVoid, "glUniform4iv", "location" to GlLocation, "count" to GlInt, "value" to GlIntPtr)
        function(
            GlVoid,
            "glUniformMatrix2fv",
            "location" to GlLocation,
            "count" to GlInt,
            "transpose" to GlBool,
            "value" to GlFloatPtr
        )
        function(
            GlVoid,
            "glUniformMatrix3fv",
            "location" to GlLocation,
            "count" to GlInt,
            "transpose" to GlBool,
            "value" to GlFloatPtr
        )
        function(
            GlVoid,
            "glUniformMatrix4fv",
            "location" to GlLocation,
            "count" to GlInt,
            "transpose" to GlBool,
            "value" to GlFloatPtr
        )
        function(GlVoid, "glUseProgram", "program" to GlProgram)
        function(GlVoid, "glValidateProgram", "program" to GlProgram)
        function(GlVoid, "glVertexAttrib1f", "index" to GlInt, "x" to GlFloat)
        function(GlVoid, "glVertexAttrib1fv", "index" to GlInt, "v" to GlFloatPtr)
        function(GlVoid, "glVertexAttrib2f", "index" to GlInt, "x" to GlFloat, "y" to GlFloat)
        function(GlVoid, "glVertexAttrib2fv", "index" to GlInt, "v" to GlFloatPtr)
        function(GlVoid, "glVertexAttrib3f", "index" to GlInt, "x" to GlFloat, "y" to GlFloat, "z" to GlFloat)
        function(GlVoid, "glVertexAttrib3fv", "index" to GlInt, "v" to GlFloatPtr)
        function(
            GlVoid,
            "glVertexAttrib4f",
            "index" to GlInt,
            "x" to GlFloat,
            "y" to GlFloat,
            "z" to GlFloat,
            "w" to GlFloat
        )
        function(GlVoid, "glVertexAttrib4fv", "index" to GlInt, "v" to GlFloatPtr)
        function(
            GlVoid,
            "glVertexAttribPointer",
            "index" to GlInt,
            "size" to GlInt,
            "type" to GlInt,
            "normalized" to GlBool,
            "stride" to GlInt,
            //"pointer" to GlVoidPtr
            "pointer" to GlSize
        )

        function(GlVoid, "glViewport", "x" to GlInt, "y" to GlInt, "width" to GlInt, "height" to GlInt)
    }
}