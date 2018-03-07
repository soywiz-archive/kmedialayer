package com.soywiz.kmedialayer.scene.geom

object Interpolator {
    fun interpolate(ratio: Double, src: Double, dst: Double): Double {
        return src + (dst - src) * ratio
    }
}