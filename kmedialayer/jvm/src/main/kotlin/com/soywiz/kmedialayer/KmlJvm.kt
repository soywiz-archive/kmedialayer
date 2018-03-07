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

object KmlBaseJvm : KmlBase() {
    lateinit var keyCallback: GLFWKeyCallback
    lateinit var cursorPosCallback: GLFWCursorPosCallback
    lateinit var mouseButtonCallback: GLFWMouseButtonCallback
    lateinit var frameBufferSize: GLFWFramebufferSizeCallback
    var window: Long = 0L

    override fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        System.setProperty("java.awt.headless", "true")
        // https://www.lwjgl.org/guide

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() == 0) throw IllegalStateException("Unable to initialize GLFW")

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        window = glfwCreateWindow(windowConfig.width, windowConfig.height, windowConfig.title, NULL, NULL)
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)

        val capabilities = createCapabilities()

        glfwMakeContextCurrent(window)

        val gl = JvmKmlGl()
        runBlocking {
            listener.init(gl)
        }

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

        fun render() {
            gl.startFrame()
            listener.render(gl)
            gl.endFrame()
        }

        frameBufferSize = object : GLFWFramebufferSizeCallback() {
            override fun invoke(window: kotlin.Long, width: kotlin.Int, height: kotlin.Int) {
                glfwMakeContextCurrent(window)
                gl.viewport(0, 0, width, height)
                listener.resized(width, height)
                render()
                glfwSwapBuffers(window) // swap the color buffers
            }
        }
        glfwSetFramebufferSizeCallback(window, frameBufferSize)
        //glfwSetWindowIcon()

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
            render()
            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()

            Timers.check()
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

    override suspend fun delay(ms: Int): Unit = suspendCoroutine { c ->
        Timers.add(ms) { c.resume(Unit) }
    }

    override fun currentTimeMillis(): Double = System.currentTimeMillis().toDouble()
}

actual val Kml: KmlBase = KmlBaseJvm


object Timers {
    private class Timer(val start: Long, val callback: () -> Unit)

    private val tempTimers = arrayListOf<Timer>()
    private val timers = arrayListOf<Timer>()

    fun add(ms: Int, callback: () -> Unit) {
        timers += Timer(System.currentTimeMillis() + ms, callback)
    }

    fun check() {
        // Timer events
        val now = System.currentTimeMillis()
        tempTimers.clear()
        tempTimers.addAll(timers)
        for (timer in tempTimers) {
            if (now >= timer.start) {
                timer.callback()
                timers.remove(timer)
            }
        }
    }
}

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
        //Timers.check()
        glfwPollEvents()
        Timers.check()
        if (glfwWindowShouldClose(KmlBaseJvm.window) != 0) {
            System.exit(0)
        }

    }
    if (resultException != null) throw resultException!!
    return resultValue
}

class BufferedImageKmlNativeImageData(val buffered: BufferedImage) : KmlNativeImageData {
    override val width: Int get() = buffered.width
    override val height: Int get() = buffered.height
    val bytes = (buffered.raster.dataBuffer as DataBufferByte).data
    init {
        //for (y in 0 until 32) {
        //    val rowSize = 32 * 4
        //    val index = y * rowSize
        //    println(bytes.sliceArray(index + 32 until index + 64).toList())
        //}

        for (n in 0 until bytes.size step 4) {
            val v0 = bytes[n + 0]
            val v1 = bytes[n + 1]
            val v2 = bytes[n + 2]
            val v3 = bytes[n + 3]
            bytes[n + 0] = v3
            bytes[n + 1] = v2
            bytes[n + 2] = v1
            bytes[n + 3] = v0
        }
    }
    val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
        clear()
        put(bytes)
        //println("BYTES: ${bytes.size}")
        flip()
    }
}