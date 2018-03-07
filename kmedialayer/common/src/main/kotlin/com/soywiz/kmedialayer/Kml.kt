package com.soywiz.kmedialayer

import kotlin.coroutines.experimental.*

data class WindowConfig(
    val width: Int = 640,
    val height: Int = 480,
    val title: String = "KMediaLayer"
)

abstract class KmlBase {
    open fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        TODO("KmlBase.application()")
    }

    open fun launch(context: CoroutineContext = EmptyCoroutineContext, callback: suspend () -> Unit) {
        callback.startCoroutine(object : Continuation<Unit> {
            override val context: CoroutineContext = context
            override fun resume(value: Unit) = Unit
            override fun resumeWithException(exception: Throwable) {
                println(exception)
            }
        })
    }

    open fun currentTimeMillis(): Double = 0.0

    open suspend fun delay(ms: Int): Unit {
        TODO("KmlBase.delay()")
    }

    open suspend fun decodeImage(path: String): KmlNativeImageData {
        TODO("KmlBase.decodeImage(String)")
    }

    open suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        TODO("KmlBase.decodeImage(ByteArray)")
    }
}

expect val Kml: KmlBase

open class KMLWindowListener {
    open suspend fun init(gl: KmlGl): Unit = gl.run {
    }

    open fun render(gl: KmlGl): Unit = gl.run {
        clearColor(1f, 0f, 1f, 1f)
        clear(COLOR_BUFFER_BIT)
    }

    open fun keyUpdate(keyCode: Int, pressed: Boolean) {
    }

    open fun gamepadUpdate(button: Int, pressed: Boolean, ratio: Double) {
    }

    open fun mouseUpdate(x: Int, y: Int, buttons: Int) {
    }

    open fun resized(width: Int, height: Int) {
    }
}

interface KmlNativeImageData {
    val width: Int
    val height: Int
}
