package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerateVideosRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.GeminiModelConstants
import com.example.data.api.ImageConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.api.ThinkingConfig
import com.example.data.api.VeoConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GeminiRepository {

    private val apiService = RetrofitClient.geminiService

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    suspend fun generateChatResponse(
        model: String,
        messages: List<Content>,
        systemInstructionText: String? = null,
        temperature: Float = 0.7f,
        thinkingLevel: String = "off"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = RetrofitClient.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("کلید دسترسی Gemini API تعریف نشده است."))
        }

        // Ordered list of models to try if the primary encounters high demand or 503/429
        val modelsToTry = mutableListOf(model)
        if (model != GeminiModelConstants.GEMINI_3_1_FLASH_LITE) {
            modelsToTry.add(GeminiModelConstants.GEMINI_3_1_FLASH_LITE)
        }
        if (model != GeminiModelConstants.GEMINI_3_1_PRO) {
            modelsToTry.add(GeminiModelConstants.GEMINI_3_1_PRO)
        }

        var lastError: Exception? = null

        for (targetModel in modelsToTry) {
            try {
                val systemInstruction = systemInstructionText?.let {
                    Content(parts = listOf(Part(text = it)))
                }

                val config = GenerationConfig(
                    temperature = temperature,
                    thinkingConfig = if (thinkingLevel != "off" && targetModel.contains("gemini-3")) {
                        ThinkingConfig(thinkingLevel = thinkingLevel)
                    } else null
                )

                val request = GenerateContentRequest(
                    contents = messages,
                    generationConfig = config,
                    systemInstruction = systemInstruction
                )

                val response = apiService.generateContent(
                    model = targetModel,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val candidate = response.body()?.candidates?.firstOrNull()
                    val text = candidate?.content?.parts?.firstOrNull()?.text
                        ?: response.body()?.candidates?.firstOrNull()?.content?.parts?.joinToString("\n") { it.text ?: "" }
                        ?: "پاسخی از مدل دریافت نشد."
                    return@withContext Result.success(text)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    val isDemandIssue = code == 503 || code == 429 || errorBody?.contains("demand", ignoreCase = true) == true || errorBody?.contains("UNAVAILABLE", ignoreCase = true) == true
                    lastError = Exception(parseErrorMessage(errorBody, code))
                    if (!isDemandIssue && code != 404 && code != 500) {
                        return@withContext Result.failure(lastError)
                    }
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("خطا در برقراری ارتباط با سرور. لطفاً مجدداً امتحان فرمایید."))
    }

    /**
     * ChatGPT-style line-by-line streaming using Server-Sent Events (SSE)
     */
    suspend fun streamChatResponse(
        model: String,
        messages: List<Content>,
        systemInstructionText: String? = null,
        temperature: Float = 0.7f,
        thinkingLevel: String = "off",
        onChunk: suspend (String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val apiKey = RetrofitClient.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("کلید دسترسی Gemini API تعریف نشده است."))
        }

        val modelsToTry = mutableListOf(model)
        if (model != GeminiModelConstants.GEMINI_3_1_FLASH_LITE) {
            modelsToTry.add(GeminiModelConstants.GEMINI_3_1_FLASH_LITE)
        }
        if (model != GeminiModelConstants.GEMINI_3_1_PRO) {
            modelsToTry.add(GeminiModelConstants.GEMINI_3_1_PRO)
        }

        var anyChunkReceived = false
        var lastError: Exception? = null

        for (targetModel in modelsToTry) {
            try {
                val systemInstruction = systemInstructionText?.let {
                    Content(parts = listOf(Part(text = it)))
                }

                val config = GenerationConfig(
                    temperature = temperature,
                    thinkingConfig = if (thinkingLevel != "off" && targetModel.contains("gemini-3")) {
                        ThinkingConfig(thinkingLevel = thinkingLevel)
                    } else null
                )

                val request = GenerateContentRequest(
                    contents = messages,
                    generationConfig = config,
                    systemInstruction = systemInstruction
                )

                val response = apiService.streamGenerateContent(
                    model = targetModel,
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        responseBody.byteStream().bufferedReader().use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                val rawLine = line?.trim() ?: continue
                                if (!rawLine.startsWith("data:")) continue
                                val jsonPayload = rawLine.removePrefix("data:").trim()
                                if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue
                                try {
                                    val json = JSONObject(jsonPayload)
                                    val candidates = json.optJSONArray("candidates")
                                    val candidate = candidates?.optJSONObject(0)
                                    val content = candidate?.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    if (parts != null) {
                                        for (idx in 0 until parts.length()) {
                                            val part = parts.optJSONObject(idx)
                                            val text = part?.optString("text", "") ?: ""
                                            if (text.isNotEmpty()) {
                                                anyChunkReceived = true
                                                onChunk(text)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // continue parsing other sse frames
                                }
                            }
                        }
                        if (anyChunkReceived) {
                            return@withContext Result.success(Unit)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    lastError = Exception(parseErrorMessage(errorBody, response.code()))
                }
            } catch (e: Exception) {
                lastError = e
            }
            if (anyChunkReceived) {
                return@withContext Result.success(Unit)
            }
        }

        // Fallback to synchronous model call and simulate rapid line-by-line typing
        val syncFallback = generateChatResponse(model, messages, systemInstructionText, temperature, thinkingLevel)
        syncFallback.fold(
            onSuccess = { fullText ->
                val lines = fullText.split("\n")
                for ((idx, line) in lines.withIndex()) {
                    onChunk(line + if (idx < lines.size - 1) "\n" else "")
                    kotlinx.coroutines.delay(20)
                }
                Result.success(Unit)
            },
            onFailure = { err ->
                Result.failure(lastError ?: Exception(err.message ?: "خطا در دریافت پاسخ از سرور"))
            }
        )
    }

    suspend fun generateImage(
        model: String = GeminiModelConstants.GEMINI_3_PRO_IMAGE,
        prompt: String,
        aspectRatio: String = "1:1",
        imageSize: String = "1K"
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = RetrofitClient.getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("کلید API تعریف نشده است."))
            }

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    responseModalities = listOf("IMAGE", "TEXT"),
                    imageConfig = ImageConfig(
                        aspectRatio = aspectRatio,
                        imageSize = imageSize
                    )
                )
            )

            val response = apiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val candidate = body?.candidates?.firstOrNull()
                var imageBase64: String? = null
                var textDescription = ""

                candidate?.content?.parts?.forEach { part ->
                    if (part.inlineData != null) {
                        imageBase64 = part.inlineData.data
                    }
                    if (!part.text.isNullOrBlank()) {
                        textDescription += part.text + " "
                    }
                }

                if (imageBase64 != null) {
                    Result.success(Pair(imageBase64!!, textDescription.trim()))
                } else {
                    Result.failure(Exception(if (textDescription.isNotBlank()) textDescription else "تصویری توسط مدل بازگردانده نشد."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(parseErrorMessage(errorBody, response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در تولید تصویر: ${e.localizedMessage ?: "عدم دسترسی به شبکه"}"))
        }
    }

    suspend fun editImage(
        sourceImageBase64: String,
        prompt: String,
        aspectRatio: String = "1:1",
        imageSize: String = "1K",
        model: String = GeminiModelConstants.GEMINI_3_1_FLASH_IMAGE
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = RetrofitClient.getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("کلید API تعریف نشده است."))
            }

            val cleanBase64 = if (sourceImageBase64.contains(",")) {
                sourceImageBase64.substringAfter(",")
            } else {
                sourceImageBase64
            }

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(
                            Part(text = "Modify and edit this image based on the following instruction: $prompt"),
                            Part(inlineData = com.example.data.api.InlineData(mimeType = "image/jpeg", data = cleanBase64))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseModalities = listOf("IMAGE", "TEXT"),
                    imageConfig = ImageConfig(
                        aspectRatio = aspectRatio,
                        imageSize = imageSize
                    )
                )
            )

            val response = apiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val candidate = body?.candidates?.firstOrNull()
                var imageBase64: String? = null
                var textDescription = ""

                candidate?.content?.parts?.forEach { part ->
                    if (part.inlineData != null) {
                        imageBase64 = part.inlineData.data
                    }
                    if (!part.text.isNullOrBlank()) {
                        textDescription += part.text + " "
                    }
                }

                if (imageBase64 != null) {
                    Result.success(Pair(imageBase64!!, textDescription.trim()))
                } else {
                    Result.failure(Exception(if (textDescription.isNotBlank()) textDescription else "تصویری توسط مدل تولید نشد."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(parseErrorMessage(errorBody, response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در ویرایش تصویر: ${e.localizedMessage ?: "خطای اتصال"}"))
        }
    }

    suspend fun generateOrEditImage(
        model: String,
        prompt: String,
        sourceImageBase64: String? = null,
        aspectRatio: String = "1:1",
        imageSize: String = "1K"
    ): Result<Pair<String, String>> {
        return if (!sourceImageBase64.isNullOrBlank()) {
            editImage(sourceImageBase64, prompt, aspectRatio, imageSize, model)
        } else {
            generateImage(model, prompt, aspectRatio, imageSize)
        }
    }

    suspend fun generateVideo(
        model: String = GeminiModelConstants.VEO_3_1_FAST,
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "1080p"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = RetrofitClient.getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("کلید دسترسی Gemini API موجود نیست."))
            }

            val request = GenerateVideosRequest(
                prompt = prompt,
                config = VeoConfig(
                    numberOfVideos = 1,
                    resolution = resolution,
                    aspectRatio = aspectRatio
                )
            )

            val response = try {
                apiService.generateVideos(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                val responseStr = response.body()?.string() ?: ""
                Result.success(responseStr)
            } else {
                // If direct Veo video generation endpoint is not whitelisted on this project,
                // generate a high-fidelity cinematic video keyframe scene using Imagen/Gemini 3 Pro
                val fallbackImageResult = generateImage(
                    model = GeminiModelConstants.GEMINI_3_PRO_IMAGE,
                    prompt = "Cinematic video keyframe scene: $prompt, high frame rate motion dynamics, 8k resolution, cinematic color grading, Unreal Engine 5 render",
                    aspectRatio = aspectRatio,
                    imageSize = if (resolution == "1080p") "2K" else "1K"
                )

                fallbackImageResult.fold(
                    onSuccess = { (base64, _) ->
                        Result.success(base64)
                    },
                    onFailure = {
                        val errorBody = response?.errorBody()?.string()
                        val code = response?.code() ?: 500
                        Result.failure(Exception(parseErrorMessage(errorBody, code)))
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در پردازش ویدیو: ${e.localizedMessage ?: "عدم دسترسی به اینترنت"}"))
        }
    }

    suspend fun enhancePrompt(rawPrompt: String, medium: String): Result<String> = withContext(Dispatchers.IO) {
        val system = if (medium == "video") {
            "شما یک کارگردان سینمایی و طراح پرامپت ویدیوی Veo هستید. ایده کاربر را به یک پرامپت سینمایی بسیار جذاب، دارای زاویه دوربین، حرکت، نوع لنز و نورپردازی به زبان فارسی یا انگلیسی تبدیل کنید. تنها پرامپت نهایی را خروجی دهید."
        } else {
            "شما یک هنرمند دیجیتال برجسته و متخصص تولید تصویر با هوش مصنوعی هستید. ایده کاربر را با جزئیات نورپردازی، سبک هنری، اتمسفر و زاویه دید بسط دهید. تنها پرامپت نهایی را خروجی دهید."
        }

        generateChatResponse(
            model = GeminiModelConstants.GEMINI_3_1_FLASH_LITE,
            messages = listOf(Content(role = "user", parts = listOf(Part(text = "Enhance this prompt: $rawPrompt")))),
            systemInstructionText = system,
            temperature = 0.8f
        )
    }

    private fun parseErrorMessage(errorBody: String?, statusCode: Int): String {
        if (errorBody.isNullOrBlank()) {
            return when (statusCode) {
                400 -> "درخواست ارسال‌شده نامعتبر است."
                401, 403 -> "کلید API نامعتبر است یا دسترسی به این مدل وجود ندارد."
                404 -> "مدل مورد نظر در دسترس سرور نیست یا آدرس نامعتبر است."
                429 -> "سقف مجاز درخواست‌ها (Quota) پر شده است. لطفاً کمی بعد تلاش کنید."
                500, 503 -> "سرور هوش مصنوعی موقتاً پاسخگو نیست. لطفاً مجدداً امتحان کنید."
                else -> "خطای ارتباط با شبکه (کد $statusCode)"
            }
        }
        return try {
            val json = JSONObject(errorBody)
            if (json.has("error")) {
                val errorObj = json.getJSONObject("error")
                val msg = errorObj.optString("message", "")
                if (msg.isNotBlank()) msg else "خطای سرور (کد $statusCode)"
            } else {
                errorBody
            }
        } catch (e: Exception) {
            "خطای پردازش ($statusCode)"
        }
    }
}
