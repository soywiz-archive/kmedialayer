package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import platform.GLUT.*
import platform.OpenGL.*
import platform.OpenGLCommon.*

lateinit var globalListener: KMLWindowListener

actual object Kml {
    actual fun createWindow(config: WindowConfig, listener: KMLWindowListener) {
        globalListener = listener

        memScoped {
            val argc = alloc<IntVar>().apply { value = 0 }
            glutInit(argc.ptr, null) // TODO: pass real args
        }

        // Display Mode
        glutInitDisplayMode(GLUT_RGB or GLUT_DOUBLE or GLUT_DEPTH)

        // Set window size
        glutInitWindowSize(config.width, config.height)
        glutInitWindowPosition(
            (glutGet(GLUT_SCREEN_WIDTH)-config.width)/2,
            (glutGet(GLUT_SCREEN_HEIGHT)-config.height)/2
        )

        // create Window
        glutCreateWindow(config.title)

        // register Display Function
        glutDisplayFunc(staticCFunction(::display))

        // register Idle Function
        glutIdleFunc(staticCFunction(::display))

        listener.init(KmlGl)

        // run GLUT mainloop
        glutMainLoop()
    }
}

fun display() {
    globalListener.render(KmlGl)
    glutSwapBuffers()
}
