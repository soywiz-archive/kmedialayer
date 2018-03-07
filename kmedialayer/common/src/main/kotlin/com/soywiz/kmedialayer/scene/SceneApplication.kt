package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*

fun SceneApplication(windowConfig: WindowConfig = WindowConfig(), sceneGen: () -> Scene) {
    Kml.application(windowConfig, object : KMLWindowListener() {
        lateinit var scene: Scene
        lateinit var renderContext: SceneRenderContext

        override suspend fun init(gl: KmlGl) = gl.run {
            renderContext = SceneRenderContext(SceneBatcher(gl))
            scene = sceneGen().apply { this.gl = gl; init() }
        }

        override fun render(gl: KmlGl) {
            super.render(gl)
            scene.render(renderContext)
            renderContext.flush()
        }

        override fun keyUpdate(keyCode: Int, pressed: Boolean) {
            if (pressed) {
                scene.onKeyDown(keyCode)
            } else {
                scene.onKeyUp(keyCode)
            }
        }

        override fun gamepadUpdate(button: Int, pressed: Boolean, ratio: Double) {
            super.gamepadUpdate(button, pressed, ratio)
        }

        override fun mouseUpdate(x: Int, y: Int, buttons: Int) {
            super.mouseUpdate(x, y, buttons)
        }

        override fun resized(width: Int, height: Int) {
            super.resized(width, height)
        }
    })
}
