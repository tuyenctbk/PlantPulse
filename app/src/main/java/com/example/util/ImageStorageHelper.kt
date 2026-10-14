package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    fun saveCompressedImage(context: Context, uri: Uri): String? {
        return try {
            // 1. Read EXIF orientation before stream close
            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            // 2. Decode original bitmap
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream.close()

            // 3. Apply rotation if needed
            val orientedBitmap = if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                val rotated = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                if (rotated != originalBitmap) {
                    originalBitmap.recycle()
                }
                rotated
            } else {
                originalBitmap
            }

            // 4. Downscale if exceeding max dimension to save storage
            val maxDimension = 1200
            val width = orientedBitmap.width
            val height = orientedBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val max = width.coerceAtLeast(height)
                maxDimension.toFloat() / max
            } else 1.0f

            val scaledBitmap = if (scale < 1.0f) {
                val scaled = Bitmap.createScaledBitmap(
                    orientedBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
                if (scaled != orientedBitmap) {
                    orientedBitmap.recycle()
                }
                scaled
            } else {
                orientedBitmap
            }

            // 5. Save to local app storage
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

            scaledBitmap.recycle()

            photoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
