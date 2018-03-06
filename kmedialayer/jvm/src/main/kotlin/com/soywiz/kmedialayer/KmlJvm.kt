package com.soywiz.kmedialayer

import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL.*
import org.lwjgl.system.MemoryUtil.*
import java.awt.image.*
import java.awt.image.BufferedImage
import java.io.*
import java.nio.*
import javax.imageio.*
import kotlin.coroutines.experimental.*


private fun <T : Any> runBlocking(context: CoroutineContext = EmptyCoroutineContext, callback: suspend () -> T): T {
    var done = false
    lateinit var resultValue: T
    var resultException: Throwable? = null
    callback.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = context
        override fun resume(value: T) {
            resultValue = value
            done = true
        }

        override fun resumeWithException(exception: Throwable) {
            exception.printStackTrace()
            resultException = exception
            done = true
        }
    })
    while (!done) {
        Thread.sleep(1L)
    }
    if (resultException != null) throw resultException!!
    return resultValue
}

actual val Kml = object : KmlBase() {
    lateinit var keyCallback: GLFWKeyCallback
    lateinit var cursorPosCallback: GLFWCursorPosCallback
    lateinit var mouseButtonCallback: GLFWMouseButtonCallback

    override fun application(windowConfig: WindowConfig, listener: KMLWindowListener) = runBlocking {
        System.setProperty("java.awt.headless", "true")
        // https://www.lwjgl.org/guide

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() == 0) throw IllegalStateException("Unable to initialize GLFW")

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        val window = glfwCreateWindow(windowConfig.width, windowConfig.height, windowConfig.title, NULL, NULL)
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)

        val capabilities = createCapabilities()

        glfwMakeContextCurrent(window)

        val gl = JvmKmlGl()
        listener.init(gl)

        var mouseX = 0
        var mouseY = 0
        var mouseButtons = 0

        fun updatedMouse() {
            listener.mouseUpdate(mouseX, mouseY, mouseButtons)
        }

        keyCallback = object : GLFWKeyCallback() {
            override fun invoke(
                window: kotlin.Long,
                key: kotlin.Int,
                scancode: kotlin.Int,
                action: kotlin.Int,
                mods: kotlin.Int
            ) {
                if (action != GLFW_REPEAT) { // Ignore repeat events
                    listener.keyUpdate(key, action != GLFW_RELEASE)
                }
            }
        }
        glfwSetKeyCallback(window, keyCallback)

        cursorPosCallback = object : GLFWCursorPosCallback() {
            override fun invoke(window: kotlin.Long, xpos: kotlin.Double, ypos: kotlin.Double) {
                mouseX = xpos.toInt()
                mouseY = ypos.toInt()
                updatedMouse()
            }
        }
        glfwSetCursorPosCallback(window, cursorPosCallback)

        mouseButtonCallback = object : GLFWMouseButtonCallback() {
            override fun invoke(window: kotlin.Long, button: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
                if (action == GLFW_PRESS) {
                    mouseButtons = mouseButtons or (1 shl button)
                } else {
                    mouseButtons = mouseButtons and (1 shl button).inv()
                }
                updatedMouse()
            }
        }
        glfwSetMouseButtonCallback(window, mouseButtonCallback)

        while (glfwWindowShouldClose(window) == 0) {
            glfwMakeContextCurrent(window)

            listener.render(gl)

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
    }

    override suspend fun decodeImage(path: String): KmlNativeImageData {
        return decodeImage(File(path).readBytes())
    }

    override suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        val img = ImageIO.read(ByteArrayInputStream(data))
        val out = BufferedImage(img.width, img.height, BufferedImage.TYPE_4BYTE_ABGR).apply {
            graphics.apply {
                drawImage(img, 0, 0, null)
                dispose()
            }
        }
        return BufferedImageKmlNativeImageData(out)
    }
}

class BufferedImageKmlNativeImageData(val buffered: BufferedImage) : KmlNativeImageData {
    override val width: Int get() = buffered.width
    override val height: Int get() = buffered.height
    val bytes = (buffered.raster.dataBuffer as DataBufferByte).data
    init {
        // FLIP R-A
        for (n in 0 until bytes.size step 4) {
            val r = bytes[n + 0]
            val b = bytes[n + 2]
            bytes[n + 0] = b
            bytes[n + 2] = r
        }
    }
    val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
        clear()
        put(bytes)
        //println("BYTES: ${bytes.size}")
        flip()
    }
}