package com.soywiz.kmedialayer

import org.w3c.dom.*
import org.w3c.dom.events.*
import kotlin.browser.*

actual object Kml {
    actual fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        document.title = windowConfig.title
        val canvas = (document.getElementById("kml-canvas") ?: ((document.createElement("canvas") as HTMLCanvasElement).apply {
            id = "kml-canvas"
            width = windowConfig.width
            height = windowConfig.height
            document.body!!.appendChild(this)
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
        fun frame(ms: Double) {
            window.requestAnimationFrame(::frame)
            listener.render(gl)
        }

        frame(0.0)
    }
}