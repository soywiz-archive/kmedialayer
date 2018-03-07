package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*

class SceneBatcher(val gl: KmlGl) {
    val QUADS = 1024
    val vertices = KmlFloatBuffer(QUADS * 4 * 4)
    val indices = KmlShortBuffer(QUADS * 6)
    var vcount = 0
    var vpos = 0
    var ipos = 0
    private var currentTex: KmlGlTex? = null
    val ortho = KmlGlUtil.ortho(640, 480)
    val program = gl.createProgram(
        vertex = """
                            uniform mat4 uprojection;
                            attribute vec2 aPos;
                            attribute vec2 aTex;
                            varying vec2 vTex;
                            void main() {
                                gl_Position = uprojection * vec4(aPos, 0.0, 1.0);
                                vTex = aTex;
                            }
                        """,
        fragment = """
                            uniform sampler2D utex;
                            varying vec2 vTex;

                            void main(void) {
                                //gl_FragColor = vec4(0.8, 0.3, 0.4, 1.0);
                                //texture2D(utex, vTex);
                                gl_FragColor = texture2D(utex, vTex);
                            }
                        """
    )
    val layout = program.layout {
        float("aPos", 2)
        float("aTex", 2)
    }
    val vertexBuffer = gl.createArrayBuffer()
    val indexBuffer = gl.createElementArrayBuffer()

    private fun reset() {
        currentTex = null
        vcount = 0
        vpos = 0
        ipos = 0
    }

    private fun addVertex(x: Float, y: Float, tx: Float, ty: Float) {
        vcount++
        vertices[vpos++] = x
        vertices[vpos++] = y
        vertices[vpos++] = tx
        vertices[vpos++] = ty
    }

    private fun addIndex(index: Int) {
        indices[ipos++] = index.toShort()
    }

    fun addQuad(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, tex: SceneTexture) {
        if (currentTex != tex.tex) {
            flush()
        }
        currentTex = tex.tex
        val vstart = vcount
        addVertex(x0, y0, tex.left, tex.top)
        addVertex(x1, y1, tex.right, tex.top)
        addVertex(x2, y2, tex.left, tex.bottom)
        addVertex(x3, y3, tex.right, tex.bottom)
        addIndex(vstart + 0)
        addIndex(vstart + 1)
        addIndex(vstart + 2)
        addIndex(vstart + 1)
        addIndex(vstart + 2)
        addIndex(vstart + 3)
    }

    fun addQuad(left: Float, top: Float, right: Float, bottom: Float, tex: SceneTexture) {
        addQuad(
            left, top,
            right, top,
            left, bottom,
            right, bottom,
            tex
        )
    }

    fun flush() {
        if (ipos > 0) {
            renderBatch()
            reset()
        }
    }

    private fun renderBatch() {
        vertexBuffer.setData(vertices, vpos * 4)
        indexBuffer.setData(indices, ipos * 2)
        layout.drawElements(vertexBuffer, indexBuffer, gl.TRIANGLES, ipos, gl.UNSIGNED_SHORT) {
            currentTex?.let { uniformTex(program.getUniformLocation("utex"), it, unit = 0) }
            uniformMatrix4fv(program.getUniformLocation("uprojection"), 1, false, ortho)
        }
    }
}
