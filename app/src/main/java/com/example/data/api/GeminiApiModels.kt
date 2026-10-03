package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object GeminiModelConstants {
    // Chat & Intelligence Models
    const val GEMINI_3_5_FLASH = "gemini-3.5-flash"
    const val GEMINI_3_1_PRO = "gemini-3.1-pro-preview"
    const val GEMINI_3_1_FLASH_LITE = "gemini-3.1-flash-lite-preview"

    // Image Models
    const val GEMINI_3_1_FLASH_IMAGE = "gemini-3.1-flash-image-preview"
    const val GEMINI_3_PRO_IMAGE = "gemini-3-pro-image-preview"
    const val GEMINI_2_5_FLASH_IMAGE = "gemini-2.5-flash-image"

    // Video Models
    const val VEO_3_1_FAST = "veo-3.1-fast-generate-preview"
    const val VEO_3_1_PRO = "veo-3.1-generate-preview"
}

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @field:Json(name = "contents") val contents: List<Content>,
    @field:Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @field:Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @field:Json(name = "role") val role: String? = null,
    @field:Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @field:Json(name = "text") val text: String? = null,
    @field:Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @field:Json(name = "mimeType") val mimeType: String,
    @field:Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @field:Json(name = "temperature") val temperature: Float? = null,
    @field:Json(name = "topP") val topP: Float? = null,
    @field:Json(name = "topK") val topK: Int? = null,
    @field:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @field:Json(name = "responseMimeType") val responseMimeType: String? = null,
    @field:Json(name = "imageConfig") val imageConfig: ImageConfig? = null,
    @field:Json(name = "thinkingConfig") val thinkingConfig: ThinkingConfig? = null,
    @field:Json(name = "responseModalities") val responseModalities: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ImageConfig(
    @field:Json(name = "aspectRatio") val aspectRatio: String? = null,
    @field:Json(name = "imageSize") val imageSize: String? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    @field:Json(name = "thinkingLevel") val thinkingLevel: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @field:Json(name = "candidates") val candidates: List<Candidate>? = null,
    @field:Json(name = "usageMetadata") val usageMetadata: UsageMetadata? = null,
    @field:Json(name = "error") val error: ApiError? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @field:Json(name = "content") val content: Content? = null,
    @field:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class UsageMetadata(
    @field:Json(name = "promptTokenCount") val promptTokenCount: Int? = null,
    @field:Json(name = "candidatesTokenCount") val candidatesTokenCount: Int? = null,
    @field:Json(name = "totalTokenCount") val totalTokenCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @field:Json(name = "code") val code: Int? = null,
    @field:Json(name = "message") val message: String? = null,
    @field:Json(name = "status") val status: String? = null
)

// Veo Video Request Models
@JsonClass(generateAdapter = true)
data class GenerateVideosRequest(
    @field:Json(name = "prompt") val prompt: String,
    @field:Json(name = "config") val config: VeoConfig? = null
)

@JsonClass(generateAdapter = true)
data class VeoConfig(
    @field:Json(name = "numberOfVideos") val numberOfVideos: Int = 1,
    @field:Json(name = "resolution") val resolution: String = "1080p",
    @field:Json(name = "aspectRatio") val aspectRatio: String = "16:9"
)
