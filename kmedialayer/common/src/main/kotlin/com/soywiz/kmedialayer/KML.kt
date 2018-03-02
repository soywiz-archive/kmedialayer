package com.soywiz.kmedialayer

external fun kmlCreateWindow(width: Int, height: Int, listener: KMLWindowListener)

open class KMLWindowListener {
    open fun render() {
        glClearColor(1f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)
    }

    open fun keyUpdate(keyCode: Int, pressed: Boolean) {
    }

    open fun gamepadUpdate(button: Int, pressed: Boolean, ratio: Double) {
    }
}