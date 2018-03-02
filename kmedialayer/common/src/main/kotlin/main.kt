import com.soywiz.kmedialayer.*

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
        }
    )
}
