package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*
import kotlin.math.*

class SceneApplication(v: Boolean, val windowConfig: WindowConfig = WindowConfig(), val sceneGen: () -> Scene) :
    KMLWindowListener() {
    lateinit var scene: Scene
    lateinit var renderContext: SceneRenderContext

    override suspend fun init(gl: KmlGl) = gl.run {
        renderContext = SceneRenderContext(SceneBatcher(gl, windowConfig.width, windowConfig.height))
        scene = sceneGen().apply { this.gl = gl; this.application = this@SceneApplication; init() }
        scene.apply { updateScene(0) }
        Unit
    }

    private var lastTime = 0.0
    override fun render(gl: KmlGl) {
        if (lastTime == 0.0) lastTime = Kml.currentTimeMillis()
        val now = Kml.currentTimeMillis()
        if (now != lastTime) {
            scene.apply {
                updateScene((now - lastTime).toInt())
            }

            lastTime = now
        }

        super.render(gl)
        scene.render(renderContext)
        renderContext.flush()
    }

    override fun keyUpdate(key: Key, pressed: Boolean) {
        if (pressed) {
            scene.onKeyDown(key)
        } else {
            scene.onKeyUp(key)
        }
    }

    override fun gamepadUpdate(button: Int, pressed: Boolean, ratio: Double) {
        super.gamepadUpdate(button, pressed, ratio)
    }

    class Mouse {
        var x: Int = -1000; internal set
        var y: Int = -1000; internal set
        val buttons = (0 until 4).map { Button(it) }

        class Button(val id: Int) {
            var downX: Int = 0
            var downY: Int = 0
            var downTime: Double = 0.0; internal set

            var upX: Int = 0
            var upY: Int = 0
            var upTime: Double = 0.0; internal set

            var pressed: Boolean = false; internal set
        }

        fun button(index: Int) = buttons.getOrNull(index)
        fun pressing(button: Int) = button(button)?.pressed ?: false
    }

    val mouse = Mouse()


    override fun mouseUpdateMove(x: Int, y: Int) {
        scene.apply {
            mouse.x = x
            mouse.y = y
            mouseMoved(x, y)
        }
    }

    override fun mouseUpdateButton(button: Int, pressed: Boolean) {
        scene.apply {
            mouse.button(button)?.let { button ->
                val now = Kml.currentTimeMillis()
                val changed = button.pressed != pressed
                button.pressed = pressed
                if (pressed) {
                    button.downX = mouse.x
                    button.downY = mouse.y
                    button.downTime = now
                } else {
                    button.upX = mouse.x
                    button.upY = mouse.y
                    button.upTime = now
                }
                if (changed) {
                    if (pressed) {
                        //println("DOWN")
                        mouseDown(button.id)
                    } else {
                        //println("UP")
                        val elapsed = now - button.downTime
                        val movedX = mouse.x - button.downX
                        val movedY = mouse.y - button.downY
                        mouseUp(button.id)
                        //println("$elapsed, $movedX, $movedY")
                        if (elapsed <= 250.0 && (movedX < 16) && (movedY < 16)) {
                            //println("CLICK!")
                            mouseClick(button.id)
                        }
                    }
                }
            }
        }
    }

    override fun resized(width: Int, height: Int) {
        super.resized(width, height)
        KmlGlUtil.ortho(width, height, 0f, 1f, renderContext.batcher.ortho)
    }
}

fun SceneApplication(windowConfig: WindowConfig = WindowConfig(), sceneGen: () -> Scene) {
    SceneScope.apply {
        Kml.application(windowConfig, SceneApplication(true, windowConfig, sceneGen))
    }
}
