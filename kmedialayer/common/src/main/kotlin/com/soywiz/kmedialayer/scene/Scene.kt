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

    private val tempComponents: ArrayList<Component> = arrayListOf()

    fun SceneApplication.updateScene(ms: Int) {
        forEachComponent<UpdateComponent> { c -> c.update(ms.toDouble() * c.view.concatSpeed) }
        onUpdate(ms)
    }

    fun SceneApplication.resizeScene(width: Int, height: Int) {
        forEachComponent<ResizeComponent> { c -> c.resized(width, height) }
        onResize(width, height)
    }

    fun SceneApplication.keyDown(key: Key) {
        forEachComponent<KeyComponent> { c -> c.onKeyDown(key) }
        onKeyDown(key)
    }

    fun SceneApplication.keyUp(key: Key) {
        forEachComponent<KeyComponent> { c -> c.onKeyUp(key) }
        onKeyDown(key)
    }

    fun SceneApplication.mouseMoved(x: Int, y: Int) {
        forEachComponent<MouseComponent> { c -> c.onMouseMove(x, y) }
        onMouseMove(x, y)
    }

    fun SceneApplication.mouseDown(button: Int) {
        forEachComponent<MouseComponent> { c -> c.onMouseDown(button) }
        onMouseDown(button)
    }

    fun SceneApplication.mouseUp(button: Int) {
        forEachComponent<MouseComponent> { c -> c.onMouseUp(button) }
        onMouseUp(button)
    }

    fun SceneApplication.mouseClick(button: Int) {
        forEachComponent<MouseComponent> { c -> c.onMouseClick(button) }
        onMouseUp(button)
    }

    private inline fun <reified T : Component> forEachComponent(callback: (T) -> Unit) {
        for (c in getComponents(root, tempComponents)) {
            if (c is T) callback(c)
        }
    }

    private fun getComponents(view: View, out: ArrayList<Component> = arrayListOf()): List<Component> {
        out.clear()
        appendComponents(view, out)
        return out
    }

    private fun appendComponents(view: View, out: ArrayList<Component>) {
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
