package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*

class SceneTexture(val tex: KmlGlTex, val left: Float = 0f, val top: Float = 0f, val right: Float = 1f, val bottom: Float = 1f) {
    val width = right - left
    val height = bottom - top

    val widthPixels get() = tex.width * width
    val heightPixels get() = tex.height * height
}