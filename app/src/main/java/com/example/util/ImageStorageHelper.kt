package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    fun saveCompressedImage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream.close()

            val maxDimension = 1200
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val max = width.coerceAtLeast(height)
                maxDimension.toFloat() / max
            } else 1.0f

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            val storageDir = File(context.filesDir, "plant_growth_photos")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val fileName = "plant_photo_${System.currentTimeMillis()}.jpg"
            val photoFile = File(storageDir, fileName)

            val outputStream = FileOutputStream(photoFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            outputStream.flush()
            outputStream.close()

            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            photoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
