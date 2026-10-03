package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.api.GeminiModelConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        GeneratedImageEntity::class,
        GeneratedVideoEntity::class,
        SavedPromptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun galleryDao(): GalleryDao
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nova_gemini_studio.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { seedInitialPrompts(it.promptDao()) }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialPrompts(promptDao: PromptDao) {
            val presets = listOf(
                SavedPromptEntity(
                    title = "بازبینی و بهینه‌سازی کد",
                    prompt = "این قطعه کد را از نظر خوانایی، کارایی، الگوهای تمیز و رفع باگ‌های احتمالی بررسی و نسخه بهینه‌شده آن را ارائه بده:",
                    category = "هوشمندی",
                    recommendedModel = GeminiModelConstants.GEMINI_3_1_PRO,
                    isPreset = true
                ),
                SavedPromptEntity(
                    title = "شهر سایبرپانک نئونی",
                    prompt = "تصویری سینمایی و فوق‌العاده باکیفیت از یک کلان‌شهر آینده‌نگرانه در شب بارانی با بازتاب نورهای نئون فیروزه‌ای و بنفش روی آسفالت خیس، کیفیت 8K",
                    category = "تصویر",
                    recommendedModel = GeminiModelConstants.GEMINI_3_PRO_IMAGE,
                    isPreset = true
                ),
                SavedPromptEntity(
                    title = "پرواز هلی‌شات بر فراز کوهستان",
                    prompt = "حرکت دوربین سینمایی به دور قلعه‌ای باستانی بر فراز قله‌های مه‌آلود آلپ هنگام غروب طلایی خورشید",
                    category = "ویدیو",
                    recommendedModel = GeminiModelConstants.VEO_3_1_FAST,
                    isPreset = true
                ),
                SavedPromptEntity(
                    title = "خلاصه اجرایی و مدیریتی",
                    prompt = "متن زیر را در قالب ۳ رکن استراتژیک کلیدی به همراه نکات مهم اجرایی و مراحل اقدام خلاصه کن:",
                    category = "هوشمندی",
                    recommendedModel = GeminiModelConstants.GEMINI_3_5_FLASH,
                    isPreset = true
                ),
                SavedPromptEntity(
                    title = "کاراکتر سه‌بعدی کریستالی",
                    prompt = "طراحی سه‌بعدی فانتزی از ربات کاوشگر که گوی نورانی در دست دارد، متریال شیشه‌ای و بازتاب‌های طیف نور، رندر استودیویی نرم و جذاب",
                    category = "تصویر",
                    recommendedModel = GeminiModelConstants.GEMINI_3_1_FLASH_IMAGE,
                    isPreset = true
                ),
                SavedPromptEntity(
                    title = "تدریس مفهومی فیزیک کوانتوم",
                    prompt = "به عنوان استاد برجسته فیزیک، پدیده درهم‌تنیدگی کوانتومی را با مثال‌های شهودی و ساده گام‌به‌گام توضیح بده.",
                    category = "گفتگو",
                    recommendedModel = GeminiModelConstants.GEMINI_3_1_PRO,
                    isPreset = true
                )
            )
            promptDao.insertPrompts(presets)
        }
    }
}
