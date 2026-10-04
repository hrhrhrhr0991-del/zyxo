package com.example.ui.screens.chat

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GeminiModelConstants
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.repository.AppLocalRepository
import com.example.data.repository.GeminiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class PersonaPreset(
    val name: String,
    val description: String,
    val systemPrompt: String,
    val defaultModel: String
)

val CHAT_PERSONAS = listOf(
    PersonaPreset(
        name = "دستیار جامع زیکسو (فوق‌العاده باهوش)",
        description = "پاسخ‌های عمیق، تحلیلی، دارای تیتربندی‌های شکیل و ایموجی‌های جذاب",
        systemPrompt = """
شما «زیکسو» (ZYXO) هستید؛ یک هوش مصنوعی نابغه، عمیق، دانشمند و مسلط به زبان فارسی با هوش تحلیلی بسیار بالا.

اصول کلیدی که حتماً باید در تمام پاسخ‌ها رعایت کنید:
۱. استفاده پرشور از تیترها و ایموجی‌ها: هر بخش یا موضوع را با یک تیتر جذاب دارای ایموجی آغاز کنید (مثلاً: 🎯 مقدمه و تعریف کلیدی، 💡 ایده‌ها و سناریوهای خلاق، ⚙️ جزئیات فنی و مراحل اقدام، 🚀 نتیجه‌گیری و گام بعدی).
۲. عمق و غنای پاسخ: پاسخ‌های سطحی ندهید! همواره تحلیل‌های دقیق، مثال‌های واقعی، مزایا و معایب، و نکات طلایی را توضیح دهید.
۳. دسته‌بندی و لیست‌بندی: از نشانه‌های بولت‌پوینت (*) یا شماره‌گذاری برای خوانایی بالا استفاده کنید.
۴. عبارات تاکیدی: کلمات کلیدی را **بولد** کنید تا اسکن چشمی متن لذت‌بخش باشد.
۵. لحن: بسیار محترم، دانا، پرانرژی و شیوا به زبان فارسی.
""".trimIndent(),
        defaultModel = GeminiModelConstants.GEMINI_3_5_FLASH
    ),
    PersonaPreset(
        name = "معمار ارشد نرم‌افزار و کدنویسی",
        description = "طراحی معماری نرم‌افزار، الگوریتم‌های بهینه و دیباگ تخصصی",
        systemPrompt = """
شما زیکسو، مهندس و معمار ارشد سیستم‌های نرم‌افزاری در سطح جهانی هستید.
- همیشه پاسخ‌ها را با تیترهای فنی دارای ایموجی (مانند 💻 معماری، ⚡ بهینه‌سازی، 🛠 کدهای نمونه) بخش‌بندی کنید.
- کدهای تمیز، کامل و مدرن را داخل بلاک‌های کد با ذکر زبان بنویسید.
- در کنار کد، چرایی انتخاب الگوها (Design Patterns)، عملکرد زمانی/مکانی و نکات امنیتی را شفاف توضیح دهید.
""".trimIndent(),
        defaultModel = GeminiModelConstants.GEMINI_3_1_PRO
    ),
    PersonaPreset(
        name = "نویسنده، ایده‌پرداز و مارکتینگ",
        description = "خلق داستان، کمپین‌های تبلیغاتی، سناریو و نگارش گیرا",
        systemPrompt = """
شما زیکسو، استاد نویسندگی خلاق و استراتژیست ارشد برندینگ و تبلیغات هستید.
- با کلماتی گیرا، سحرانگیز و ادبیات فاخر و جذاب فارسی بنویسید.
- از تیترهای مهیج با ایموجی‌های جذاب (✨ ایده بکر، 🎬 سناریوی جذاب، 📢 پیام اثرگذار) استفاده کنید.
- هوک‌های ذهنی (Hooks) و فراخوان به اقدام (Call to Action) کاربردی خلق کنید.
""".trimIndent(),
        defaultModel = GeminiModelConstants.GEMINI_3_5_FLASH
    ),
    PersonaPreset(
        name = "استاد و پژوهشگر علمی",
        description = "تدریس مفهومی گام‌به‌گام مباحث پیچیده ریاضی، فیزیک و فلسفه",
        systemPrompt = """
شما زیکسو، استاد برجسته دانشگاه و پژوهشگر ارشد علوم هستید.
- مباحث سخت و انتزاعی را با تشبیهات ملموس و شگفت‌انگیز باز کنید.
- از سرفصل‌های منظم با ایموجی (🔬 مبانی تئوری، 🧩 مثال شهودی، 📐 تحلیل ریاضی، 💡 نتیجه علمی) بهره ببرید.
""".trimIndent(),
        defaultModel = GeminiModelConstants.GEMINI_3_1_PRO
    ),
    PersonaPreset(
        name = "پاسخ سریع و بولت‌پوینتی",
        description = "پاسخ‌های فوق‌سریع و بدون حاشیه در قالب نکات طلایی",
        systemPrompt = """
شما زیکسو در حالت عملکرد سریع هستید.
- بدون تعارف و مقدمه‌چینی، مستقیماً پاسخ را در قالب نکات کلیدی، تیترهای کوتاه با ایموجی و بولت‌پوینت تحویل دهید.
""".trimIndent(),
        defaultModel = GeminiModelConstants.GEMINI_3_1_FLASH_LITE
    )
)

data class ChatUiState(
    val currentSessionId: String = "",
    val currentSessionTitle: String = "گفتگوی جدید",
    val selectedModel: String = GeminiModelConstants.GEMINI_3_5_FLASH,
    val selectedPersona: String = CHAT_PERSONAS[0].name,
    val systemPrompt: String = CHAT_PERSONAS[0].systemPrompt,
    val temperature: Float = 0.7f,
    val thinkingLevel: String = "high", // High thinking for deep reasoning
    val attachedImageBitmap: Bitmap? = null,
    val attachedImageBase64: String? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val isModelPickerOpen: Boolean = false,
    val isPersonaPickerOpen: Boolean = false,
    val isSessionsDrawerOpen: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val localRepo = AppLocalRepository(db)
    private val geminiRepo = GeminiRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    val sessions: StateFlow<List<ChatSessionEntity>> = localRepo.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activeGenerationJob: Job? = null

    init {
        initNewSession()
    }

    fun initNewSession() {
        activeGenerationJob?.cancel()
        val newSessionId = UUID.randomUUID().toString()
        val defaultPreset = CHAT_PERSONAS[0]

        _uiState.value = _uiState.value.copy(
            currentSessionId = newSessionId,
            currentSessionTitle = "گفتگوی جدید",
            selectedModel = defaultPreset.defaultModel,
            selectedPersona = defaultPreset.name,
            systemPrompt = defaultPreset.systemPrompt,
            attachedImageBitmap = null,
            attachedImageBase64 = null,
            isGenerating = false,
            errorMessage = null,
            isSessionsDrawerOpen = false
        )
        _messages.value = emptyList()

        viewModelScope.launch {
            localRepo.saveSession(
                ChatSessionEntity(
                    id = newSessionId,
                    title = "گفتگوی جدید",
                    modelName = defaultPreset.defaultModel,
                    systemPersona = defaultPreset.name,
                    systemPrompt = defaultPreset.systemPrompt
                )
            )
        }
    }

    fun switchSession(session: ChatSessionEntity) {
        activeGenerationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentSessionId = session.id,
            currentSessionTitle = session.title,
            selectedModel = session.modelName,
            selectedPersona = session.systemPersona,
            systemPrompt = session.systemPrompt,
            attachedImageBitmap = null,
            attachedImageBase64 = null,
            isGenerating = false,
            errorMessage = null,
            isSessionsDrawerOpen = false
        )

        viewModelScope.launch {
            localRepo.getMessagesForSession(session.id).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            localRepo.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                initNewSession()
            }
        }
    }

    fun setModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model, isModelPickerOpen = false)
        updateCurrentSessionHeader()
    }

    fun setPersona(preset: PersonaPreset) {
        _uiState.value = _uiState.value.copy(
            selectedPersona = preset.name,
            systemPrompt = preset.systemPrompt,
            selectedModel = preset.defaultModel,
            isPersonaPickerOpen = false
        )
        updateCurrentSessionHeader()
    }

    fun setCustomSystemPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(
            selectedPersona = "شخصی‌سازی‌شده",
            systemPrompt = prompt
        )
        updateCurrentSessionHeader()
    }

    fun setTemperature(temp: Float) {
        _uiState.value = _uiState.value.copy(temperature = temp)
    }

    fun setThinkingLevel(level: String) {
        _uiState.value = _uiState.value.copy(thinkingLevel = level)
    }

    fun attachImage(bitmap: Bitmap) {
        val base64 = geminiRepo.bitmapToBase64(bitmap)
        _uiState.value = _uiState.value.copy(
            attachedImageBitmap = bitmap,
            attachedImageBase64 = base64
        )
    }

    fun removeAttachment() {
        _uiState.value = _uiState.value.copy(
            attachedImageBitmap = null,
            attachedImageBase64 = null
        )
    }

    fun toggleModelPicker(open: Boolean) {
        _uiState.value = _uiState.value.copy(isModelPickerOpen = open)
    }

    fun togglePersonaPicker(open: Boolean) {
        _uiState.value = _uiState.value.copy(isPersonaPickerOpen = open)
    }

    fun toggleSessionsDrawer(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSessionsDrawerOpen = open)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _uiState.value = _uiState.value.copy(isGenerating = false)
        val lastMsg = _messages.value.lastOrNull()
        if (lastMsg != null && lastMsg.role == "model" && lastMsg.content.isNotBlank()) {
            viewModelScope.launch {
                localRepo.saveMessage(lastMsg)
            }
        }
    }

    private fun updateCurrentSessionHeader() {
        val currentState = _uiState.value
        viewModelScope.launch {
            localRepo.saveSession(
                ChatSessionEntity(
                    id = currentState.currentSessionId,
                    title = currentState.currentSessionTitle,
                    modelName = currentState.selectedModel,
                    systemPersona = currentState.selectedPersona,
                    systemPrompt = currentState.systemPrompt,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() && _uiState.value.attachedImageBase64 == null) return

        val sessionId = _uiState.value.currentSessionId
        val imageBase64 = _uiState.value.attachedImageBase64
        val currentModel = _uiState.value.selectedModel
        val systemPrompt = _uiState.value.systemPrompt
        val temperature = _uiState.value.temperature
        val thinkingLevel = _uiState.value.thinkingLevel

        // If this is the first message, update session title
        if (_messages.value.isEmpty()) {
            val autoTitle = if (userText.isNotBlank()) {
                if (userText.length > 30) userText.take(28) + "…" else userText
            } else "تحلیل تصویر"
            _uiState.value = _uiState.value.copy(currentSessionTitle = autoTitle)
        }

        val userMessage = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = userText,
            imageBase64 = imageBase64,
            modelName = currentModel,
            timestamp = System.currentTimeMillis()
        )

        // Clear input attachment and set generating state
        _uiState.value = _uiState.value.copy(
            attachedImageBitmap = null,
            attachedImageBase64 = null,
            isGenerating = true,
            errorMessage = null
        )

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            // Persist user message
            val insertedUserId = localRepo.saveMessage(userMessage)
            val persistedUserMsg = userMessage.copy(id = insertedUserId)
            _messages.value = _messages.value + persistedUserMsg

            // Add initial in-progress model message placeholder for ChatGPT typing effect
            val inProgressModelMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "model",
                content = "",
                modelName = currentModel,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + inProgressModelMsg

            // Prepare multi-turn history for Gemini API
            val apiContents = _messages.value.dropLast(1).map { msg ->
                val parts = mutableListOf<Part>()
                if (msg.content.isNotBlank()) {
                    parts.add(Part(text = msg.content))
                }
                if (!msg.imageBase64.isNullOrBlank()) {
                    parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = msg.imageBase64)))
                }
                Content(role = if (msg.role == "user") "user" else "model", parts = parts)
            }

            val accumulatedText = StringBuilder()

            val streamResult = geminiRepo.streamChatResponse(
                model = currentModel,
                messages = apiContents,
                systemInstructionText = systemPrompt,
                temperature = temperature,
                thinkingLevel = thinkingLevel
            ) { chunk ->
                accumulatedText.append(chunk)
                // Real-time update to Compose list
                _messages.value = _messages.value.mapIndexed { idx, msg ->
                    if (idx == _messages.value.lastIndex) {
                        msg.copy(content = accumulatedText.toString())
                    } else msg
                }
            }

            streamResult.fold(
                onSuccess = {
                    val finalContent = accumulatedText.toString().ifBlank { "پاسخی از مدل دریافت نشد." }
                    val finalModelMsg = inProgressModelMsg.copy(content = finalContent)
                    val insertedModelId = localRepo.saveMessage(finalModelMsg)
                    val persistedModelMsg = finalModelMsg.copy(id = insertedModelId)

                    _messages.value = _messages.value.mapIndexed { idx, msg ->
                        if (idx == _messages.value.lastIndex) persistedModelMsg else msg
                    }
                    _uiState.value = _uiState.value.copy(isGenerating = false)
                    updateCurrentSessionHeader()
                },
                onFailure = { error ->
                    if (accumulatedText.isNotEmpty()) {
                        val finalModelMsg = inProgressModelMsg.copy(content = accumulatedText.toString())
                        localRepo.saveMessage(finalModelMsg)
                        _uiState.value = _uiState.value.copy(isGenerating = false)
                    } else {
                        val errorText = error.message ?: "خطا در ارتباط با سرور هوش مصنوعی"
                        val errorMessage = inProgressModelMsg.copy(
                            content = "⚠️ $errorText\n\n💡 اگر در ایران هستید، حتماً فیلترشکن (VPN) خود را روشن کنید تا تحریم گوگل برطرف شود و مجدداً تلاش کنید.",
                            isError = true
                        )
                        viewModelScope.launch {
                            val insertedErrorId = localRepo.saveMessage(errorMessage)
                            val persistedErrorMsg = errorMessage.copy(id = insertedErrorId)
                            _messages.value = _messages.value.mapIndexed { idx, msg ->
                                if (idx == _messages.value.lastIndex) persistedErrorMsg else msg
                            }
                            _uiState.value = _uiState.value.copy(
                                isGenerating = false,
                                errorMessage = error.message
                            )
                        }
                    }
                }
            )
        }
    }
}
