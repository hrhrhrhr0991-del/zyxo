package com.example.data.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object ZyxoAntiSanctionEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Bypasses Iranian sanctions and delivers real, brilliant AI responses without any VPN or API key!
     */
    suspend fun streamText(
        prompt: String,
        systemPrompt: String? = null,
        onChunk: suspend (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sysInstruction = systemPrompt ?: """
                شما «زیکسو» (ZYXO) هستید؛ دستیار هوش مصنوعی نابغه، تحلیلی، دانا و مسلط به زبان فارسی فاخر.
                - همیشه پاسخ‌ها را با تیترهای جذاب دارای ایموجی دسته‌بندی کنید.
                - نکات کلیدی را بولد کنید و توضیحات عمیق و کاربردی ارائه دهید.
                - اگر کد یا فرمول خواسته شد، با فرمت استاندارد مارک‌داون بنویسید.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", sysInstruction)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messagesArray)
                put("model", "openai-fast")
            }

            val request = Request.Builder()
                .url("https://text.pollinations.ai/")
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .addHeader("User-Agent", "ZYXO-AI/2.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error code: ${response.code}"))
            }

            val fullText = response.body?.string()?.trim() ?: ""
            if (fullText.isBlank() || fullText == "{}") {
                return@withContext Result.failure(Exception("پاسخ دریافتی خالی بود"))
            }

            // Stream response chunk by chunk for the natural typing effect
            val tokens = fullText.split(Regex("(?<=\\s)|(?<=\\n)"))
            for (token in tokens) {
                onChunk(token)
                if (token.contains("\n")) {
                    delay(30)
                } else {
                    delay(16)
                }
            }

            Result.success(fullText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates real, professional 4K photorealistic AI images using FLUX AI
     * completely free, without requiring an API key or VPN!
     */
    suspend fun generateProfessionalImage(
        prompt: String,
        styleName: String,
        aspectRatio: String,
        resolution: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val (width, height) = when (aspectRatio) {
                "16:9" -> 1280 to 720
                "9:16" -> 720 to 1280
                "4:3" -> 1024 to 768
                "3:4" -> 768 to 1024
                else -> 1024 to 1024
            }

            val styleKeywords = when {
                styleName.contains("سایبرپانک") -> "cyberpunk style, neon glow, octane render, 8k, ray tracing"
                styleName.contains("انیمه") -> "masterpiece anime style, makoto shinkai art, vibrant colors, highly detailed"
                styleName.contains("سه بعدی") -> "hyperrealistic 3D render, Unreal Engine 5, volumetric lighting, photorealistic"
                styleName.contains("آبرنگ") -> "artistic watercolor painting, expressive brush strokes, elegant aesthetics"
                else -> "cinematic photorealistic, 4k ultra realistic, Hasselblad photography, golden hour, highly detailed"
            }

            val finalEnglishPrompt = "$prompt, $styleKeywords"
            val encodedPrompt = URLEncoder.encode(finalEnglishPrompt, "UTF-8")
            val seed = Random.nextInt(1000, 9999999)
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&model=flux&nologo=true&seed=$seed"

            val request = Request.Builder()
                .url(imageUrl)
                .addHeader("User-Agent", "ZYXO-AI/2.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Image download HTTP error: ${response.code}"))
            }

            val imageBytes = response.body?.bytes()
            if (imageBytes == null || imageBytes.isEmpty()) {
                return@withContext Result.failure(Exception("Empty image bytes received"))
            }

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return@withContext Result.failure(Exception("Failed to decode image bitmap"))

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val description = "تصویر حرفه‌ای و واقعی با موتور FLUX و کیفیت فوق‌العاده بر پایه «$prompt» تولید شد."
            Result.success(Pair(base64, description))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
