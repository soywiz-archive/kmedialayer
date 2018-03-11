package com.soywiz.kmedialayer.sample

import com.soywiz.kmedialayer.*
import com.soywiz.kmedialayer.scene.*
import com.soywiz.kmedialayer.scene.components.*
import com.soywiz.kmedialayer.scene.geom.*

object KMediaLayerSample2 {
    fun main(args: Array<String>) {
        SceneApplication {
            object : Scene() {
                lateinit var image: Image
                lateinit var container: ViewContainer

                val rotationQueue = JobQueue()
                val queue = JobQueue()

                override suspend fun init() {
                    //val data = Kml.loadFileBytes("mini.png")
                    //val tex = texture(Bitmap32(32, 32).apply {
                    //    for (x in 0 until 32) {
                    //        this[x, x] = 0xFF007000.toInt()
                    //        this[31 - x, x] = 0xFF007000.toInt()
                    //    }
                    //})
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
                            anchor(0.5, 0.5)
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
                                launch {
                                    parallel({
                                        moveBy(100.0, 100.0, easing = Easing.QUADRATIC_EASE_IN_OUT)
                                    }, {
                                        hide()
                                    })
                                    show()
                                }
                                Unit
                                //println("CLICKED!")
                            }
                            downOutside {
                                queue.cancel().queue {
                                    image.moveTo(mouse.x, mouse.y, time = 0.3, easing = Easing.QUADRATIC_EASE_IN_OUT)
                                }
                            }
                        }
                        val image = this["image"]!!
                        image.keys {
                            down(Key.ENTER) {
                                rotationQueue.queue {
                                    rotateBy(90.0, time = 0.3)
                                }
                            }
                            down(Key.UP) {
                                queue.cancelComplete().queue {
                                    moveBy(0.0, -32.0)
                                }
                            }
                            down(Key.DOWN) {
                                queue.cancelComplete().queue {
                                    moveBy(0.0, 32.0)
                                }
                            }
                            down(Key.LEFT) {
                                queue.cancelComplete().queue {
                                    moveBy(-32.0, 0.0)
                                }
                            }
                            down(Key.RIGHT) {
                                queue.cancelComplete().queue {
                                    moveBy(+32.0, 0.0)
                                }
                            }
                        }
                    }
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

