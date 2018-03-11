package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import platform.GLUT.*
import platform.OpenGL.*
import platform.OpenGLCommon.*
import platform.posix.*

private lateinit var globalListener: KMLWindowListener
private val glNative: KmlGlNative by lazy { KmlGlNative() }

object KmlBaseNative : KmlBaseNoEventLoop() {
    override fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        globalListener = listener

        memScoped {
            val argc = alloc<IntVar>().apply { value = 0 }
            glutInit(argc.ptr, null) // TODO: pass real args
        }

        // Display Mode
        glutInitDisplayMode(GLUT_RGB or GLUT_DOUBLE or GLUT_DEPTH)

        // Set window size
        //glutSetOption(GLUT_ACTION_ON_WINDOW_CLOSE, GLUT_ACTION_GLUTMAINLOOP_RETURNS);
        glutInitWindowSize(windowConfig.width, windowConfig.height)
        glutInitWindowPosition(
            (glutGet(GLUT_SCREEN_WIDTH)-windowConfig.width)/2,
            (glutGet(GLUT_SCREEN_HEIGHT)-windowConfig.height)/2
        )

        // create Window
        glutCreateWindow(windowConfig.title)

        // register Display Function
        glutDisplayFunc(staticCFunction(::onDisplay))
        glutMouseFunc(staticCFunction(::onMouseButton))
        glutMotionFunc(staticCFunction(::onMouseMotion));
        glutPassiveMotionFunc(staticCFunction(::onMouseMotion));

        // register Idle Function
        glutIdleFunc(staticCFunction(::onDisplay))

        runBlocking {
            listener.init(glNative)
        }

        // run GLUT mainloop
        glutMainLoop()
        //glutMainLoopEvent()
    }

    override fun currentTimeMillis(): Double = kotlin.system.getTimeMillis().toDouble()

    override fun sleep(time: Int) {
        usleep(time * 1000)
    }

    override fun pollEvents() {
    }

    override suspend fun decodeImage(path: String): KmlNativeImageData {
        TODO("KmlBase.decodeImage(String)")
    }

    override suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        TODO("KmlBase.decodeImage(ByteArray)")
    }

    override suspend fun loadFileBytes(path: String, range: LongRange?): ByteArray {
        TODO("KmlBase.loadFileBytes")
    }

    override suspend fun writeFileBytes(path: String, data: ByteArray, offset: Long?): Unit {
        TODO("KmlBase.writeFileBytes")
    }
}

actual val Kml: KmlBase = KmlBaseNative

private fun onDisplay() {
    globalListener.render(glNative)
    glutSwapBuffers()
}

private fun onMouseButton(button: Int, state: Int, x: Int, y: Int) {
    globalListener.mouseUpdateButton(button, state == GLUT_DOWN)
}

private fun onMouseMotion(x: Int, y: Int) {
    globalListener.mouseUpdateMove(x, y)
}

class KmlNativeNativeImageData(override val width: Int, override val height: Int, val data: KmlBuffer) : KmlNativeImageData {

}
