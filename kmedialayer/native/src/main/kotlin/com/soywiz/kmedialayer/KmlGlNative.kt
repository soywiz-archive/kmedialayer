package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import platform.GLUT.*
import platform.OpenGL.*
import platform.OpenGLCommon.*

private fun Boolean.toInt() = if (this) 1 else 0

actual object KmlGl {
    actual fun ActiveTexture(texture: Int): Unit = TODO()
    actual fun AttachShader(program: Int, shader: Int): Unit = TODO()
    actual fun BindAttribLocation(program: Int, index: Int, name: String): Unit = TODO()
    actual fun BindBuffer(target: Int, buffer: Int): Unit = TODO()
    actual fun BindFramebuffer(target: Int, framebuffer: Int): Unit = TODO()
    actual fun BindRenderbuffer(target: Int, renderbuffer: Int): Unit = TODO()
    actual fun BindTexture(target: Int, texture: Int): Unit = TODO()
    actual fun BlendColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = TODO()
    actual fun BlendEquation(mode: Int): Unit = TODO()
    actual fun BlendEquationSeparate(modeRGB: Int, modeAlpha: Int): Unit = TODO()
    actual fun BlendFunc(sfactor: Int, dfactor: Int): Unit = TODO()
    actual fun BlendFuncSeparate(sfactorRGB: Int, dfactorRGB: Int, sfactorAlpha: Int, dfactorAlpha: Int): Unit = TODO()
    actual fun BufferData(target: Int, size: KmlIntPtr, data: KmlVoidPtr, usage: Int): Unit = TODO()
    actual fun BufferSubData(target: Int, offset: KmlIntPtr, size: KmlIntPtr, data: KmlVoidPtr): Unit = TODO()
    actual fun CheckFramebufferStatus(target: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun Clear(mask: Int) = glClear(mask)
    actual fun ClearColor(red: Float, green: Float, blue: Float, alpha: Float) =
        glClearColor(red, green, blue, alpha)

    actual fun ClearDepthf(d: Float) = glClearDepth(d.toDouble())
    actual fun ClearStencil(s: Int) = glClearStencil(s)
    actual fun ColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) =
        glColorMask(red.toInt().narrow(), green.toInt().narrow(), blue.toInt().narrow(), alpha.toInt().narrow())

    actual fun CompileShader(shader: Int) = glCompileShader(shader)
    actual fun CompressedTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        imageSize: Int,
        data: KmlVoidPtr
    ) {
    }

    actual fun CompressedTexSubImage2D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        width: Int,
        height: Int,
        format: Int,
        imageSize: Int,
        data: KmlVoidPtr
    ) {
    }

    actual fun CopyTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        border: Int
    ) {
    }

    actual fun CopyTexSubImage2D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
    }

    actual fun CreateProgram(): Int = glCreateProgram()
    actual fun CreateShader(type: Int): Int = glCreateShader(type)

    actual fun CullFace(mode: Int) = glCullFace(mode)
    actual fun DeleteBuffers(n: Int, buffers: KmlIntPtr): Unit = TODO()
    actual fun DeleteFramebuffers(n: Int, framebuffers: KmlIntPtr): Unit = TODO()
    actual fun DeleteProgram(program: Int): Unit = TODO()
    actual fun DeleteRenderbuffers(n: Int, renderbuffers: KmlIntPtr): Unit = TODO()
    actual fun DeleteShader(shader: Int): Unit = TODO()
    actual fun DeleteTextures(n: Int, textures: KmlIntPtr): Unit = TODO()
    actual fun DepthFunc(func: Int): Unit = TODO()
    actual fun DepthMask(flag: Boolean): Unit = TODO()
    actual fun DepthRangef(n: Float, f: Float): Unit = TODO()
    actual fun DetachShader(program: Int, shader: Int): Unit = TODO()
    actual fun Disable(cap: Int): Unit = TODO()
    actual fun DisableVertexAttribArray(index: Int): Unit = TODO()
    actual fun DrawArrays(mode: Int, first: Int, count: Int): Unit = TODO()
    actual fun DrawElements(mode: Int, count: Int, type: Int, indices: KmlVoidPtr): Unit = TODO()
    actual fun Enable(cap: Int): Unit = TODO()
    actual fun EnableVertexAttribArray(index: Int): Unit = TODO()
    actual fun Finish(): Unit = TODO()
    actual fun Flush(): Unit = TODO()
    actual fun FramebufferRenderbuffer(
        target: Int,
        attachment: Int,
        renderbuffertarget: Int,
        renderbuffer: Int
    ) {
    }

    actual fun FramebufferTexture2D(
        target: Int,
        attachment: Int,
        textarget: Int,
        texture: Int,
        level: Int
    ) {
    }

    actual fun FrontFace(mode: Int): Unit = TODO()
    actual fun GenBuffers(n: Int, buffers: KmlIntPtr): Unit = TODO()
    actual fun GenerateMipmap(target: Int): Unit = TODO()
    actual fun GenFramebuffers(n: Int, framebuffers: KmlIntPtr): Unit = TODO()
    actual fun GenRenderbuffers(n: Int, renderbuffers: KmlIntPtr): Unit = TODO()
    actual fun GenTextures(n: Int, textures: KmlIntPtr): Unit = TODO()
    actual fun GetActiveAttrib(
        program: Int,
        index: Int,
        bufSize: Int,
        length: KmlIntPtr,
        size: KmlIntPtr,
        type: KmlIntPtr,
        name: String
    ) {
    }

    actual fun GetActiveUniform(
        program: Int,
        index: Int,
        bufSize: Int,
        length: KmlIntPtr,
        size: KmlIntPtr,
        type: KmlIntPtr,
        name: String
    ) {
    }

    actual fun GetAttachedShaders(program: Int, maxCount: Int, count: KmlIntPtr, shaders: KmlIntPtr): Unit = TODO()
    actual fun GetAttribLocation(program: Int, name: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun GetBooleanv(pname: Int, data: KmlBoolPtr): Unit = TODO()
    actual fun GetBufferParameteriv(target: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetError(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun GetFloatv(pname: Int, data: KmlFloatPtr): Unit = TODO()
    actual fun GetFramebufferAttachmentParameteriv(
        target: Int,
        attachment: Int,
        pname: Int,
        params: KmlIntPtr
    ) {
    }

    actual fun GetIntegerv(pname: Int, data: KmlIntPtr): Unit = TODO()
    actual fun GetProgramiv(program: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetProgramInfoLog(program: Int, bufSize: Int, length: KmlIntPtr, infoLog: KmlCharPtr): Unit = TODO()
    actual fun GetRenderbufferParameteriv(target: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetShaderiv(shader: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetShaderInfoLog(shader: Int, bufSize: Int, length: KmlIntPtr, infoLog: KmlCharPtr): Unit = TODO()
    actual fun GetShaderPrecisionFormat(
        shadertype: Int,
        precisiontype: Int,
        range: KmlIntPtr,
        precision: KmlIntPtr
    ) {
    }

    actual fun GetShaderSource(shader: Int, bufSize: Int, length: KmlIntPtr, source: KmlCharPtr): Unit = TODO()
    actual fun GetString(name: Int): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun GetTexParameterfv(target: Int, pname: Int, params: KmlFloatPtr): Unit = TODO()
    actual fun GetTexParameteriv(target: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetUniformfv(program: Int, location: Int, params: KmlFloatPtr): Unit = TODO()
    actual fun GetUniformiv(program: Int, location: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetUniformLocation(program: Int, name: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun GetVertexAttribfv(index: Int, pname: Int, params: KmlFloatPtr): Unit = TODO()
    actual fun GetVertexAttribiv(index: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun GetVertexAttribPointerv(index: Int, pname: Int, pointer: KmlVoidPtrPtr): Unit = TODO()
    actual fun Hint(target: Int, mode: Int): Unit = TODO()
    actual fun IsBuffer(buffer: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun IsEnabled(cap: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun IsFramebuffer(framebuffer: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun IsProgram(program: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun IsRenderbuffer(renderbuffer: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun IsShader(shader: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun IsTexture(texture: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun LineWidth(width: Float): Unit = TODO()
    actual fun LinkProgram(program: Int): Unit = TODO()
    actual fun PixelStorei(pname: Int, param: Int): Unit = TODO()
    actual fun PolygonOffset(factor: Float, units: Float): Unit = TODO()
    actual fun ReadPixels(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        pixels: KmlVoidPtr
    ) {
    }

    actual fun ReleaseShaderCompiler(): Unit = TODO()
    actual fun RenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int): Unit = TODO()
    actual fun SampleCoverage(value: Float, invert: Boolean): Unit = TODO()
    actual fun Scissor(x: Int, y: Int, width: Int, height: Int): Unit = TODO()
    actual fun ShaderBinary(count: Int, shaders: KmlIntPtr, binaryformat: Int, binary: KmlVoidPtr, length: Int): Unit = TODO()
    actual fun ShaderSource(shader: Int, count: Int, string: Array<String>, length: KmlIntPtr): Unit = TODO()
    actual fun StencilFunc(func: Int, ref: Int, mask: Int): Unit = TODO()
    actual fun StencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int): Unit = TODO()
    actual fun StencilMask(mask: Int): Unit = TODO()
    actual fun StencilMaskSeparate(face: Int, mask: Int): Unit = TODO()
    actual fun StencilOp(fail: Int, zfail: Int, zpass: Int): Unit = TODO()
    actual fun StencilOpSeparate(face: Int, sfail: Int, dpfail: Int, dppass: Int): Unit = TODO()
    actual fun TexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: KmlVoidPtr
    ) {
    }

    actual fun TexParameterf(target: Int, pname: Int, param: Float): Unit = TODO()
    actual fun TexParameterfv(target: Int, pname: Int, params: KmlFloatPtr): Unit = TODO()
    actual fun TexParameteri(target: Int, pname: Int, param: Int): Unit = TODO()
    actual fun TexParameteriv(target: Int, pname: Int, params: KmlIntPtr): Unit = TODO()
    actual fun TexSubImage2D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        pixels: KmlVoidPtr
    ) {
    }

    actual fun Uniform1f(location: Int, v0: Float): Unit = TODO()
    actual fun Uniform1fv(location: Int, count: Int, value: KmlFloatPtr): Unit = TODO()
    actual fun Uniform1i(location: Int, v0: Int): Unit = TODO()
    actual fun Uniform1iv(location: Int, count: Int, value: KmlIntPtr): Unit = TODO()
    actual fun Uniform2f(location: Int, v0: Float, v1: Float): Unit = TODO()
    actual fun Uniform2fv(location: Int, count: Int, value: KmlFloatPtr): Unit = TODO()
    actual fun Uniform2i(location: Int, v0: Int, v1: Int): Unit = TODO()
    actual fun Uniform2iv(location: Int, count: Int, value: KmlIntPtr): Unit = TODO()
    actual fun Uniform3f(location: Int, v0: Float, v1: Float, v2: Float): Unit = TODO()
    actual fun Uniform3fv(location: Int, count: Int, value: KmlFloatPtr): Unit = TODO()
    actual fun Uniform3i(location: Int, v0: Int, v1: Int, v2: Int): Unit = TODO()
    actual fun Uniform3iv(location: Int, count: Int, value: KmlIntPtr): Unit = TODO()
    actual fun Uniform4f(location: Int, v0: Float, v1: Float, v2: Float, v3: Float): Unit = TODO()
    actual fun Uniform4fv(location: Int, count: Int, value: KmlFloatPtr): Unit = TODO()
    actual fun Uniform4i(location: Int, v0: Int, v1: Int, v2: Int, v3: Int): Unit = TODO()
    actual fun Uniform4iv(location: Int, count: Int, value: KmlIntPtr): Unit = TODO()
    actual fun UniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: KmlFloatPtr): Unit = TODO()
    actual fun UniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: KmlFloatPtr): Unit = TODO()
    actual fun UniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: KmlFloatPtr): Unit = TODO()
    actual fun UseProgram(program: Int): Unit = TODO()
    actual fun ValidateProgram(program: Int): Unit = TODO()
    actual fun VertexAttrib1f(index: Int, x: Float): Unit = TODO()
    actual fun VertexAttrib1fv(index: Int, v: KmlFloatPtr): Unit = TODO()
    actual fun VertexAttrib2f(index: Int, x: Float, y: Float): Unit = TODO()
    actual fun VertexAttrib2fv(index: Int, v: KmlFloatPtr): Unit = TODO()
    actual fun VertexAttrib3f(index: Int, x: Float, y: Float, z: Float): Unit = TODO()
    actual fun VertexAttrib3fv(index: Int, v: KmlFloatPtr): Unit = TODO()
    actual fun VertexAttrib4f(index: Int, x: Float, y: Float, z: Float, w: Float): Unit = TODO()
    actual fun VertexAttrib4fv(index: Int, v: KmlFloatPtr): Unit = TODO()
    actual fun VertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: KmlVoidPtr
    ) {
    }

    actual fun Viewport(x: Int, y: Int, width: Int, height: Int): Unit = TODO()
}
