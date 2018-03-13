package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import kotlinx.cinterop.ByteVar
import platform.windows.*
import platform.opengl32.*
import platform.posix.*

//fun main(args: Array<String>) {
//    runApp(MyAppHandler())
//}
actual val Kml: KmlBase = KmlBaseNativeWin32
private val glNative: KmlGlNative by lazy { KmlGlNative() }

lateinit var nwindowConfig: WindowConfig
lateinit var nlistener: KMLWindowListener

object KmlBaseNativeWin32 : KmlBaseNoEventLoop() {
    override fun application(windowConfig: WindowConfig, listener: KMLWindowListener) {
        nwindowConfig = windowConfig
        nlistener = listener

        memScoped {
            // https://www.khronos.org/opengl/wiki/Creating_an_OpenGL_Context_(WGL)

            val windowTitle = windowConfig.title
            val windowWidth = windowConfig.width
            val windowHeight = windowConfig.height

            val wc = alloc<WNDCLASSW>()

            val clazzName = "oglkotlinnative"
            val clazzNamePtr = clazzName.wcstr.getPointer(this@memScoped)
            wc.lpfnWndProc = staticCFunction(::WndProc)
            wc.hInstance = null
            wc.hbrBackground = COLOR_BACKGROUND.uncheckedCast()
            wc.lpszClassName = clazzNamePtr
            wc.style = CS_OWNDC;
            if (RegisterClassW(wc.ptr).toInt() == 0) {
                return
            }

            val screenWidth = GetSystemMetrics(SM_CXSCREEN)
            val screenHeight = GetSystemMetrics(SM_CYSCREEN)
            val hwnd = CreateWindowExW(
                    WS_EX_CLIENTEDGE,
                    clazzName,
                    windowTitle,
                    WS_OVERLAPPEDWINDOW or WS_VISIBLE,
                    kotlin.math.min(kotlin.math.max(0, (screenWidth - windowWidth) / 2), screenWidth - 16),
                    kotlin.math.min(kotlin.math.max(0, (screenHeight - windowHeight) / 2), screenHeight - 16),
                    windowWidth,
                    windowHeight,
                    null, null, null, null
            )
            println("ERROR: " + GetLastError())

            ShowWindow(hwnd, SW_SHOWNORMAL)

            SetTimer(hwnd, 1, 1000 / 60, staticCFunction(::WndTimer))
        }

        memScoped {
            val msg = alloc<MSG>()
            while (GetMessageW(msg.ptr, null, 0, 0) > 0) {
                TranslateMessage(msg.ptr)
                DispatchMessageW(msg.ptr)
            }
        }
    }

    fun runInitBlocking(listener: KMLWindowListener) {
        runBlocking {
            listener.init(glNative)
        }
    }

    override fun currentTimeMillis(): Double = kotlin.system.getTimeMillis().toDouble()

    override fun sleep(time: Int) {
        usleep(time * 1000)
    }

    override fun pollEvents() {
    }

    override suspend fun decodeImage(data: ByteArray): KmlNativeImageData {
        return decodeImageSync(data)
    }

    fun decodeImageSync(data: ByteArray): KmlNativeImageData {
        TODO()
    }

    override suspend fun loadFileBytes(path: String, range: LongRange?): ByteArray {
        return loadFileBytesSync(path, range)
    }

    override suspend fun writeFileBytes(path: String, data: ByteArray, offset: Long?): Unit {
        TODO("KmlBase.writeFileBytes")
    }
}

var glRenderContext: HGLRC? = null

fun renderFunction() {
    glClearColor(0.2f, 0.4f, 0.6f, 1.0f)
    glClear(GL_COLOR_BUFFER_BIT)
    //initExtGLOnce()
    val program = glCreateProgram()
    println(program)
}

val glCreateProgram by lazy { wglGetProcAddress("glCreateProgram").uncheckedCast<PFNGLCREATEPROGRAMPROC>() }
// ...

fun resized(width: Int, height: Int) {
    println("RESIZED: $width, $height")
    glViewport(0, 0, width, height)
    nlistener.resized(width, height)
}

fun mouseMove(x: Int, y: Int) {
    println("MOUSE_MOVE: $x, $y")
    nlistener.mouseUpdateMove(x, y)
}

fun mouseButton(button: Int, pressed: Boolean) {
    println("MOUSE_BUTTON: $button, $pressed")
    nlistener.mouseUpdateButton(button, pressed)
}

fun keyUpdate(key: Int, pressed: Boolean) {
    // VK_LEFT
    println("KEY_UPDATE: $key, $pressed")
    nlistener.keyUpdate(Key.UNKNOWN, pressed)
}

fun tryRender(hWnd: HWND?) {
    if (glRenderContext != null) {
        val hDC = GetDC(hWnd)
        wglMakeCurrent(hDC, glRenderContext)
        renderFunction()
        SwapBuffers(hDC)
    }
}

@Suppress("UNUSED_PARAMETER")
fun WndTimer(hWnd: HWND?, message: UINT, WPARAM: UINT_PTR, lParam: DWORD) {
    //println("TIMER")
    tryRender(hWnd)
}

@Suppress("UNUSED_PARAMETER")
fun WndProc(hWnd: HWND?, message: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    //println("WndProc: $hWnd, $message, $wParam, $lParam")
    when (message) {
        WM_CREATE -> {
            memScoped {
                val pfd = alloc<PIXELFORMATDESCRIPTOR>()
                pfd.nSize = PIXELFORMATDESCRIPTOR.size.toShort()
                pfd.nVersion = 1
                pfd.dwFlags = PFD_DRAW_TO_WINDOW or PFD_SUPPORT_OPENGL or PFD_DOUBLEBUFFER
                pfd.iPixelType = PFD_TYPE_RGBA.toByte()
                pfd.cColorBits = 32
                pfd.cDepthBits = 24
                pfd.cStencilBits = 8
                pfd.iLayerType = PFD_MAIN_PLANE.toByte()
                val hDC = GetDC(hWnd)
                val letWindowsChooseThisPixelFormat = ChoosePixelFormat(hDC, pfd.ptr)

                SetPixelFormat(hDC, letWindowsChooseThisPixelFormat, pfd.ptr)
                glRenderContext = wglCreateContext(hDC)
            }
        }
        WM_SIZE -> {
            var width = 0
            var height = 0
            memScoped {
                val rect = alloc<RECT>()
                GetClientRect(hWnd, rect.ptr)
                width = rect.right - rect.left
                height = rect.bottom - rect.top
            }
            //val width = (lParam.toInt() ushr 0) and 0xFFFF
            //val height = (lParam.toInt() ushr 16) and 0xFFFF
            resized(width, height)
            tryRender(hWnd)
        }
        WM_QUIT -> {
            kotlin.system.exitProcess(0)
        }
        WM_MOUSEMOVE -> {
            val x = (lParam.toInt() ushr 0) and 0xFFFF
            val y = (lParam.toInt() ushr 16) and 0xFFFF
            mouseMove(x, y)
        }
        WM_LBUTTONDOWN -> mouseButton(0, true)
        WM_MBUTTONDOWN -> mouseButton(1, true)
        WM_RBUTTONDOWN -> mouseButton(2, true)
        WM_LBUTTONUP -> mouseButton(0, false)
        WM_MBUTTONUP -> mouseButton(1, false)
        WM_RBUTTONUP -> mouseButton(2, false)
        WM_KEYDOWN -> keyUpdate(wParam.toInt(), true)
        WM_KEYUP -> keyUpdate(wParam.toInt(), false)

        WM_CLOSE -> {
            kotlin.system.exitProcess(0)
        }
    }
    return DefWindowProcW(hWnd, message, wParam, lParam)
}
