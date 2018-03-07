package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*
import com.soywiz.kmedialayer.scene.geom.*
import kotlin.math.*

open class Scene {
    val root = SceneContainer(this)

    lateinit var gl: KmlGl

    open suspend fun init() {
    }

    fun render(rc: SceneRenderContext) {
        root.render(rc)
    }


    open fun onKeyDown(keyCode: Int) {
    }

    open fun onKeyUp(keyCode: Int) {
    }

    open fun onMouseMove(x: Int, y: Int) {
    }

    open fun onMouseDown(button: Int) {
    }

    open fun onMouseUp(button: Int) {
    }

    fun update(ms: Int) {
        update(root, ms.toDouble())
    }

    fun update(view: View, ms: Double) {
        val ams = if (view.speed == 1.0) ms else view.speed * ms
        if (view is ViewContainer) {
            for (child in view.children) update(child, ams)
        }
        view.actions?.update(ams)
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

open class SceneContainer(val rootScene: Scene) : ViewContainer() {
}

open class View {
    var parent: ViewContainer? = null
    val root: ViewContainer? get() = parent?.root ?: this as? ViewContainer?
    val scene: Scene? get() = (root as? SceneContainer?)?.rootScene
    var validParents = false
    var validChildren = false

    protected val _transform = ViewTransform()
    protected val _globalMatrix = Matrix2d()
    protected var _invGlobalMatrixValid = false
    protected val _invGlobalMatrix = Matrix2d()
    var actions: ViewActionActionRunner? = null

    protected open fun recompute() {
        if (validParents && validChildren) return
        validParents = true
        validChildren = true
        _transform.update()
        val pgm = parent?.globalMatrix ?: IDENTITY
        _globalMatrix.setToIdentity()
        _globalMatrix.premultiply(pgm)
        _globalMatrix.premultiply(_transform.matrix)
        _invGlobalMatrixValid = false
    }

    val localMatrix: Matrix2d
        get() {
            recompute()
            return _transform.matrix
        }

    val localTransform: Matrix2d.Transform
        get() {
            recompute()
            return _transform.transform
        }

    val globalMatrix: Matrix2d
        get() {
            recompute()
            return _globalMatrix
        }

    val invGlobalMatrix: Matrix2d
        get() {
            recompute()
            if (!_invGlobalMatrixValid) {
                _invGlobalMatrix.setToInverse(_globalMatrix)
            }
            return _invGlobalMatrix
        }

    fun globalToLocal(p: Point, out: Point = Point()): Point = out.setToTransform(invGlobalMatrix, p)
    fun localToGlobal(p: Point, out: Point = Point()): Point = out.setToTransform(globalMatrix, p)

    open fun viewInGlobal(x: Int, y: Int): View? {
        return null
    }

    private val t get() = _transform.transform
    var x; get() = t.x; set(value) = run { invalidate(); t.x = value }
    var y; get() = t.y; set(value) = run { invalidate(); t.y = value }
    var scaleX; get() = t.scaleX; set(value) = run { invalidate(); t.scaleX = value }
    var scaleY; get() = t.scaleY; set(value) = run { invalidate(); t.scaleY = value }
    var rotation; get() = t.rotation; set(value) = run { invalidate(); t.rotation = value }
    var rotationDegrees; get() = t.rotationDegrees; set(value) = run { invalidate(); t.rotationDegrees = value }

    var speed = 1.0
    var alpha = 1.0

    fun act(action: ViewAction) {
        this.actions?.finish()
        this.actions = ViewActionActionRunner(action.createRunner(this))
    }

    fun act(callback: ActionListBuilder.() -> Unit) = act(buildAction(callback))

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
    private val _children = arrayListOf<View>()
    val children: List<View> get() = _children

    fun removeChild(view: View) {
        if (view.parent == this) {
            view.parent = null
            _children.remove(view)
        }
    }

    fun addChild(view: View) {
        if (view == this) throw RuntimeException("Can't add view to itself!")
        view.removeFromParent()
        view.parent = this
        this._children += view
        view.invalidate()
    }

    override fun render(rc: SceneRenderContext) {
        for (child in _children) child.render(rc)
    }

    operator fun plusAssign(view: View) {
        addChild(view)
    }

    override fun invalidateChildren() {
        validChildren = false
        for (child in _children) {
            if (child.validChildren) child.invalidateChildren()
        }
    }

    override fun viewInGlobal(x: Int, y: Int): View? {
        for (child in _children) return child.viewInGlobal(x, y) ?: continue
        return null
    }
}

open class Image(var tex: SceneTexture) : View() {
    var p0 = Point()
    var p1 = Point()
    var p2 = Point()
    var p3 = Point()

    override fun recompute() {
        if (validParents && validChildren) return
        super.recompute()
        val gm = _globalMatrix
        p0.setToTransform(gm, 0.0, 0.0)
        p1.setToTransform(gm, tex.widthPixels.toDouble(), 0.0)
        p2.setToTransform(gm, 0.0, tex.heightPixels.toDouble())
        p3.setToTransform(gm, tex.widthPixels.toDouble(), tex.heightPixels.toDouble())
    }

    override fun viewInGlobal(x: Int, y: Int): View? {
        recompute()
        val minX = min(min(min(p0.x, p1.x), p2.x), p3.x)
        val maxX = max(max(max(p0.x, p1.x), p2.x), p3.x)
        val minY = min(min(min(p0.y, p1.y), p2.y), p3.y)
        val maxY = max(max(max(p0.y, p1.y), p2.y), p3.y)
        return if ((x in minX..maxX) && (y in minY..maxY)) this else null
    }

    override fun render(rc: SceneRenderContext) {
        recompute()
        rc.batcher.addQuad(
            p0.x.toFloat(),
            p0.y.toFloat(),
            p1.x.toFloat(),
            p1.y.toFloat(),
            p2.x.toFloat(),
            p2.y.toFloat(),
            p3.x.toFloat(),
            p3.y.toFloat(),
            tex,
            alpha.toFloat()
        )
    }
}

suspend fun Scene.texture(name: String) = SceneTexture(gl.createKmlTexture().upload(Kml.decodeImage(name)))
suspend fun Scene.texture(data: ByteArray) = SceneTexture(gl.createKmlTexture().upload(Kml.decodeImage(data)))
