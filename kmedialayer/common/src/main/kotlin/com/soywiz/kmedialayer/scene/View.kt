package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.scene.geom.*

open class View {
    companion object {
        private val IDENTITY = Matrix2d()
    }

    var parent: ViewContainer? = null
    val root: ViewContainer? get() = parent?.root ?: this as? ViewContainer?
    var validParents = false
    var validChildren = false
    var name: String? = null

    protected val _transform = ViewTransform()
    protected val _globalMatrix = Matrix2d()
    protected var _invGlobalMatrixValid = false
    protected val _invGlobalMatrix = Matrix2d()
    private var _components: ArrayList<ViewComponent>? = null
    val components: List<ViewComponent>? get() = _components

    inline fun <reified T : ViewComponent> getOrCreateComponent(component: (View) -> T): T {
        return (components?.firstOrNull { it is T } as? T) ?: component(this).apply { addComponent(this) }
    }

    fun addComponent(component: ViewComponent) {
        if (_components == null) _components = arrayListOf()
        _components?.add(component)
    }

    fun removeComponent(component: ViewComponent) {
        val removed = _components?.remove(component) ?: false
        if (removed) {
            //component.dettached()
        }
        if (_components?.size == 0) {
            _components = null
        }
    }

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

    val concatSpeed: Double get() = (parent?.concatSpeed ?: 1.0) * speed
    val concatAlpha: Double get() = (parent?.concatAlpha ?: 1.0) * alpha

    fun globalToLocalX(x: Double, y: Double): Double = invGlobalMatrix.transformX(x, y)
    fun globalToLocalY(x: Double, y: Double): Double = invGlobalMatrix.transformY(x, y)

    fun globalToLocal(p: Point, out: Point = Point()): Point = out.setToTransform(invGlobalMatrix, p)
    fun localToGlobal(p: Point, out: Point = Point()): Point = out.setToTransform(globalMatrix, p)

    open fun viewInGlobal(x: Double, y: Double): View? {
        return null
    }

    private val t get() = _transform.transform
    var x; get() = t.x; set(value) = run { invalidate(); t.x = value }
    var y; get() = t.y; set(value) = run { invalidate(); t.y = value }
    var scaleX; get() = t.scaleX; set(value) = run { invalidate(); t.scaleX = value }
    var scaleY; get() = t.scaleY; set(value) = run { invalidate(); t.scaleY = value }
    var rotation; get() = t.rotation; set(value) = run { invalidate(); t.rotation = value }
    var rotationDegrees; get() = t.rotationDegrees; set(value) = run { invalidate(); t.rotationDegrees = value }
    var alpha = 1.0; set(value) { invalidate(); field = value }
    var speed = 1.0

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

    open operator fun get(name: String): View? = if (this.name == name) this else null
}

interface ViewComponent {
    val view: View
}

fun ViewComponent.dettatch() = view.removeComponent(this)

interface ViewMouseComponent : ViewComponent {
    fun onMouseMove(x: Int, y: Int)
    fun onMouseUp(button :Int)
    fun onMouseDown(button :Int)
    fun onMouseClick(button :Int)
}

interface ViewUpdateComponent : ViewComponent {
    fun update(ms: Double)
}

interface ViewResizeComponent : ViewComponent {
    fun resized(width: Int, height: Int)
}

class ViewTransform {
    val matrix = Matrix2d()
    val transform = Matrix2d.Transform()

    fun update() {
        transform.toMatrix(matrix)
    }
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

    override fun viewInGlobal(x: Double, y: Double): View? {
        for (child in _children) return child.viewInGlobal(x, y) ?: continue
        return null
    }

    override operator fun get(name: String): View? {
        if (this.name == name) return this
        for (c in children) return c[name] ?: continue
        return null
    }
}

open class Image(var tex: SceneTexture) : View() {
    var p0 = Point()
    var p1 = Point()
    var p2 = Point()
    var p3 = Point()
    var computedAlpha = 1.0

    override fun recompute() {
        if (validParents && validChildren) return
        super.recompute()
        val gm = _globalMatrix
        p0.setToTransform(gm, 0.0, 0.0)
        p1.setToTransform(gm, tex.widthPixels.toDouble(), 0.0)
        p2.setToTransform(gm, 0.0, tex.heightPixels.toDouble())
        p3.setToTransform(gm, tex.widthPixels.toDouble(), tex.heightPixels.toDouble())
        computedAlpha = concatAlpha
    }

    override fun viewInGlobal(x: Double, y: Double): View? {
        val localX = globalToLocalX(x, y)
        val localY = globalToLocalY(x, y)
        return if (localX >= 0.0 && localX <= tex.widthPixels.toDouble() && localY >= 0.0 && localY <= tex.heightPixels.toDouble()) this else null
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
            computedAlpha.toFloat()
        )
    }
}
