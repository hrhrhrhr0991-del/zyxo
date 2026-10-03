package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.GeneratedImageEntity
import com.example.data.local.GeneratedVideoEntity
import com.example.data.local.SavedPromptEntity
import kotlinx.coroutines.flow.Flow

class AppLocalRepository(private val database: AppDatabase) {

    private val chatDao = database.chatDao()
    private val galleryDao = database.galleryDao()
    private val promptDao = database.promptDao()

    // Chat
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    suspend fun getSessionById(sessionId: String): ChatSessionEntity? = chatDao.getSessionById(sessionId)

    suspend fun saveSession(session: ChatSessionEntity) = chatDao.insertSession(session)

    suspend fun updateSession(session: ChatSessionEntity) = chatDao.updateSession(session)

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
        chatDao.deleteMessagesForSession(sessionId)
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun saveMessage(message: ChatMessageEntity): Long = chatDao.insertMessage(message)

    // Gallery
    fun getAllImages(): Flow<List<GeneratedImageEntity>> = galleryDao.getAllImages()

    suspend fun getImageById(id: Long): GeneratedImageEntity? = galleryDao.getImageById(id)

    suspend fun saveImage(image: GeneratedImageEntity): Long = galleryDao.insertImage(image)

    suspend fun updateImage(image: GeneratedImageEntity) = galleryDao.updateImage(image)

    suspend fun deleteImage(image: GeneratedImageEntity) = galleryDao.deleteImage(image)

    fun getAllVideos(): Flow<List<GeneratedVideoEntity>> = galleryDao.getAllVideos()

    suspend fun saveVideo(video: GeneratedVideoEntity): Long = galleryDao.insertVideo(video)

    suspend fun deleteVideo(video: GeneratedVideoEntity) = galleryDao.deleteVideo(video)

    // Prompts
    fun getAllPrompts(): Flow<List<SavedPromptEntity>> = promptDao.getAllPrompts()

    fun getPromptsByCategory(category: String): Flow<List<SavedPromptEntity>> =
        promptDao.getPromptsByCategory(category)

    suspend fun savePrompt(prompt: SavedPromptEntity): Long = promptDao.insertPrompt(prompt)

    suspend fun deletePrompt(prompt: SavedPromptEntity) = promptDao.deletePrompt(prompt)
}
