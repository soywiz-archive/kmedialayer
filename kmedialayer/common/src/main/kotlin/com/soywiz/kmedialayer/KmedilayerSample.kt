package com.soywiz.kmedialayer

object KmedilayerSample {
    fun main(args: Array<String>) {
        Kml.createWindow(
            WindowConfig(
                640,
                480,
                title = "KmedilayerSample"
            ), object : KMLWindowListener() {
                override fun init(gl: KmlGl) = gl.run {
                    val program = createProgram()
                    val shaderVertex = createShader(GL_VERTEX_SHADER)
                    val shaderFragment = createShader(GL_FRAGMENT_SHADER)
                    shaderSource(shaderVertex, """
                        attribute vec2 aPos;
                        void main() {
                            gl_Position = vec4(aPos, 0.0, 1.0);
                        }
                    """)
                    shaderSource(shaderFragment, """
                        void main(void) {
                            gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
                        }
                    """)
                    compileShader(shaderFragment)
                    //compileShader(shaderVertexasaclass)
                    attachShader(program, shaderVertex)
                    attachShader(program, shaderFragment)
                    linkProgram(program)
                }

                override fun render(gl: KmlGl) = gl.run {
                    clearColor(.5f, .55f, .6f, 1f)
                    clear(GL_COLOR_BUFFER_BIT)
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