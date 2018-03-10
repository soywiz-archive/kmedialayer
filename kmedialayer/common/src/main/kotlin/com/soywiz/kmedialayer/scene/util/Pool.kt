package com.soywiz.kmedialayer.scene.util

class Pool<T>(private val reset: T.() -> Unit = {}, private val gen: () -> T) {
    private val freed = arrayListOf<T>()

    inline fun use(callback: (T) -> Unit) {
        val item = get()
        try {
            callback(item)
        } finally {
            free(item)
        }
    }

    fun get(): T = synchronized(freed) {
        if (freed.isEmpty()) free(gen())
        val item = freed.removeAt(freed.size - 1)
        item.reset()
        return item
    }

    fun free(value: T) = synchronized(freed) {
        freed += value
    }
}
