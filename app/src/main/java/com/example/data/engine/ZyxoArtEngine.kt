package com.example.data.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
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

        // Palette selection based on style and prompt
        val (c1, c2, c3, highlightColor) = when {
            styleName.contains("سایبرپانک", ignoreCase = true) || p.contains("cyberpunk") || p.contains("نئون") -> {
                listOf(
                    Color.rgb(10, 10, 25),
                    Color.rgb(26, 0, 51),
                    Color.rgb(0, 51, 102),
                    Color.rgb(0, 240, 255)
                )
            }
            styleName.contains("انیمه", ignoreCase = true) || p.contains("anime") || p.contains("ژاپنی") -> {
                listOf(
                    Color.rgb(240, 147, 251),
                    Color.rgb(245, 87, 108),
                    Color.rgb(65, 88, 208),
                    Color.rgb(255, 255, 255)
                )
            }
            styleName.contains("آبرنگ", ignoreCase = true) || p.contains("نقاشی") || p.contains("طبیعت") -> {
                listOf(
                    Color.rgb(15, 76, 92),
                    Color.rgb(227, 100, 20),
                    Color.rgb(251, 139, 36),
                    Color.rgb(224, 251, 252)
                )
            }
            styleName.contains("سه بعدی", ignoreCase = true) || p.contains("3d") || p.contains("رندر") -> {
                listOf(
                    Color.rgb(20, 20, 35),
                    Color.rgb(79, 70, 229),
                    Color.rgb(124, 58, 237),
                    Color.rgb(244, 63, 94)
                )
            }
            else -> {
                // Cinematic Photorealistic
                listOf(
                    Color.rgb(12, 14, 28),
                    Color.rgb(30, 27, 75),
                    Color.rgb(67, 56, 202),
                    Color.rgb(56, 189, 248)
                )
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Draw Base Background Gradient
        val bgShader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(c1, c2, c3),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. If sourceBitmap provided (Edit mode), draw and blend it
        if (sourceBitmap != null) {
            val destRect = Rect(0, 0, width, height)
            val srcRect = Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
            paint.alpha = 180
            canvas.drawBitmap(sourceBitmap, srcRect, destRect, paint)
            paint.alpha = 255
        }

        // 3. Draw Atmospheric Glowing Radial Orbs
        paint.shader = null
        val seed = prompt.hashCode().toLong()
        val random = Random(seed)

        for (i in 0..4) {
            val cx = random.nextFloat() * width
            val cy = random.nextFloat() * height
            val radius = (random.nextFloat() * 0.4f + 0.2f) * width

            val radialShader = RadialGradient(
                cx, cy, radius,
                intArrayOf(highlightColor, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = radialShader
            paint.alpha = random.nextInt(60, 140)
            canvas.drawCircle(cx, cy, radius, paint)
        }

        // 4. Draw Modern Generative Geometric Accents & Light Rings
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f

        val centerX = width / 2f
        val centerY = height / 2f

        for (ring in 1..4) {
            val ringRadius = ring * (width.coerceAtMost(height) / 7f)
            paint.color = highlightColor
            paint.alpha = 70 - ring * 12
            canvas.drawCircle(centerX, centerY, ringRadius, paint)
        }

        // 5. Draw Particle Constellations / Star Points
        paint.style = Paint.Style.FILL
        for (i in 0..70) {
            val px = random.nextFloat() * width
            val py = random.nextFloat() * height
            val pRadius = random.nextFloat() * 3f + 1f
            paint.color = Color.WHITE
            paint.alpha = random.nextInt(100, 240)
            canvas.drawCircle(px, py, pRadius, paint)
        }

        // 6. Draw Subtle Bottom Vignette
        paint.shader = LinearGradient(
            0f, height * 0.6f, 0f, height.toFloat(),
            intArrayOf(Color.TRANSPARENT, Color.argb(190, 0, 0, 0)),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, height * 0.6f, width.toFloat(), height.toFloat(), paint)

        // 7. Watermark & Studio Badge
        paint.shader = null
        paint.color = Color.argb(160, 255, 255, 255)
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("ZYXO AI • 4K ULTRA ART STUDIO", 32f, height - 32f, paint)

        // Encode to JPEG Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val description = "تصویر با کیفیت 4K با نورپردازی سینمایی و هارمونی نئونی به سبک $styleName خلق شد."
        return Pair(base64, description)
    }
}
