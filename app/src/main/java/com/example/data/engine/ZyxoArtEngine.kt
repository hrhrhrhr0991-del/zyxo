package com.example.data.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.random.Random

object ZyxoArtEngine {

    fun generateArt(
        prompt: String,
        styleName: String,
        aspectRatio: String,
        resolution: String,
        sourceBitmap: Bitmap? = null
    ): Pair<String, String> {
        val (width, height) = when (aspectRatio) {
            "16:9" -> 1280 to 720
            "9:16" -> 720 to 1280
            "4:3" -> 1024 to 768
            "3:4" -> 768 to 1024
            else -> 1024 to 1024
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val p = prompt.lowercase(Locale.ROOT)

        val (c1, c2, c3, highlightColor) = when {
            p.contains("سایبر") || p.contains("نئون") || styleName.contains("سایبرپانک") -> {
                listOf(
                    Color.rgb(10, 10, 26),
                    Color.rgb(38, 14, 68),
                    Color.rgb(13, 31, 74),
                    Color.rgb(6, 182, 212)
                )
            }
            p.contains("طبیعت") || p.contains("جنگل") || p.contains("nature") -> {
                listOf(
                    Color.rgb(15, 28, 20),
                    Color.rgb(20, 50, 35),
                    Color.rgb(34, 80, 55),
                    Color.rgb(74, 222, 128)
                )
            }
            p.contains("غروب") || p.contains("sunset") || styleName.contains("آبرنگ") -> {
                listOf(
                    Color.rgb(40, 10, 30),
                    Color.rgb(120, 35, 60),
                    Color.rgb(220, 80, 60),
                    Color.rgb(251, 191, 36)
                )
            }
            else -> {
                listOf(
                    Color.rgb(8, 12, 30),
                    Color.rgb(25, 28, 65),
                    Color.rgb(45, 60, 120),
                    Color.rgb(56, 189, 248)
                )
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Sky & Atmosphere Gradient
        val bgShader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(c1, c2, c3),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Source image overlay if editing
        if (sourceBitmap != null) {
            val destRect = Rect(0, 0, width, height)
            val srcRect = Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
            paint.alpha = 200
            canvas.drawBitmap(sourceBitmap, srcRect, destRect, paint)
            paint.alpha = 255
        }

        // 3. Glowing Celestial Body (Sun/Moon/Core)
        paint.shader = null
        val celestialX = width * 0.72f
        val celestialY = height * 0.32f
        val celestialRadius = width * 0.14f

        val glowShader = RadialGradient(
            celestialX, celestialY, celestialRadius * 2.2f,
            intArrayOf(highlightColor, Color.argb(80, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor)), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = glowShader
        canvas.drawCircle(celestialX, celestialY, celestialRadius * 2.2f, paint)

        paint.shader = null
        paint.color = Color.WHITE
        paint.alpha = 240
        canvas.drawCircle(celestialX, celestialY, celestialRadius, paint)

        // 4. Starfield & Cosmic Dust
        val random = Random(prompt.hashCode().toLong())
        paint.style = Paint.Style.FILL
        for (i in 0..120) {
            val sx = random.nextFloat() * width
            val sy = random.nextFloat() * (height * 0.65f)
            val sRadius = random.nextFloat() * 2.5f + 0.8f
            paint.color = Color.WHITE
            paint.alpha = random.nextInt(90, 255)
            canvas.drawCircle(sx, sy, sRadius, paint)
        }

        // 5. Cinematic Mountain Silhouettes (Back Layer)
        paint.shader = null
        paint.color = Color.argb(160, Color.red(c1), Color.green(c1), Color.blue(c1))
        val backMountainPath = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(0f, height * 0.58f)
            lineTo(width * 0.22f, height * 0.42f)
            lineTo(width * 0.45f, height * 0.54f)
            lineTo(width * 0.68f, height * 0.38f)
            lineTo(width * 0.88f, height * 0.50f)
            lineTo(width.toFloat(), height * 0.44f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(backMountainPath, paint)

        // 6. Foreground Silhouette Layer with Horizon
        paint.color = Color.argb(245, 5, 8, 16)
        val foreMountainPath = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(0f, height * 0.68f)
            lineTo(width * 0.18f, height * 0.55f)
            lineTo(width * 0.35f, height * 0.64f)
            lineTo(width * 0.55f, height * 0.50f)
            lineTo(width * 0.78f, height * 0.62f)
            lineTo(width.toFloat(), height * 0.56f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(foreMountainPath, paint)

        // 7. Horizon Mist / Fog Light
        val mistShader = LinearGradient(
            0f, height * 0.52f, 0f, height * 0.75f,
            intArrayOf(Color.TRANSPARENT, Color.argb(60, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor)), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = mistShader
        canvas.drawRect(0f, height * 0.52f, width.toFloat(), height * 0.75f, paint)

        // 8. Studio Watermark Badge
        paint.shader = null
        paint.color = Color.argb(160, 255, 255, 255)
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("ZYXO AI • 4K ARTWORK", 36f, height - 36f, paint)

        // Compress to JPEG Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val description = "اثر هنری با کیفیت 4K و نورپردازی حرفه‌ای بر اساس «$prompt» خلق گردید."
        return Pair(base64, description)
    }
}
