package com.soywiz.kmedialayer.scene.components

import com.soywiz.kmedialayer.scene.*
import com.soywiz.kmedialayer.scene.util.*

interface MouseEvents {
    fun click(handler: View.() -> Unit)
    fun out(handler: View.() -> Unit)
    fun over(handler: View.() -> Unit)
}

private class MouseEventsComponent(override val view: View) : ViewMouseComponent, ViewUpdateComponent, MouseEvents {
    val clickEvents = Signal<View>()
    val outEvents = Signal<View>()
    val overEvents = Signal<View>()

    override fun click(handler: View.() -> Unit) {
        clickEvents += handler
    }

    override fun out(handler: View.() -> Unit) {
        outEvents += handler
    }

    override fun over(handler: View.() -> Unit) {
        overEvents += handler
    }

    var lastInside: Boolean? = null

    override fun onMouseMove(x: Int, y: Int) {
    }

    override fun onMouseUp(button: Int) {
    }

    override fun onMouseDown(button: Int) {
    }

    override fun onMouseClick(button: Int) {
        if (lastInside == true) {
            clickEvents(view)
        }
    }

    override fun update(ms: Double) {
        val mouseX = view.scene?.application?.mouse?.x ?: 0
        val mouseY = view.scene?.application?.mouse?.y ?: 0
        val nowInside = view.viewInGlobal(mouseX.toDouble(), mouseY.toDouble()) != null
        if (lastInside != nowInside) {
            this.lastInside = nowInside
            if (nowInside) {
                overEvents(view)
            } else {
                outEvents(view)
            }
        }
    }
}

operator fun MouseEvents.invoke(callback: MouseEvents.() -> Unit) = this.apply(callback)
val View.mouse get() = getOrCreateComponent { MouseEventsComponent(it) } as MouseEvents