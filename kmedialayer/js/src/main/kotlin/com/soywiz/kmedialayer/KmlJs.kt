package com.soywiz.kmedialayer

import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.url.*
import org.w3c.files.*
import kotlin.browser.*
import kotlin.coroutines.experimental.*

fun launch(context: CoroutineContext = EmptyCoroutineContext, callback: suspend () -> Unit) {
    callback.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext = context
        override fun resume(value: Unit) = Unit
        override fun resumeWithException(exception: Throwable) = console.error(exception)
    })
}

actual val Kml: KmlBase = object : KmlBase() {
    override fun application(windowConfig: WindowConfig, listener: KMLWindowListener) = launch {
        document.title = windowConfig.title
        var mustAppendCanvas = false
        val canvas =
            (document.getElementById("kml-canvas") ?: ((document.createElement("canvas") as HTMLCanvasElement).apply {
                id = "kml-canvas"
                document.body?.style?.padding = "0"
                document.body?.style?.margin = "0"
                mustAppendCanvas = true
            })) as HTMLCanvasElement

        var mouseX = 0
        var mouseY = 0
        var mouseButtons = 0

        fun mouseUpdate(me: MouseEvent) {
            val pos = canvas.getBoundingClientRect()
            mouseX = me.clientX - pos.left.toInt()
            mouseY = me.clientY - pos.top.toInt()
            mouseButtons = me.buttons.toInt()
            listener.mouseUpdate(mouseX, mouseY, mouseButtons)
        }

        window.addEventListener("mousemove", { e: Event -> mouseUpdate(e as MouseEvent) })
        window.addEventListener("mousedown", { e: Event -> mouseUpdate(e as MouseEvent) })
        window.addEventListener("mouseup", { e: Event -> mouseUpdate(e as MouseEvent) })

        window.addEventListener("keydown", { e: Event ->
            listener.keyUpdate((e as KeyboardEvent).keyCode, true)
        })

        window.addEventListener("keyup", { e: Event ->
            listener.keyUpdate((e as KeyboardEvent).keyCode, false)
        })

        val gl = KmlGlJsCanvas(canvas)

        listener.init(gl)

        if (mustAppendCanvas) {
            fun resize() {
                val width = window.innerWidth
                val height = window.innerHeight
                canvas.width = width
                canvas.height = height
                canvas.style.width = "${width}px"
                canvas.style.height = "${height}px"
                gl.viewport(0, 0, width, height)
                listener.resized(width, height)
            }
            window.onresize = {
                resize()
            }
            resize()
            document.body?.appendChild(canvas)
        }

        fun frame(ms: Double) {
            window.requestAnimationFrame(::frame)
            listener.render(gl)
        }

        frame(0.0)
    }

    override suspend fun decodeImage(path: String): KmlNativeImageData = suspendCoroutine { c ->
        val image = document.createElement("img").unsafeCast<HTMLImageElement>()
        image.src = path
        image.onerror = { _, msg, _, _, _ ->
            c.resumeWithException(Exception("Error loading image: $msg"))
        }
        image.onload = {
            c.resume(KmlImgNativeImageData(image))
        }
    }

    override suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        val url = URL.createObjectURL(Blob(arrayOf(data.unsafeCast<Int8Array>())))
        try {
            return decodeImage(url)
        } finally {
            URL.revokeObjectURL(url)
        }
    }
}

class KmlImgNativeImageData(val img: HTMLImageElement) : KmlNativeImageData {
    override val width get() = img.width
    override val height get() = img.width
}
