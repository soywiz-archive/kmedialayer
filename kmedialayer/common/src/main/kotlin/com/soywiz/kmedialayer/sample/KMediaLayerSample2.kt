package com.soywiz.kmedialayer.sample

import com.soywiz.kmedialayer.scene.*

object KMediaLayerSample2 {
    fun main(args: Array<String>) {
        SceneApplication {
            object : Scene() {
                lateinit var image: Image
                lateinit var container: ViewContainer

                override suspend fun init() {
                    val tex = texture("mini.png")
                    root += ViewContainer().apply {
                        container = this
                        scaleX = 2.0
                        scaleY = 2.0
                        this += Image(tex).apply {
                            image = this
                            x = 10.0
                            y = 10.0
                        }
                        this += Image(tex).apply {
                            x = 100.0
                            y = 10.0
                        }
                    }
                }

                override fun onKeyDown(keyCode: Int) {
                    image.x += 10f
                    container.rotationDegrees += 2.0
                }
            }
        }
    }
}

