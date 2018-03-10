package com.soywiz.kmedialayer.scene.components

import com.soywiz.kmedialayer.scene.*
import com.soywiz.kmedialayer.scene.util.*

interface MouseEvents {
    val click: Signal<View>
    val out: Signal<View>
    val over: Signal<View>
}

private class MouseEventsComponent(override val view: View) : MouseComponent, UpdateComponent, MouseEvents {
    override val click = Signal<View>()
    override val out = Signal<View>()
    override val over = Signal<View>()

    var lastInside: Boolean? = null

    override fun onMouseMove(x: Int, y: Int) {
    }

    override fun onMouseUp(button: Int) {
    }

    override fun onMouseDown(button: Int) {
    }

    override fun onMouseClick(button: Int) {
        if (lastInside == true) {
            click(view)
        }
    }

    override fun update(ms: Double) {
        val mouseX = view.scene?.application?.mouse?.x ?: 0
        val mouseY = view.scene?.application?.mouse?.y ?: 0
        val nowInside = view.viewInGlobal(mouseX.toDouble(), mouseY.toDouble()) != null
        if (lastInside != nowInside) {
            this.lastInside = nowInside
            if (nowInside) {
                over(view)
            } else {
                out(view)
            }
        }
    }
}

operator fun MouseEvents.invoke(callback: MouseEvents.() -> Unit) = this.apply(callback)
val View.mouse get() = getOrCreateComponent { MouseEventsComponent(it) } as MouseEvents
