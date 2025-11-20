package com.example.facecheckapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class FaceNetModel(private val context: Context) {

    private var interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile("facenet.tflite"))
    }

    private fun loadModelFile(name: String): ByteBuffer {
        val fd = context.assets.openFd(name)
        val input = FileInputStream(fd.fileDescriptor)
        val channel = input.channel

        return channel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {

        // 🔥 แก้ปัญหากล้องหน้ากลับด้าน
        val matrix = Matrix()
        matrix.preScale(-1f, 1f)  // flip left-right
        val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        val resized = Bitmap.createScaledBitmap(flipped, 160, 160, true)

        val inputBuffer = ByteBuffer.allocateDirect(1 * 160 * 160 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 160) {
            for (x in 0 until 160) {
                val px = resized.getPixel(x, y)

                inputBuffer.putFloat(((px shr 16) and 0xFF) / 255f)
                inputBuffer.putFloat(((px shr 8) and 0xFF) / 255f)
                inputBuffer.putFloat((px and 0xFF) / 255f)
            }
        }

        val output = Array(1) { FloatArray(512) }
        interpreter.run(inputBuffer, output)

        return output[0]
    }
}
