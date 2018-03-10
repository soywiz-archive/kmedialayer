package com.soywiz.kmedialayer.scene.util

class Signal<T> {
    private val handlers = arrayListOf<(T) -> Unit>()
    companion object {
        private val arrayPool = Pool<ArrayList<(Any) -> Unit>>({ clear() }, { arrayListOf() })
    }

    operator fun invoke(handler: (T) -> Unit): (T) -> Unit {
        handlers += handler
        return handler
    }

    operator fun plusAssign(handler: (T) -> Unit) {
        handlers += handler
    }

    operator fun minusAssign(handler: (T) -> Unit) {
        handlers -= handler
    }

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(value: T) {
        arrayPool.use { temp ->
            temp.addAll(handlers as ArrayList<(Any) -> Unit>)
            for (handler in temp) {
                (handler as (T) -> Unit)(value)
            }
        }
    }
}
