package com.soywiz.kmedialayer

object KmedilayerSample {
    fun main(args: Array<String>) {
        Kml.createWindow(
            WindowConfig(
                640,
                480
            ), object : KMLWindowListener() {
                override fun init(gl: KmlGl) = gl.run {
                    val shader1 = CreateShader(GL_VERTEX_SHADER)
                    println(shader1)
                }

                override fun render(gl: KmlGl) = gl.run {
                    ClearColor(1f, 1f, 0f, 1f)
                    Clear(GL_COLOR_BUFFER_BIT)
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