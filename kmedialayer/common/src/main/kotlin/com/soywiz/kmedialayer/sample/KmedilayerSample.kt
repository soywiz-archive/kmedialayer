package com.soywiz.kmedialayer.sample

import com.soywiz.kmedialayer.*

object KmedilayerSample {
    fun main(args: Array<String>) {
        Kml.application(
            WindowConfig(
                width = 640,
                height = 480,
                title = "KmedilayerSample"
            ), object : KMLWindowListener() {
                lateinit var program: KmlGlProgram
                lateinit var layout: KmlGlVertexLayout
                lateinit var buffer: KmlGlBuffer

                override fun init(gl: KmlGl) = gl.run {
                    program = createProgram(
                        vertex = """
                            attribute vec2 aPos;
                            void main() {
                                gl_Position = vec4(aPos, 0.0, 1.0);
                            }
                        """,
                        fragment = """
                            void main(void) {
                                gl_FragColor = vec4(0.8, 0.3, 0.4, 1.0);
                            }
                        """
                    )
                    layout = program.layout {
                        float("aPos", 2)
                    }
                    buffer = createArrayBuffer()
                }

                var n = 0
                override fun render(gl: KmlGl) = gl.run {
                    buffer.setData(KmlFloatBuffer(floatArrayOf(
                        0f, 0f,
                        1f, 0f,
                        1f, .5f + (n.toFloat() * 0.001f)
                    )))
                    n++
                    clearColor(.5f, .55f, .6f, 1f)
                    clear(COLOR_BUFFER_BIT)
                    drawArrays(layout, buffer, TRIANGLES, 0, 3)
                }

                override fun keyUpdate(keyCode: Int, pressed: Boolean) {
                    println("keyUpdate($keyCode, $pressed)")
                }

                override fun gamepadUpdate(button: Int, pressed: Boolean, ratio: Double) {
                    println("gamepadUpdate($button, $pressed, $ratio)")
                }

                override fun mouseUpdate(x: Int, y: Int, buttons: Int) {
                    println("mouseUpdate($x, $y, $buttons)")
                }
            }
        )
    }
}