package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val modelName: String,
    val systemPersona: String,
    val systemPrompt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user" or "model"
    val content: String,
    val imageBase64: String? = null,
    val modelName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val negativePrompt: String? = null,
    val imageBase64: String,
    val modelName: String,
    val resolution: String, // "512px", "1K", "2K", "4K"
    val aspectRatio: String, // "1:1", "16:9", "9:16", "4:3", "3:4"
    val stylePreset: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isEditedFromSource: Boolean = false
)

@Entity(tableName = "generated_videos")
data class GeneratedVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val enhancedPrompt: String? = null,
    val modelName: String,
    val aspectRatio: String, // "16:9", "9:16"
    val resolution: String, // "720p", "1080p"
    val operationName: String? = null,
    val videoUri: String? = null,
    val status: String, // "COMPLETED", "PROCESSING", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_prompts")
data class SavedPromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val prompt: String,
    val category: String, // "Chat", "Image", "Video", "Logic"
    val recommendedModel: String,
    val isPreset: Boolean = false
)
