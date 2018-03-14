package com.soywiz.kmedialayer

import kotlinx.cinterop.*
import platform.gdiplus.*
import platform.windows.*

// https://referencesource.microsoft.com/#System.Drawing/commonui/System/Drawing/Advanced/Gdiplus.cs,87a3562f8aa6f54e,references

var initializedGdiPlus = false
fun initGdiPlusOnce() {
    if (initializedGdiPlus) return
    initializedGdiPlus = true
    memScoped {
        val ptoken = allocArray<ULONG_PTRVar>(1)
        val si = alloc<GdiplusStartupInput>().apply {
            GdiplusVersion = 1
            DebugEventCallback = null
            SuppressExternalCodecs = FALSE
            SuppressBackgroundThread = FALSE
        }
        GdiplusStartup(ptoken, si.ptr, null)
    }
}

fun gdipKmlLoadImage(imageName: String): KmlNativeNativeImageData {
    return memScoped {
        val pimage = allocArray<COpaquePointerVar>(1)
        val width = alloc<FloatVar>()
        val height = alloc<FloatVar>()

        println("Loading image $imageName...")

        initGdiPlusOnce()
        val res = GdipCreateBitmapFromFile(imageName.wcstr, pimage)
        if (res != 0) {
            throw RuntimeException("Can't find image $imageName")
        }

        GdipGetImageDimension(pimage[0], width.ptr, height.ptr)
        val iwidth = width.value.toInt()
        val iheight = height.value.toInt()

        val rect = alloc<GpRect>().apply {
            X = 0
            Y = 0
            Width = iwidth
            Height = iheight
        }
        val bmpData = alloc<BitmapData>()
        val res2 = GdipBitmapLockBits(pimage[0], rect.ptr, ImageLockModeRead, PixelFormat32bppARGB, bmpData.ptr)
        //println("res2: $res2")
        //println(bmpData.Width)
        //println(bmpData.Height)
        //println(bmpData.Stride)
        //println(bmpData.Scan0)
        val out = KmlIntBuffer(bmpData.Width * bmpData.Height)
        var n = 0
        for (y in 0 until bmpData.Height) {
            val p = (bmpData.Scan0.toLong() + (bmpData.Stride * y)).toCPointer<IntVar>()
            for (x in 0 until bmpData.Width) {
                out[n] = p!![x]
                n++
            }
        }

        GdipBitmapUnlockBits(pimage[0], bmpData.ptr)
        GdipDisposeImage(pimage[0])

        //println(out.toList())

        println("Loaded image $imageName ($iwidth, $iheight)")

        KmlNativeNativeImageData(width.value.toInt(), height.value.toInt(), out)
    }
}
