package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import platform.GLUT.*
import platform.OpenGL.*
import platform.OpenGLCommon.*

private lateinit var globalListener: KMLWindowListener

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
        //glutSetOption(GLUT_ACTION_ON_WINDOW_CLOSE, GLUT_ACTION_GLUTMAINLOOP_RETURNS);
        glutInitWindowSize(config.width, config.height)
        glutInitWindowPosition(
            (glutGet(GLUT_SCREEN_WIDTH)-config.width)/2,
            (glutGet(GLUT_SCREEN_HEIGHT)-config.height)/2
        )

        // create Window
        glutCreateWindow(config.title)

        // register Display Function
        glutDisplayFunc(staticCFunction(::onDisplay))
        glutMouseFunc(staticCFunction(::onMouseButton))
        glutMotionFunc(staticCFunction(::onMouseMotion));
        glutPassiveMotionFunc(staticCFunction(::onMouseMotion));

        // register Idle Function
        glutIdleFunc(staticCFunction(::onDisplay))

        listener.init(KmlGl)

        // run GLUT mainloop
        glutMainLoop()
        //glutMainLoopEvent()
    }
}

private fun onDisplay() {
    globalListener.render(KmlGl)
    glutSwapBuffers()
}

private var mouseX = 0
private var mouseY = 0
private var mouseButtons = 0

private fun onMouseButton(button: Int, state: Int, x: Int, y: Int) {
    mouseX = x
    mouseY = y
    if (state == GLUT_DOWN) {
        mouseButtons = mouseButtons or (1 shl button)
    } else {
        mouseButtons = mouseButtons and (1 shl button).inv()
    }
    globalListener.mouseUpdate(mouseX, mouseY, mouseButtons)
}

private fun onMouseMotion(x: Int, y: Int) {
    mouseX = x
    mouseY = y
    globalListener.mouseUpdate(mouseX, mouseY, mouseButtons)
}
