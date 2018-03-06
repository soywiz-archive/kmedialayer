package com.soywiz.kmedialayer


class KmlGlProgram(val gl: KmlGl, val program: Int, val vertex: Int, val fragment: Int) {
    fun use() = gl.useProgram(program)
    fun unuse() = gl.useProgram(0)
    fun getAttribLocation(name: String) = gl.getAttribLocation(program, name)
    fun getUniformLocation(name: String) = gl.getUniformLocation(program, name)
    fun dispose() {
        gl.deleteProgram(program)
        gl.deleteShader(vertex)
        gl.deleteShader(fragment)
    }

    inline fun use(callback: () -> Unit) {
        val oldProgram = gl.getIntegerv(gl.CURRENT_PROGRAM)
        gl.useProgram(program)
        try {
            callback()
        } finally {
            gl.useProgram(oldProgram)
        }
    }
}

private fun KmlGl.createShader(type: Int, source: String): Int {
    val shader = createShader(type)
    shaderSource(shader, source)
    compileShaderAndCheck(shader)
    return shader
}

// TODO: Release resources on failure
fun KmlGl.createProgram(vertex: String, fragment: String): KmlGlProgram {
    val program = createProgram()
    val shaderVertex = createShader(VERTEX_SHADER, vertex)
    val shaderFragment = createShader(FRAGMENT_SHADER, fragment)
    attachShader(program, shaderVertex)
    attachShader(program, shaderFragment)
    linkProgramAndCheck(program)
    return KmlGlProgram(this, program, shaderVertex, shaderFragment)
}

class KmlGlVertexLayout(val program: KmlGlProgram) {
    data class Element(val index: Int, val size: Int, val type: Int, val pointer: Int, val normalized: Boolean)

    val gl = program.gl
    private var index: Int = 0
    private var size: Int = 0
    private val elements = arrayListOf<Element>()

    private fun add(name: String, type: Int, esize: Int, count: Int, normalized: Boolean): KmlGlVertexLayout {
        elements += Element(program.getAttribLocation(name), count, type, size, normalized)
        size += count * esize
        index++
        return this
    }

    fun byte(name: String, count: Int, normalized: Boolean = false) = add(name, gl.BYTE, 1, count, normalized)
    fun ubyte(name: String, count: Int, normalized: Boolean = false) = add(name, gl.UNSIGNED_BYTE, 1, count, normalized)
    fun short(name: String, count: Int, normalized: Boolean = false) = add(name, gl.SHORT, 2, count, normalized)
    fun ushort(name: String, count: Int, normalized: Boolean = false) = add(name, gl.UNSIGNED_SHORT, 2, count, normalized)
    fun int(name: String, count: Int, normalized: Boolean = false) = add(name, gl.INT, 4, count, normalized)
    fun float(name: String, count: Int, normalized: Boolean = false) = add(name, gl.FLOAT, 4, count, normalized)

    fun enable(): Unit = gl.run {
        for ((index, element) in elements.withIndex()) {
            enableVertexAttribArray(index)
            vertexAttribPointer(
                element.index,
                element.size,
                element.type,
                element.normalized,
                size,
                element.pointer
            )
        }
    }

    fun disable(): Unit = gl.run {
        for ((index, _) in elements.withIndex()) {
            disableVertexAttribArray(index)
        }
    }

    inline fun use(callback: () -> Unit): Unit {
        program.use {
            enable()
            try {
                callback()
            } finally {
                disable()
            }
        }
    }
}

fun KmlGlProgram.layout(config: KmlGlVertexLayout.() -> Unit): KmlGlVertexLayout = KmlGlVertexLayout(this).apply(config)

class KmlGlBuffer(val gl: KmlGl, val type: Int, val bufs: KmlIntBuffer) {
    fun bind() {
        gl.bindBuffer(type, bufs[0])
    }

    fun unbind() {
        gl.bindBuffer(type, 0)
    }

    inline fun bind(callback: () -> Unit) {
        bind()
        try {
            callback()
        } finally {
            unbind()
        }
    }

    fun setData(data: KmlBuffer): KmlGlBuffer {
        bind()
        gl.bufferData(type, data.baseBuffer.size, data, gl.STATIC_DRAW)
        return this
    }

    fun dispose() {
        gl.deleteBuffers(bufs.size, bufs)
    }
}

fun KmlGl.createBuffer(type: Int): KmlGlBuffer {
    val bufs = KmlIntBuffer(1)
    genBuffers(1, bufs)
    return KmlGlBuffer(this, type, bufs)
}

fun KmlGl.createArrayBuffer(): KmlGlBuffer = createBuffer(ARRAY_BUFFER)
fun KmlGl.createElementArrayBuffer(): KmlGlBuffer = createBuffer(ELEMENT_ARRAY_BUFFER)

inline fun KmlGlVertexLayout.drawArrays(buffer: KmlGlBuffer, mode: Int, first: Int, count: Int, uniforms: () -> Unit = {}) {
    this.use {
        buffer.bind {
            uniforms()
            gl.drawArrays(mode, first, count)
        }
    }
}

class KmlGlTex(val gl: KmlGl, val texb: KmlIntBuffer) {
    val tex get() = texb[0]

    fun bind(unit: Int) = gl.run {
        activeTexture(TEXTURE0 + unit)
        bindTexture(TEXTURE_2D, tex)
        texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
        texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
        texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
        texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    }

    fun upload(width: Int, height: Int, data: KmlBuffer, format: Int = gl.RGBA, type: Int = gl.UNSIGNED_BYTE): KmlGlTex {
        bind(0)
        gl.texImage2D(gl.TEXTURE_2D, 0, format, width, height, 0, format, type, data)
        return this
    }

    fun dispose() {
        gl.deleteTextures(1, texb)
    }
}

fun KmlGl.createKmlTexture(): KmlGlTex {
    val buf = KmlIntBuffer(1)
    genTextures(1, buf)
    return KmlGlTex(this, buf)
}