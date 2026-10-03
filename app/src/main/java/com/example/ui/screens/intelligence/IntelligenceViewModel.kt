package com.example.ui.screens.intelligence

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GeminiModelConstants
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class IntelligenceTool(
    val title: String,
    val description: String,
    val model: String,
    val systemPrompt: String
) {
    CODE_ARCHITECT(
        title = "معماری، بهینه‌سازی و رفع باگ کد",
        description = "بررسی عمیق کدها، شناسایی آسیب‌پذیری‌ها، ریفکتور تمیز و رفع خطاها.",
        model = GeminiModelConstants.GEMINI_3_1_PRO,
        systemPrompt = "شما یک مهندس ارشد و حسابرس امنیتی نرم‌افزار هستید. کد ارائه شده را به دقت بررسی کنید و در پاسخ: ۱) تحلیل و علت مشکلات، ۲) آسیب‌پذیری‌ها و الگوهای ضدطراحی، ۳) نسخه تمیز و بازنویسی شده به همراه توضیحات فارسی را خروجی دهید."
    ),
    DOCUMENT_SYNTHESIZER(
        title = "خلاصه‌سازی و چکیده هوشمند اسناد",
        description = "تهیه خلاصه مدیریتی، استخراج ارکان کلیدی و فهرست اقدامات لازم از متن‌های طولانی.",
        model = GeminiModelConstants.GEMINI_3_5_FLASH,
        systemPrompt = "شما یک تحلیل‌گر برجسته محتوا هستید. متن ورودی را بررسی کرده و: ۱) خلاصه جامع در ۲ تا ۳ جمله، ۲) نکات و محورهای استراتژیک کلیدی، ۳) اقدامات پیشنهادی بعدی را به زبان فارسی زیبا خروجی دهید."
    ),
    RAPID_COPYWRITER(
        title = "بازنویسی سریع و تولید متن تبلیغاتی",
        description = "ویرایش لحن ایمیل، جملات گیرا، متن تبلیغاتی و شعارهای جذاب با سرعت فوق‌العاده.",
        model = GeminiModelConstants.GEMINI_3_1_FLASH_LITE,
        systemPrompt = "شما یک کپی‌رایتر حرفه‌ای هستید. برای متن ورودی ۳ نسخه متفاوت تولید کنید: ۱) رسمی و حرفه‌ای، ۲) جذاب و متقاعدکننده، ۳) کوتاه و نکته‌ای."
    ),
    VISION_DIAGNOSTIC(
        title = "تحلیل و عیب‌یابی چندوجهی تصویر",
        description = "تشخیص اشیا، تحلیل نمودارها، خواندن متن و تصویر اسکرین‌شات ارورها.",
        model = GeminiModelConstants.GEMINI_3_5_FLASH,
        systemPrompt = "شما یک مدل بینایی ماشین هوشمند هستید. تصویر ارسالی را به دقت تحلیل کرده و جزئیات بصری، دیاگرام‌ها، نمودارها یا خطاهای موجود در تصویر را به زبان فارسی شیوا تشریح کنید."
    )
}

data class IntelligenceUiState(
    val selectedTool: IntelligenceTool = IntelligenceTool.CODE_ARCHITECT,
    val inputText: String = "",
    val attachedImageBitmap: Bitmap? = null,
    val attachedImageBase64: String? = null,
    val outputResult: String? = null,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null
)

class IntelligenceViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiRepo = GeminiRepository()

    private val _uiState = MutableStateFlow(IntelligenceUiState())
    val uiState: StateFlow<IntelligenceUiState> = _uiState.asStateFlow()

    fun selectTool(tool: IntelligenceTool) {
        _uiState.value = _uiState.value.copy(
            selectedTool = tool,
            outputResult = null,
            errorMessage = null
        )
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun attachImage(bitmap: Bitmap) {
        val base64 = geminiRepo.bitmapToBase64(bitmap)
        _uiState.value = _uiState.value.copy(
            attachedImageBitmap = bitmap,
            attachedImageBase64 = base64,
            selectedTool = IntelligenceTool.VISION_DIAGNOSTIC
        )
    }

    fun removeAttachment() {
        _uiState.value = _uiState.value.copy(
            attachedImageBitmap = null,
            attachedImageBase64 = null
        )
    }

    fun runIntelligenceTask() {
        val current = _uiState.value
        if (current.inputText.isBlank() && current.attachedImageBase64 == null) return

        _uiState.value = _uiState.value.copy(
            isAnalyzing = true,
            outputResult = null,
            errorMessage = null
        )

        viewModelScope.launch {
            val parts = mutableListOf<Part>()
            if (current.inputText.isNotBlank()) {
                parts.add(Part(text = current.inputText))
            }
            if (!current.attachedImageBase64.isNullOrBlank()) {
                parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = current.attachedImageBase64)))
            }

            val result = geminiRepo.generateChatResponse(
                model = current.selectedTool.model,
                messages = listOf(Content(role = "user", parts = parts)),
                systemInstructionText = current.selectedTool.systemPrompt,
                temperature = if (current.selectedTool == IntelligenceTool.CODE_ARCHITECT) 0.2f else 0.7f
            )

            result.onSuccess { responseText ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    outputResult = responseText
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = error.message ?: "عملیات با خطا مواجه شد."
                )
            }
        }
    }
}
