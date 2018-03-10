package com.soywiz.kmedialayer

import kotlin.coroutines.experimental.*

data class WindowConfig(
    val width: Int = 640,
    val height: Int = 480,
    val title: String = "KMediaLayer"
)

abstract class KmlBase {
    open fun launch(context: CoroutineContext = EmptyCoroutineContext, callback: suspend () -> Unit) {
        callback.startCoroutine(object : Continuation<Unit> {
            override val context: CoroutineContext = context
            override fun resume(value: Unit) = Unit
            override fun resumeWithException(exception: Throwable) {
                println(exception)
            }
        })
    }

    open fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        TODO("KmlBase.application()")
    }

    open fun currentTimeMillis(): Double = TODO("KmlBase.currentTimeMillis")
    open suspend fun delay(ms: Int): Unit = TODO("KmlBase.delay()")

    open fun enqueue(task: () -> Unit): Unit {
        TODO("KmlBase.delay()")
    }

    open suspend fun decodeImage(path: String): KmlNativeImageData {
        TODO("KmlBase.decodeImage(String)")
    }

    open suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        TODO("KmlBase.decodeImage(ByteArray)")
    }

    open suspend fun loadFileBytes(path: String, range: LongRange? = null): ByteArray {
        TODO("KmlBase.loadFileBytes")
    }

    open suspend fun writeFileBytes(path: String, data: ByteArray, offset: Long? = null): Unit {
        TODO("KmlBase.writeFileBytes")
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

    open fun keyUpdate(key: Key, pressed: Boolean) {
    }

    open fun gamepadUpdate(button: Int, pressed: Boolean, ratio: Double) {
    }

    open fun mouseUpdate(x: Int, y: Int, buttons: Int) {
    }

    open fun resized(width: Int, height: Int) {
    }
}

enum class Key {
    SPACE, APOSTROPHE, COMMA, MINUS, PERIOD, SLASH,
    N0, N1, N2, N3, N4, N5, N6, N7, N8, N9,
    SEMICOLON, EQUAL,
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    LEFT_BRACKET, BACKSLASH, RIGHT_BRACKET, GRAVE_ACCENT,
    WORLD_1, WORLD_2,
    ESCAPE,
    ENTER, TAB, BACKSPACE, INSERT, DELETE,
    RIGHT, LEFT, DOWN, UP,
    PAGE_UP, PAGE_DOWN,
    HOME, END,
    CAPS_LOCK, SCROLL_LOCK, NUM_LOCK,
    PRINT_SCREEN, PAUSE,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
    F13, F14, F15, F16, F17, F18, F19, F20, F21, F22, F23, F24, F25,
    KP_0, KP_1, KP_2, KP_3, KP_4, KP_5, KP_6, KP_7, KP_8, KP_9,
    KP_DECIMAL, KP_DIVIDE, KP_MULTIPLY,
    KP_SUBTRACT, KP_ADD, KP_ENTER, KP_EQUAL,
    LEFT_SHIFT, LEFT_CONTROL, LEFT_ALT, LEFT_SUPER,
    RIGHT_SHIFT, RIGHT_CONTROL, RIGHT_ALT, RIGHT_SUPER,
    MENU,

    UNDERLINE, SELECT_KEY,

    UNKNOWN
}

interface KmlNativeImageData {
    val width: Int
    val height: Int
}
