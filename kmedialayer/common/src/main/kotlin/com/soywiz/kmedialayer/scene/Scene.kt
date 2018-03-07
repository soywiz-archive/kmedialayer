package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*
import com.soywiz.kmedialayer.scene.geom.*

open class Scene {
    val root = ViewContainer()
    lateinit var gl: KmlGl

    open suspend fun init() {
    }

    fun render(rc: SceneRenderContext) {
        root.render(rc)
    }

    fun update(dt: Int) {
    }

    open fun onKeyDown(keyCode: Int) {
    }

    open fun onKeyUp(keyCode: Int) {
    }
}

class SceneRenderContext(
    val batcher: SceneBatcher
) {
    fun flush() {
        batcher.flush()
    }
}

private val IDENTITY = Matrix2d()

class ViewTransform {
    val matrix = Matrix2d()
    val transform = Matrix2d.Transform()

    fun update() {
        transform.toMatrix(matrix)
    }
}

open class View {
    var parent: ViewContainer? = null
    var validParents = false
    var validChildren = false

    protected val _transform = ViewTransform()
    protected val _globalMatrix = Matrix2d()

    protected open fun recompute() {
        if (validParents && validChildren) return
        validParents = true
        validChildren = true
        _transform.update()
        val pgm = parent?.globalMatrix ?: IDENTITY
        _globalMatrix.setToIdentity()
        _globalMatrix.premultiply(pgm)
        _globalMatrix.premultiply(_transform.matrix)
    }

    val localMatrix: Matrix2d get() {
        recompute()
        return _transform.matrix
    }

    val globalMatrix: Matrix2d get() {
        recompute()
        return _globalMatrix
    }

    var x get() = _transform.transform.x; set(value) { invalidate(); _transform.transform.x = value }
    var y get() = _transform.transform.y; set(value) { invalidate(); _transform.transform.y = value }
    var scaleX get() = _transform.transform.scaleX; set(value) { invalidate(); _transform.transform.scaleX = value }
    var scaleY get() = _transform.transform.scaleY; set(value) { invalidate(); _transform.transform.scaleY = value }
    var rotation get() = _transform.transform.rotation; set(value) { invalidate(); _transform.transform.rotation = value }
    var rotationDegrees get() = _transform.transform.rotationDegrees; set(value) { invalidate(); _transform.transform.rotationDegrees = value }
    var speed = 1f
    var alpha = 1f

    fun invalidate() {
        invalidateParent()
        invalidateChildren()
    }

    open fun invalidateChildren() {
        validChildren = false
    }

    private fun invalidateParent() {
        if (parent?.validParents == true) parent?.invalidate()
        validParents = false
    }

    open fun render(rc: SceneRenderContext) {
    }

    fun removeFromParent() = run { parent?.removeChild(this) }
}

open class ViewContainer : View() {
    private val children = arrayListOf<View>()

    fun removeChild(view: View) {
        if (view.parent == this) {
            view.parent = null
            children.remove(view)
        }
    }

    fun addChild(view: View) {
        if (view == this) throw RuntimeException("Can't add view to itself!")
        view.removeFromParent()
        view.parent = this
        this.children += view
    }

    override fun render(rc: SceneRenderContext) {
        for (child in children) child.render(rc)
    }

    operator fun plusAssign(view: View) {
        addChild(view)
    }

    override fun invalidateChildren() {
        validChildren = false
        for (child in children) {
            if (child.validChildren) child.invalidateChildren()
        }
    }
}

open class Image(var tex: SceneTexture) : View() {
    var p0 = Point()
    var p1 = Point()
    var p2 = Point()
    var p3 = Point()

    protected override fun recompute() {
        if (validParents && validChildren) return
        super.recompute()
        val gm = _globalMatrix
        p0.setToTransform(gm, 0.0, 0.0)
        p1.setToTransform(gm, tex.widthPixels.toDouble(), 0.0)
        p2.setToTransform(gm, 0.0, tex.heightPixels.toDouble())
        p3.setToTransform(gm, tex.widthPixels.toDouble(), tex.heightPixels.toDouble())
    }

    override fun render(rc: SceneRenderContext) {
        recompute()
        rc.batcher.addQuad(p0.x.toFloat(), p0.y.toFloat(), p1.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat(), tex)
    }
}

suspend fun Scene.texture(name: String) = SceneTexture(gl.createKmlTexture().upload(Kml.decodeImage(name)))
