package com.example.ui.screens.image

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiModelConstants
import com.example.ui.components.AspectRatioSelector
import com.example.ui.components.ResolutionSelector
import com.example.ui.components.ShimmerPlaceholder
import com.example.ui.components.StudioHeader
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.RoseSpark
import com.example.ui.theme.VioletGlow
import kotlinx.coroutines.launch

@Composable
fun ImageStudioScreen(
    viewModel: ImageStudioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.setSourceImage(bitmap)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                StudioHeader(
                    title = "ZYXO | استودیو تصویر",
                    subtitle = if (uiState.mode == ImageStudioMode.EDIT) "ویرایش و تبدیل هوشمند تصویر" else "خلق اثر هنری 4K با زیکسو",
                    badgeText = "زیکسو 4K"
                )

                // Mode Tabs
                TabRow(
                    selectedTabIndex = if (uiState.mode == ImageStudioMode.GENERATE) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CyanSpark,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (uiState.mode == ImageStudioMode.GENERATE) 0 else 1]),
                            color = CyanSpark
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.mode == ImageStudioMode.GENERATE,
                        onClick = { viewModel.setMode(ImageStudioMode.GENERATE) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("خلق تصویر (4K Pro)", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = uiState.mode == ImageStudioMode.EDIT,
                        onClick = { viewModel.setMode(ImageStudioMode.EDIT) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ویرایش تصویر", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Edit Mode Source Image Uploader
            if (uiState.mode == ImageStudioMode.EDIT) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.sourceImageBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = uiState.sourceImageBitmap!!.asImageBitmap(),
                                    contentDescription = "تصویر مبدا",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { viewModel.removeSourceImage() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { photoPickerLauncher.launch("image/*") }
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(CyanSpark.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = CyanSpark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تصویری را برای ویرایش انتخاب کنید",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "جمینای ۳.۱ تصویر را بر اساس دستور شما تغییر خواهد داد",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Prompt Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.mode == ImageStudioMode.EDIT) "دستور ویرایش تصویر" else "توصیف و پرامپت اثر هنری",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Prompt Enhancer Spark Button
                        OutlinedButton(
                            onClick = { viewModel.enhancePrompt() },
                            enabled = !uiState.isEnhancingPrompt && uiState.promptText.isNotBlank(),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            if (uiState.isEnhancingPrompt) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VioletGlow, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بهینه‌سازی پرامپت", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.promptText,
                        onValueChange = { viewModel.setPrompt(it) },
                        placeholder = {
                            Text(
                                text = if (uiState.mode == ImageStudioMode.EDIT)
                                    "مثال: نورپردازی نئونی سایبرپانک و عینک آفتابی به سوژه اضافه کن…"
                                else
                                    "مثال: تصویری چشم‌نواز از پلنگی از جنس کریستال سیاه با مدارهای نورانی فیروزه‌ای در جنگلی مه‌آلود…"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanSpark,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            }

            // Model Selection
            Column {
                Text(
                    text = "مدل تولید تصویر",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val models = if (uiState.mode == ImageStudioMode.GENERATE) {
                        listOf(
                            Pair(GeminiModelConstants.GEMINI_3_PRO_IMAGE, "جمینای ۳ پرو تصویر (4K Pro)"),
                            Pair(GeminiModelConstants.GEMINI_3_1_FLASH_IMAGE, "جمینای ۳.۱ فلش تصویر"),
                            Pair(GeminiModelConstants.GEMINI_2_5_FLASH_IMAGE, "جمینای ۲.۵ فلش")
                        )
                    } else {
                        listOf(
                            Pair(GeminiModelConstants.GEMINI_3_1_FLASH_IMAGE, "جمینای ۳.۱ فلش (مخصوص ادیت)"),
                            Pair(GeminiModelConstants.GEMINI_2_5_FLASH_IMAGE, "جمینای ۲.۵ فلش")
                        )
                    }

                    models.forEach { (modelId, label) ->
                        val isSelected = uiState.selectedModel == modelId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) CyanSpark.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) CyanSpark else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setModel(modelId) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanSpark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Image Size / Resolution Affordance (1K, 2K, 4K, 512px)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "کیفیت و وضوح تصویر",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "پشتیبانی از 1K, 2K, 4K HD",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanSpark
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ResolutionSelector(
                    selectedResolution = uiState.selectedResolution,
                    onResolutionSelected = { viewModel.setResolution(it) },
                    resolutions = listOf("512px", "1K", "2K", "4K")
                )
            }

            // Aspect Ratio Selector
            Column {
                Text(
                    text = "نسبت ابعاد تصویر",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                AspectRatioSelector(
                    selectedRatio = uiState.selectedAspectRatio,
                    onRatioSelected = { viewModel.setAspectRatio(it) }
                )
            }

            // Style Presets
            Column {
                Text(
                    text = "سبک هنری و استایل",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IMAGE_STYLE_PRESETS.forEach { preset ->
                        val isSelected = uiState.selectedStyle.id == preset.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) VioletGlow.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) VioletGlow else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setStylePreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VioletGlow else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Generate Action Button
            Button(
                onClick = { viewModel.generateImage() },
                enabled = !uiState.isGenerating && uiState.promptText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_image_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanSpark)
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.mode == ImageStudioMode.EDIT) "در حال ویرایش تصویر…" else "در حال رندر تصویر ${uiState.selectedResolution}…",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.mode == ImageStudioMode.EDIT) "اعمال ویرایش روی تصویر" else "تولید شاهکار ${uiState.selectedResolution}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Error Banner
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoseSpark.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ ${uiState.errorMessage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoseSpark,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Output Display Canvas
            if (uiState.isGenerating) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else if (uiState.generatedImageBase64 != null) {
                val imageBitmap = try {
                    val bytes = android.util.Base64.decode(uiState.generatedImageBase64, android.util.Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    null
                }

                if (imageBitmap != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = imageBitmap.asImageBitmap(),
                                    contentDescription = "تصویر تولیدشده",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Action overlay buttons
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(uiState.promptText))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("پرامپت کپی شد")
                                            }
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "کپی پرامپت",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.setSourceImage(imageBitmap)
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ویرایش مجدد این تصویر",
                                            tint = CyanSpark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.selectedResolution} • ${uiState.selectedAspectRatio} • ذخیره در گالری",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanSpark
                                )
                                Text(
                                    text = uiState.selectedModel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
