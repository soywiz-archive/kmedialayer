package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*

object SceneScope

open class Scene {
    val root = SceneContainer(this)

    lateinit var gl: KmlGl
    lateinit var application: SceneApplication

    open suspend fun init() {
    }

    fun render(rc: SceneRenderContext) {
        root.render(rc)
    }


    open fun onKeyDown(key: Key) {
    }

    open fun onKeyUp(key: Key) {
    }

    open fun onMouseMove(x: Int, y: Int) {
    }

    open fun onMouseDown(button: Int) {
    }

    open fun onMouseUp(button: Int) {
    }

    open fun onUpdate(ms: Int) {
    }

    open fun onResize(width: Int, height: Int) {
    }

    private val tempComponents: ArrayList<ViewComponent> = arrayListOf()

    fun SceneApplication.updateScene(ms: Int) {
        forEachComponent<ViewUpdateComponent> { c -> c.update(ms.toDouble() * c.view.concatSpeed) }
        onUpdate(ms)
    }

    fun SceneApplication.resizeScene(width: Int, height: Int) {
        forEachComponent<ViewResizeComponent> { c -> c.resized(width, height) }
        onResize(width, height)
    }

    fun SceneApplication.mouseMoved(x: Int, y: Int) {
        forEachComponent<ViewMouseComponent> { c -> c.onMouseMove(x, y) }
        onMouseMove(x, y)
    }

    fun SceneApplication.mouseDown(button: Int) {
        forEachComponent<ViewMouseComponent> { c -> c.onMouseDown(button) }
        onMouseDown(button)
    }

    fun SceneApplication.mouseUp(button: Int) {
        forEachComponent<ViewMouseComponent> { c -> c.onMouseUp(button) }
        onMouseUp(button)
    }

    fun SceneApplication.mouseClick(button: Int) {
        forEachComponent<ViewMouseComponent> { c -> c.onMouseClick(button) }
        onMouseUp(button)
    }

    private inline fun <reified T : ViewComponent> forEachComponent(callback: (T) -> Unit) {
        for (c in getComponents(root, tempComponents)) {
            if (c is T) callback(c)
        }
    }

    private fun getComponents(view: View, out: ArrayList<ViewComponent> = arrayListOf()): List<ViewComponent> {
        out.clear()
        appendComponents(view, out)
        return out
    }

    private fun appendComponents(view: View, out: ArrayList<ViewComponent>) {
        if (view is ViewContainer) for (child in view.children) appendComponents(child, out)
        val components = view.components
        if (components != null) out.addAll(components)
    }
}

class SceneRenderContext(
    val batcher: SceneBatcher
) {
    fun flush() {
        batcher.flush()
    }
}

open class SceneContainer(val rootScene: Scene) : ViewContainer() {
}

suspend fun Scene.texture(name: String) = SceneTexture(gl.createKmlTexture().upload(Kml.decodeImage(name)))
suspend fun Scene.texture(data: ByteArray) = SceneTexture(gl.createKmlTexture().upload(Kml.decodeImage(data)))

val View.scene: Scene? get() = (root as? SceneContainer?)?.rootScene
