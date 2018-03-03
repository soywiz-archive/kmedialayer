package com.soywiz.kmedialayer

import org.w3c.dom.*
import kotlin.browser.*

actual object Kml {
    actual fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        document.title = windowConfig.title
        val canvas = (document.getElementById("kml-canvas") ?: ((document.createElement("canvas") as HTMLCanvasElement).apply {
            id = "kml-canvas"
            width = windowConfig.width
            height = windowConfig.height
            document.appendChild(this)
        })) as HTMLCanvasElement
        val gl = KmlGlJsCanvas(canvas)
    }
}