package com.example.facecheckapp

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.abs

object ImageUtils {

    fun cropFace(bitmap: Bitmap?, box: Rect): Bitmap {
        if (bitmap == null) {
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }

        val left = box.left.coerceAtLeast(0)
        val top = box.top.coerceAtLeast(0)
        val width = box.width().coerceAtMost(bitmap.width - left)
        val height = box.height().coerceAtMost(bitmap.height - top)

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    fun calculatePHash(bmp: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bmp, 32, 32, false)

        val gray = ArrayList<Int>()
        for (x in 0 until 32) {
            for (y in 0 until 32) {
                val c = resized.getPixel(x, y)
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff
                gray.add((r + g + b) / 3)
            }
        }

        val avg = gray.average()
        return gray.joinToString("") { if (it > avg) "1" else "0" }
    }

    fun hammingDistance(h1: String, h2: String): Int {
        if (h1.length != h2.length) return Int.MAX_VALUE
        return h1.indices.count { h1[it] != h2[it] }
    }
}
