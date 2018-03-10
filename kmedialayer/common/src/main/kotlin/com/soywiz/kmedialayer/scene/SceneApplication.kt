package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*

fun SceneApplication(windowConfig: WindowConfig = WindowConfig(), sceneGen: () -> Scene) {
    Kml.application(windowConfig, object : KMLWindowListener() {
        lateinit var scene: Scene
        lateinit var renderContext: SceneRenderContext

        override suspend fun init(gl: KmlGl) = gl.run {
            renderContext = SceneRenderContext(SceneBatcher(gl, windowConfig.width, windowConfig.height))
            scene = sceneGen().apply { this.gl = gl; init() }
        }

        private var lastTime = 0.0
        override fun render(gl: KmlGl) {
            if (lastTime == 0.0) lastTime = Kml.currentTimeMillis()
            val now = Kml.currentTimeMillis()
            if (now != lastTime) {
                scene.updateScene((now - lastTime).toInt())
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

        private var lastMouseX: Int = -1000
        private var lastMouseY: Int = -1000
        override fun mouseUpdate(x: Int, y: Int, buttons: Int) {
            super.mouseUpdate(x, y, buttons)
            if (lastMouseX != x || lastMouseY != y) {
                lastMouseX = x
                lastMouseY = y
                scene.onMouseMove(x, y)
            }
        }

        override fun resized(width: Int, height: Int) {
            super.resized(width, height)
            KmlGlUtil.ortho(width, height, 0f, 1f, renderContext.batcher.ortho)
        }
    })
}
