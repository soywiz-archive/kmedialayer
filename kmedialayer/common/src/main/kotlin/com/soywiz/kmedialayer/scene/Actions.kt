package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.scene.geom.*
import kotlin.math.*

class ViewActionActionRunner(val runner: ViewActionRunner) {
    var elapsedTime = 0.0
    val totalTime = runner.time * 1000.0

    fun update(time: Double) {
        if (elapsedTime == 0.0) {
            runner.start()
        }
        elapsedTime += time
        if (elapsedTime >= totalTime) {
            runner.update(1.0)
            runner.view.actions = null
        } else {
            runner.update(elapsedTime / totalTime)
        }
    }

    fun finish() {
        runner.update(1.0)
    }
}

abstract class ViewActionRunner(val view: View, val action: ViewAction) {
    val time get() = action.time
    abstract fun update(ratio: Double)
    open fun start(): Unit = Unit
    open fun finish(): Unit = update(1.0)
}

interface ViewAction {
    val time: Double
    fun createRunner(view: View): ViewActionRunner

    class MoveBy(val x: Double, val y: Double, override val time: Double) : ViewAction {
        override fun createRunner(view: View): ViewActionRunner = object : ViewActionRunner(view, this) {
            var sx = 0.0
            var sy = 0.0
            var dx = 0.0
            var dy = 0.0

            override fun start() {
                sx = view.x
                sy = view.y
                dx = view.x + x
                dy = view.y + y
                println("START ($sx, $sy) -> ($dx, $dy)")
            }

            override fun update(ratio: Double) {
                view.x = Interpolator.interpolate(ratio, sx, dx)
                view.y = Interpolator.interpolate(ratio, sy, dy)
            }
        }
    }

    class SetAlpha(val dst: Double, override val time: Double) : ViewAction {
        override fun createRunner(view: View): ViewActionRunner = object : ViewActionRunner(view, this) {
            var src = 0.0

            override fun start() {
                src = view.alpha
            }

            override fun update(ratio: Double) {
                view.alpha = Interpolator.interpolate(ratio, src, dst)
            }
        }
    }

    class AList(val actions: List<ViewAction>) : ViewAction {
        override val time: Double = actions.sumByDouble { it.time }
        override fun createRunner(view: View): ViewActionRunner = object : ViewActionRunner(view, this) {
            val tasks = actions.map { it.createRunner(view) }
            val ratioPerTask = 1.0 / tasks.size
            var lastTaskIndex = -1
            override fun update(ratio: Double) {
                val taskIndex = (ratio / ratioPerTask).toInt()
                val ratioInTask = (ratio - (taskIndex * ratioPerTask)) / ratioPerTask
                while (taskIndex > lastTaskIndex) {
                    tasks.getOrNull(lastTaskIndex)?.finish()
                    lastTaskIndex++
                    tasks.getOrNull(lastTaskIndex)?.start()
                }
                val action = tasks[min(tasks.size - 1, taskIndex)]
                action.update(ratio)
                println("UPDATE: ${tasks.size}, $ratio, $taskIndex, $ratioInTask")
            }
        }
    }
    class Parallel(val actions: List<ViewAction>) : ViewAction {
        override val time: Double = actions.map { it.time }.max() ?: 0.0
        override fun createRunner(view: View): ViewActionRunner = object : ViewActionRunner(view, this) {
            val tasks = actions.map { it.createRunner(view) }
            override fun update(ratio: Double) {
                for (task in tasks) task.update(ratio)
            }
        }
    }

    class Repeat(val count: Int, val action: ViewAction) : ViewAction {
        override val time: Double = action.time * count
        override fun createRunner(view: View): ViewActionRunner = object : ViewActionRunner(view, this) {
            override fun update(ratio: Double) {
            }
        }
    }
}

fun buildAction(callback: ActionListBuilder.() -> Unit): ViewAction {
    return ActionListBuilder().apply(callback).build()
}

fun buildActionList(callback: ActionListBuilder.() -> Unit): List<ViewAction> {
    return ActionListBuilder().apply(callback).buildList()
}

class ActionListBuilder {
    private val list = arrayListOf<ViewAction>()

    fun seq(callback: ActionListBuilder.() -> Unit) {
        list += buildAction(callback)
    }

    fun parallel(callback: ActionListBuilder.() -> Unit) {
        list += ViewAction.Parallel(buildActionList(callback))
    }

    fun repeat(count: Int, callback: ActionListBuilder.() -> Unit) {
        list += ViewAction.Repeat(count, buildAction(callback))
    }

    fun moveBy(x: Double, y: Double, time: Double = 1.0) {
        list += ViewAction.MoveBy(x, y, time)
    }

    fun show(time: Double = 1.0) {
        list += ViewAction.SetAlpha(1.0, time)
    }

    fun hide(time: Double = 1.0) {
        list += ViewAction.SetAlpha(0.0, time)
    }

    internal fun buildList(): List<ViewAction> = list

    internal fun build(): ViewAction {
        if (list.size == 1) {
            return list[0]
        } else {
            return ViewAction.AList(list)
        }
    }
}
