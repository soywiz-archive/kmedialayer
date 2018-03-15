package com.soywiz.kmedialayer.scene

import com.soywiz.kmedialayer.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

public inline fun <T, R> Iterable<T>.firstNotNullOrNull(predicate: (T) -> R?): R? {
    for (e in this) {
        val res = predicate(e)
        if (res != null) return res
    }
    return null
}

fun <T : Scene> testSceneApplication(scene: T, windowConfig: WindowConfig = WindowConfig(), testBlock: suspend T.() -> Unit) {
    var time = 0L
    scene.gl = KmlGlDummy
    val kml = object : KmlBase() {
        lateinit var app: SceneApplication

        override suspend fun loadFileBytes(path: String, range: LongRange?): ByteArray {
            val base = File(".").absolutePath.replaceAfter(".idea", "").replace(".idea", "")
            val all = File(File(base), path).readBytes()
            //val clazz = SceneApplication::class.java
            //val loaders = Thread.currentThread().stackTrace.map { Class.forName(it.className).classLoader }.filterNotNull()
            //val i = loaders.firstNotNullOrNull { it.getResourceAsStream(path) } ?: throw RuntimeException("Can't load file '$path'")
            //val all = i.readBytes()
            //val i = loaders.firstNotNullOrNull { it.getResourceAsStream(path) } ?: throw RuntimeException("Can't load file '$path'")
            //val all = i.readBytes()
            return if (range == null) all else all.copyOfRange(range.start.toInt(), (range.endInclusive + 1).toInt())
        }

        override suspend fun delay(ms: Int) {
            // Do not delay
            step(ms)
        }

        fun step(ms: Int) {
            val chunk = 16
            var elapsed = 0
            app.apply {
                scene.apply {
                    while (elapsed <= ms) {
                        time += chunk
                        //println("step: $chunk")
                        updateScene(chunk)
                        elapsed += chunk
                    }
                }
            }
        }

        override fun currentTimeMillis(): Double = time.toDouble()

        override suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
            val reader = DataInputStream(ByteArrayInputStream(data))
            val magic = reader.readInt()
            if (magic == 0x89504E47.toInt()) { // PNG
                reader.skip(12)
                val width = reader.readInt()
                val height = reader.readInt()
                return object : KmlNativeImageData {
                    override val width: Int = width
                    override val height: Int = height
                }
            }
            throw IllegalArgumentException("Unknown image type with magic $magic")
        }
    }
    scene.application = object : SceneApplication {
        override val kml: KmlBase = kml
        override val mouse = SceneApplication.Mouse()
    }
    kml.app = scene.application
    runBlocking(CancellationToken(), {
        kml.step(16)
    }) {
        scene.init()
        scene.testBlock()
    }
}

fun <T : Any> runBlocking(context: CoroutineContext = EmptyCoroutineContext, step: () -> Unit, callback: suspend () -> T): T {
    var done = false
    lateinit var resultValue: T
    var resultException: Throwable? = null
    callback.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = context
        override fun resume(value: T) {
            resultValue = value
            done = true
        }

        override fun resumeWithException(exception: Throwable) {
            println(exception)
            resultException = exception
            done = true
        }
    })
    while (!done) {
        step()
    }
    if (resultException != null) throw resultException!!
    return resultValue
}

