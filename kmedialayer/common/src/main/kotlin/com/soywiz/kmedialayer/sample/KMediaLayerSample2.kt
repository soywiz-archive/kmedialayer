package com.soywiz.kmedialayer.sample

import com.soywiz.kmedialayer.*
import com.soywiz.kmedialayer.scene.*
import com.soywiz.kmedialayer.scene.components.*

object KMediaLayerSample2 {
    fun main(args: Array<String>) {
        SceneApplication {
            object : Scene() {
                lateinit var image: Image
                lateinit var container: ViewContainer

                override suspend fun init() {
                    //val data = Kml.loadFileBytes("mini.png")
                    val tex = texture("mini.png")
                    //val tex = texture(data)
                    //println(data.size)
                    root += ViewContainer().apply {
                        container = this
                        scaleX = 2.0
                        scaleY = 2.0
                        this += Image(tex).apply {
                            name = "image"
                            image = this
                            x = 10.0
                            y = 10.0
                        }
                        this += Image(tex).apply {
                            x = 100.0
                            y = 10.0
                        }

                        mouse {
                            over {
                                //this["image"]?.alpha = 1.0
                                alpha = 1.0
                            }
                            out {
                                //this["image"]?.alpha = 0.5
                                alpha = 0.5
                            }
                            click {
                                println("CLICKED!")
                            }
                        }
                    }
                }

                override fun onKeyDown(key: Key) {
                    println("key: $key")
                    when (key) {
                        Key.RIGHT -> {
                            image.act {
                                moveBy(32.0, 0.0)
                                //repeat(2) {
                                //    moveBy(0.0, 10.0)
                                //}
                                moveBy(0.0, 32.0)
                                moveBy(0.0, 32.0)
                                parallel {
                                    show()
                                    moveBy(32.0, 32.0)
                                }
                            }
                            container.rotationDegrees += 2.0
                        }
                    }
                    //println(keyCode)
                }

                /*
                var mouseX: Double = 0.0
                var mouseY: Double = 0.0

                override fun onMouseMove(x: Int, y: Int) {
                    mouseX = x.toDouble()
                    mouseY = y.toDouble()
                }

                override fun onUpdate(ms: Int) {
                    val x = mouseX
                    val y = mouseY
                    //println("$x,$y")
                    if (image.viewInGlobal(x, y) != null) {
                        image.alpha = 0.5
                    } else {
                        image.alpha = 1.0
                    }
                    //println(root.viewInGlobal(x, y))
                }
                */
            }
        }
    }
}

