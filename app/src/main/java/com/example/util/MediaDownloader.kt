package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaDownloader {

    /**
     * Saves a base64 or Bitmap image directly to the public device gallery (Pictures/ZYXO_AI)
     * Compatible with Android 10+ scoped storage without requiring dangerous storage permissions.
     */
    suspend fun saveImageToGallery(
        context: Context,
        imageBase64: String?,
        title: String = "ZYXO_AI_Art"
    ): Boolean = withContext(Dispatchers.IO) {
        if (imageBase64.isNullOrBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا: تصویر برای ذخیره یافت نشد", Toast.LENGTH_SHORT).show()
            }
            return@withContext false
        }

        try {
            val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext false

            val filename = "ZYXO_${System.currentTimeMillis()}.jpg"

            var outputStream: OutputStream? = null
            var success = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ZYXO_AI")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        outputStream.flush()
                        outputStream.close()
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                    success = true
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val zyxoDir = File(picturesDir, "ZYXO_AI")
                if (!zyxoDir.exists()) zyxoDir.mkdirs()

                val file = File(zyxoDir, filename)
                outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                outputStream.flush()
                outputStream.close()
                success = true
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "✅ تصویر با موفقیت در گالری گوشی ذخیره شد!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "خطا در ذخیره تصویر در گالری", Toast.LENGTH_SHORT).show()
                }
            }
            success
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}
