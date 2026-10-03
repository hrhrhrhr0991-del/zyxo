package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface GalleryDao {
    @Query("SELECT * FROM generated_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<GeneratedImageEntity>>

    @Query("SELECT * FROM generated_images WHERE id = :id LIMIT 1")
    suspend fun getImageById(id: Long): GeneratedImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GeneratedImageEntity): Long

    @Update
    suspend fun updateImage(image: GeneratedImageEntity)

    @Delete
    suspend fun deleteImage(image: GeneratedImageEntity)

    @Query("SELECT * FROM generated_videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<GeneratedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: GeneratedVideoEntity): Long

    @Update
    suspend fun updateVideo(video: GeneratedVideoEntity)

    @Delete
    suspend fun deleteVideo(video: GeneratedVideoEntity)
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM saved_prompts ORDER BY id ASC")
    fun getAllPrompts(): Flow<List<SavedPromptEntity>>

    @Query("SELECT * FROM saved_prompts WHERE category = :category ORDER BY id ASC")
    fun getPromptsByCategory(category: String): Flow<List<SavedPromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: SavedPromptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<SavedPromptEntity>)

    @Delete
    suspend fun deletePrompt(prompt: SavedPromptEntity)

    @Query("SELECT COUNT(*) FROM saved_prompts")
    suspend fun getPromptCount(): Int
}
