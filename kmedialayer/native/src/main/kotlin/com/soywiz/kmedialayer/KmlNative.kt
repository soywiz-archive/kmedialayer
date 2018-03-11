package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import platform.GLUT.*
import platform.OpenGL.*
import platform.OpenGLCommon.*
import platform.posix.*

private lateinit var globalListener: KMLWindowListener
private val glNative: KmlGlNative by lazy { KmlGlNative() }

object KmlBaseNative : KmlBaseNoEventLoop() {
    override fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        globalListener = listener

        memScoped {
            val argc = alloc<IntVar>().apply { value = 0 }
            glutInit(argc.ptr, null) // TODO: pass real args
        }

        // Display Mode
        glutInitDisplayMode(GLUT_RGB or GLUT_DOUBLE or GLUT_DEPTH)

        // Set window size
        //glutSetOption(GLUT_ACTION_ON_WINDOW_CLOSE, GLUT_ACTION_GLUTMAINLOOP_RETURNS);
        glutInitWindowSize(windowConfig.width, windowConfig.height)
        glutInitWindowPosition(
            (glutGet(GLUT_SCREEN_WIDTH)-windowConfig.width)/2,
            (glutGet(GLUT_SCREEN_HEIGHT)-windowConfig.height)/2
        )

        // create Window
        glutCreateWindow(windowConfig.title)

        // register Display Function
        glutDisplayFunc(staticCFunction(::onDisplay))
        glutReshapeFunc(staticCFunction(::onReshape))
        glutMouseFunc(staticCFunction(::onMouseButton))
        glutMotionFunc(staticCFunction(::onMouseMotion));
        glutPassiveMotionFunc(staticCFunction(::onMouseMotion));
        //glutKeyboardFunc(staticCFunction(::onKeyboardDown))
        //glutKeyboardUpFunc(staticCFunction(::onKeyboardUp))

        glutSpecialFunc(staticCFunction(::onKeyboardDown))
        glutSpecialUpFunc(staticCFunction(::onKeyboardUp))

        // register Idle Function
        glutIdleFunc(staticCFunction(::onDisplay))

        runBlocking {
            listener.init(glNative)
        }

        // run GLUT mainloop
        glutMainLoop()
        //glutMainLoopEvent()
    }

    override fun currentTimeMillis(): Double = kotlin.system.getTimeMillis().toDouble()

    override fun sleep(time: Int) {
        usleep(time * 1000)
    }

    override fun pollEvents() {
    }

    override suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        TODO("KmlBase.decodeImage(ByteArray)")
    }

    override suspend fun loadFileBytes(path: String, range: LongRange?): ByteArray {
        return readBytes(path, range)
    }

    override suspend fun writeFileBytes(path: String, data: ByteArray, offset: Long?): Unit {
        TODO("KmlBase.writeFileBytes")
    }
}

fun readBytes(fileName: String, range: LongRange?): ByteArray {
    val file = fopen(fileName, "rb") ?: throw RuntimeException("Can't open file $fileName")
    fseek(file, 0, SEEK_END)
    val endPos = ftell(file)
    val start = range?.start ?: 0L
    val count = range?.endInclusive?.minus(1) ?: (endPos - start)
    fseek(file, start.narrow(), SEEK_CUR)
    val bytes = memScoped {
        val ptr = allocArray<ByteVar>(count)
        val readCount = fread(ptr, 1, count.narrow(), file).toInt()
        ptr.readBytes(readCount)
    }
    fclose(file)
    return bytes
}

actual val Kml: KmlBase = KmlBaseNative

private fun onDisplay() {
    globalListener.render(glNative)
    glutSwapBuffers()
}

private fun onMouseButton(button: Int, state: Int, x: Int, y: Int) {
    globalListener.mouseUpdateButton(button, state == GLUT_DOWN)
}

private fun onMouseMotion(x: Int, y: Int) {
    globalListener.mouseUpdateMove(x, y)
}

private fun onReshape(width: Int, height: Int) {
    glViewport(0, 0, width, height)
    globalListener.resized(width, height)
}

private fun onKeyboardInternal(keyCode: Int, pressed: Boolean) {
    val key = KEYS[keyCode.toInt() and 0xFF] ?: Key.UNKNOWN
    println("$keyCode: $key: $pressed")
    globalListener.keyUpdate(key, pressed)
}

private fun onKeyboardDown(keyCode: Int, x: Int, y: Int) = onKeyboardInternal(keyCode, true)
private fun onKeyboardUp(keyCode: Int, x: Int, y: Int) = onKeyboardInternal(keyCode, false)


private val KEYS = mapOf(
    8 to Key.BACKSPACE,
    9 to Key.TAB,
    13 to Key.ENTER,
    16 to Key.LEFT_SHIFT,
    17 to Key.LEFT_CONTROL,
    18 to Key.LEFT_ALT,
    19 to Key.PAUSE,
    20 to Key.CAPS_LOCK,
    27 to Key.ESCAPE,
    33 to Key.PAGE_UP,
    34 to Key.PAGE_DOWN,
    35 to Key.END,
    36 to Key.HOME,
    37 to Key.LEFT,
    38 to Key.UP,
    39 to Key.RIGHT,
    40 to Key.DOWN,
    45 to Key.INSERT,
    46 to Key.DELETE,
    48 to Key.N0,
    49 to Key.N1,
    50 to Key.N2,
    51 to Key.N3,
    52 to Key.N4,
    53 to Key.N5,
    54 to Key.N6,
    55 to Key.N7,
    56 to Key.N8,
    57 to Key.N9,
    65 to Key.A,
    66 to Key.B,
    67 to Key.C,
    68 to Key.D,
    69 to Key.E,
    70 to Key.F,
    71 to Key.G,
    72 to Key.H,
    73 to Key.I,
    74 to Key.J,
    75 to Key.K,
    76 to Key.L,
    77 to Key.M,
    78 to Key.N,
    79 to Key.O,
    80 to Key.P,
    81 to Key.Q,
    82 to Key.R,
    83 to Key.S,
    84 to Key.T,
    85 to Key.U,
    86 to Key.V,
    87 to Key.W,
    88 to Key.X,
    89 to Key.Y,
    90 to Key.Z,
    91 to Key.LEFT_SUPER,
    92 to Key.RIGHT_SUPER,
    93 to Key.SELECT_KEY,
    96 to Key.KP_0,
    97 to Key.KP_1,
    98 to Key.KP_2,
    99 to Key.KP_3,
    100 to Key.KP_4,
    101 to Key.KP_5,
    102 to Key.KP_6,
    103 to Key.KP_7,
    104 to Key.KP_8,
    105 to Key.KP_9,
    106 to Key.KP_MULTIPLY,
    107 to Key.KP_ADD,
    109 to Key.KP_SUBTRACT,
    110 to Key.KP_DECIMAL,
    111 to Key.KP_DIVIDE,
    112 to Key.F1,
    113 to Key.F2,
    114 to Key.F3,
    115 to Key.F4,
    116 to Key.F5,
    117 to Key.F6,
    118 to Key.F7,
    119 to Key.F8,
    120 to Key.F9,
    121 to Key.F10,
    122 to Key.F11,
    123 to Key.F12,
    144 to Key.NUM_LOCK,
    145 to Key.SCROLL_LOCK,
    186 to Key.SEMICOLON,
    187 to Key.EQUAL,
    188 to Key.COMMA,
    189 to Key.UNDERLINE,
    190 to Key.PERIOD,
    191 to Key.SLASH,
    192 to Key.GRAVE_ACCENT,
    219 to Key.LEFT_BRACKET,
    220 to Key.BACKSLASH,
    221 to Key.RIGHT_BRACKET,
    222 to Key.APOSTROPHE
)

class KmlNativeNativeImageData(override val width: Int, override val height: Int, val data: KmlBuffer) : KmlNativeImageData {

}
