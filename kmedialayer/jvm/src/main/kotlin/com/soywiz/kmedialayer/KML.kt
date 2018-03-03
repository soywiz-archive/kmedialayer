package com.soywiz.kmedialayer

import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL.*
import org.lwjgl.system.MemoryUtil.*

actual object Kml {
    actual fun createWindow(config: WindowConfig, listener: KMLWindowListener) {
        // https://www.lwjgl.org/guide

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() == 0)
            throw IllegalStateException("Unable to initialize GLFW")

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        val window = glfwCreateWindow(config.width, config.height, config.title, NULL, NULL)
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)

        val capabilities = createCapabilities()

        glfwMakeContextCurrent(window)

        val gl = KmlGl()
        listener.init(gl)

        var mouseX = 0
        var mouseY = 0
        var mouseButtons = 0

        fun updatedMouse() {
            listener.mouseUpdate(mouseX, mouseY, mouseButtons)
        }

        glfwSetCursorPosCallback(window, object : GLFWCursorPosCallback() {
            override fun invoke(window: kotlin.Long, xpos: kotlin.Double, ypos: kotlin.Double) {
                mouseX = xpos.toInt()
                mouseY = ypos.toInt()
                updatedMouse()
            }
        })

        glfwSetMouseButtonCallback(window, object : GLFWMouseButtonCallback() {
            override fun invoke(window: kotlin.Long, button: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
                if (action == GLFW_PRESS) {
                    mouseButtons = mouseButtons or (1 shl button)
                } else {
                    mouseButtons = mouseButtons and (1 shl button).inv()
                }
                updatedMouse()
            }
        })

        while (glfwWindowShouldClose(window) == 0) {
            glfwMakeContextCurrent(window)

            listener.render(gl)

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
    }
}