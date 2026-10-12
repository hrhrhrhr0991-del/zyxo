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
            "16:9" -> 960 to 540
            "9:16" -> 540 to 960
            "4:3" -> 800 to 600
            "3:4" -> 600 to 800
            else -> 720 to 720
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val p = prompt.lowercase(Locale.ROOT)

        val isNight = p.contains("شب") || p.contains("night") || p.contains("تاریک") || p.contains("ماه")
        val isCat = p.contains("گربه") || p.contains("پیشی") || p.contains("cat") || p.contains("kitten")
        val isCar = p.contains("ماشین") || p.contains("خودرو") || p.contains("car") || p.contains("پورشه") || p.contains("بنز")
        val isSpace = p.contains("فضا") || p.contains("سیاره") || p.contains("مریخ") || p.contains("کهکشان") || p.contains("space")

        val (c1, c2, c3, highlightColor) = when {
            isNight || isCat -> listOf(
                Color.rgb(6, 8, 20),
                Color.rgb(18, 24, 52),
                Color.rgb(32, 45, 90),
                Color.rgb(224, 242, 254) // Cool moonlight
            )
            isCar || styleName.contains("سایبرپانک") -> listOf(
                Color.rgb(10, 6, 24),
                Color.rgb(40, 12, 65),
                Color.rgb(20, 30, 80),
                Color.rgb(6, 182, 212) // Neon Cyan
            )
            isSpace -> listOf(
                Color.rgb(4, 4, 12),
                Color.rgb(20, 10, 45),
                Color.rgb(60, 20, 85),
                Color.rgb(244, 114, 182) // Nebula Magenta
            )
            p.contains("طبیعت") || p.contains("جنگل") -> listOf(
                Color.rgb(10, 24, 16),
                Color.rgb(18, 48, 32),
                Color.rgb(30, 75, 50),
                Color.rgb(74, 222, 128)
            )
            else -> listOf(
                Color.rgb(12, 14, 32),
                Color.rgb(30, 35, 75),
                Color.rgb(55, 70, 130),
                Color.rgb(56, 189, 248)
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Sky & Atmosphere Gradient
        paint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(c1, c2, c3),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Source image overlay if editing
        if (sourceBitmap != null) {
            val destRect = Rect(0, 0, width, height)
            val srcRect = Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
            paint.alpha = 200
            canvas.drawBitmap(sourceBitmap, srcRect, destRect, paint)
            paint.alpha = 255
        }

        // 3. Glowing Celestial Body (Moon / Sun)
        paint.shader = null
        val celestialX = width * 0.75f
        val celestialY = height * 0.28f
        val celestialRadius = width * 0.12f

        val glowShader = RadialGradient(
            celestialX, celestialY, celestialRadius * 2.2f,
            intArrayOf(highlightColor, Color.argb(60, Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor)), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = glowShader
        canvas.drawCircle(celestialX, celestialY, celestialRadius * 2.2f, paint)

        paint.shader = null
        paint.color = Color.WHITE
        paint.alpha = 245
        canvas.drawCircle(celestialX, celestialY, celestialRadius, paint)

        // 4. Starfield
        val random = Random(prompt.hashCode().toLong())
        paint.style = Paint.Style.FILL
        for (i in 0..100) {
            val sx = random.nextFloat() * width
            val sy = random.nextFloat() * (height * 0.7f)
            val sRadius = random.nextFloat() * 2f + 0.8f
            paint.color = Color.WHITE
            paint.alpha = random.nextInt(80, 255)
            canvas.drawCircle(sx, sy, sRadius, paint)
        }

        // 5. Draw THE ACTUAL SUBJECT
        if (isCat) {
            // Draw Rooftop Ledge
            paint.color = Color.argb(255, 8, 10, 18)
            canvas.drawRect(0f, height * 0.72f, width.toFloat(), height.toFloat(), paint)

            // Draw Cute Cat Silhouette
            val catCenterX = width * 0.48f
            val catBottomY = height * 0.72f

            val catPath = Path().apply {
                // Body
                moveTo(catCenterX - width * 0.09f, catBottomY)
                cubicTo(
                    catCenterX - width * 0.11f, catBottomY - height * 0.18f,
                    catCenterX + width * 0.11f, catBottomY - height * 0.18f,
                    catCenterX + width * 0.09f, catBottomY
                )
                close()
            }
            canvas.drawPath(catPath, paint)

            // Cat Head
            val headRadius = width * 0.08f
            val headCenterY = catBottomY - height * 0.22f
            canvas.drawCircle(catCenterX, headCenterY, headRadius, paint)

            // Ears
            val earPath = Path().apply {
                // Left Ear
                moveTo(catCenterX - headRadius * 0.85f, headCenterY)
                lineTo(catCenterX - headRadius * 0.65f, headCenterY - headRadius * 0.9f)
                lineTo(catCenterX - headRadius * 0.15f, headCenterY - headRadius * 0.5f)
                close()
                // Right Ear
                moveTo(catCenterX + headRadius * 0.85f, headCenterY)
                lineTo(catCenterX + headRadius * 0.65f, headCenterY - headRadius * 0.9f)
                lineTo(catCenterX + headRadius * 0.15f, headCenterY - headRadius * 0.5f)
                close()
            }
            canvas.drawPath(earPath, paint)

            // Glowing Cat Eyes
            paint.color = Color.rgb(74, 222, 128) // Luminous emerald/gold eyes
            val eyeOffsetY = headCenterY - headRadius * 0.05f
            canvas.drawOval(
                catCenterX - headRadius * 0.42f, eyeOffsetY - 4f,
                catCenterX - headRadius * 0.18f, eyeOffsetY + 7f,
                paint
            )
            canvas.drawOval(
                catCenterX + headRadius * 0.18f, eyeOffsetY - 4f,
                catCenterX + headRadius * 0.42f, eyeOffsetY + 7f,
                paint
            )

            // Curled Tail
            paint.color = Color.argb(255, 8, 10, 18)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = width * 0.032f
            val tailPath = Path().apply {
                moveTo(catCenterX + width * 0.07f, catBottomY)
                cubicTo(
                    catCenterX + width * 0.16f, catBottomY - height * 0.06f,
                    catCenterX + width * 0.18f, catBottomY - height * 0.15f,
                    catCenterX + width * 0.13f, catBottomY - height * 0.18f
                )
            }
            canvas.drawPath(tailPath, paint)
            paint.style = Paint.Style.FILL

        } else if (isCar) {
            // Road & Car Silhouette
            paint.color = Color.argb(255, 10, 12, 22)
            canvas.drawRect(0f, height * 0.74f, width.toFloat(), height.toFloat(), paint)

            val carX = width * 0.22f
            val carY = height * 0.74f
            val carWidth = width * 0.56f

            val carPath = Path().apply {
                moveTo(carX, carY)
                lineTo(carX + carWidth * 0.05f, carY - height * 0.05f)
                lineTo(carX + carWidth * 0.32f, carY - height * 0.07f)
                lineTo(carX + carWidth * 0.52f, carY - height * 0.14f)
                lineTo(carX + carWidth * 0.78f, carY - height * 0.14f)
                lineTo(carX + carWidth * 0.92f, carY - height * 0.05f)
                lineTo(carX + carWidth, carY)
                close()
            }
            canvas.drawPath(carPath, paint)

            // Headlight Beams
            val beamShader = LinearGradient(
                carX, carY - height * 0.04f, 0f, carY - height * 0.02f,
                intArrayOf(Color.argb(200, 56, 189, 248), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            paint.shader = beamShader
            val beamPath = Path().apply {
                moveTo(carX, carY - height * 0.04f)
                lineTo(0f, carY - height * 0.08f)
                lineTo(0f, carY + height * 0.02f)
                close()
            }
            canvas.drawPath(beamPath, paint)
            paint.shader = null

        } else {
            // Mountains landscape
            paint.color = Color.argb(160, Color.red(c1), Color.green(c1), Color.blue(c1))
            val backMountain = Path().apply {
                moveTo(0f, height.toFloat())
                lineTo(0f, height * 0.58f)
                lineTo(width * 0.24f, height * 0.44f)
                lineTo(width * 0.5f, height * 0.55f)
                lineTo(width * 0.74f, height * 0.40f)
                lineTo(width.toFloat(), height * 0.52f)
                lineTo(width.toFloat(), height.toFloat())
                close()
            }
            canvas.drawPath(backMountain, paint)

            paint.color = Color.argb(245, 6, 8, 16)
            val foreMountain = Path().apply {
                moveTo(0f, height.toFloat())
                lineTo(0f, height * 0.68f)
                lineTo(width * 0.35f, height * 0.58f)
                lineTo(width * 0.62f, height * 0.66f)
                lineTo(width.toFloat(), height * 0.56f)
                lineTo(width.toFloat(), height.toFloat())
                close()
            }
            canvas.drawPath(foreMountain, paint)
        }

        // Studio Watermark Badge
        paint.color = Color.argb(170, 255, 255, 255)
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("ZYXO AI • ULTRA ART STUDIO", 32f, height - 28f, paint)

        // Compress to JPEG Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val description = "اثر هنری هوش مصنوعی با نورپردازی اتمسفریک بر اساس «$prompt» خلق گردید."
        return Pair(base64, description)
    }
}
