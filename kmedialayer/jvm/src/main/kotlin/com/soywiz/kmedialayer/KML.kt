package com.soywiz.kmedialayer

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

        listener.init(KmlGl)

        while (glfwWindowShouldClose(window) == 0) {
            glfwMakeContextCurrent(window)

            listener.render(KmlGl)

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
    }
}