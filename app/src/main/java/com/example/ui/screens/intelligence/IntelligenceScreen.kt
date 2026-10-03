package com.example.ui.screens.intelligence

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ModelBadge
import com.example.ui.components.ShimmerPlaceholder
import com.example.ui.components.StudioHeader
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.RoseSpark
import kotlinx.coroutines.launch

@Composable
fun IntelligenceScreen(
    viewModel: IntelligenceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.attachImage(bitmap)
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
                    title = "ZYXO | ابزارهای هوشمندی",
                    subtitle = "تحلیل محتوا، تصحیح کد، کپی‌رایتینگ و بینایی ماشین",
                    badgeText = "زیکسو AI"
                )
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
            // Tool Selection Horizontal Row
            Text(
                text = "ابزارهای تخصصی هوش مصنوعی",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntelligenceTool.entries.forEach { tool ->
                    val isSelected = uiState.selectedTool == tool
                    val icon: ImageVector = when (tool) {
                        IntelligenceTool.CODE_ARCHITECT -> Icons.Default.Code
                        IntelligenceTool.DOCUMENT_SYNTHESIZER -> Icons.Default.Description
                        IntelligenceTool.RAPID_COPYWRITER -> Icons.Default.FlashOn
                        IntelligenceTool.VISION_DIAGNOSTIC -> Icons.Default.Visibility
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) CyanSpark.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CyanSpark else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectTool(tool) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) CyanSpark else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tool.title.substringBefore("،").substringBefore("و").trim(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanSpark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Tool Details & Model Badge
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
                            text = uiState.selectedTool.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        ModelBadge(modelName = uiState.selectedTool.model)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.selectedTool.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Box
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
                            text = "ورودی داده یا متن",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "پیوست تصویر برای تحلیل بینایی",
                                tint = if (uiState.attachedImageBitmap != null) CyanSpark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Attached image thumbnail if any
                    if (uiState.attachedImageBitmap != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = uiState.attachedImageBitmap!!.asImageBitmap(),
                                contentDescription = "تصویر پیوست‌شده",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { viewModel.removeAttachment() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val placeholderText = when (uiState.selectedTool) {
                        IntelligenceTool.CODE_ARCHITECT -> "کد، اسکریپت یا معماری مورد نظر را اینجا جای‌گذاری کنید…"
                        IntelligenceTool.DOCUMENT_SYNTHESIZER -> "مقاله، متن سخنرانی، گزارش یا سند طولانی را اینجا قرار دهید…"
                        IntelligenceTool.RAPID_COPYWRITER -> "متن اولیه، پیش‌نویس ایمیل یا پیام خود را بنویسید…"
                        IntelligenceTool.VISION_DIAGNOSTIC -> "تصویر را پیوست کرده و سوال یا هدف خود را بپرسید…"
                    }

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        placeholder = { Text(placeholderText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("intelligence_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanSpark,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        minLines = 4,
                        maxLines = 10,
                        textStyle = if (uiState.selectedTool == IntelligenceTool.CODE_ARCHITECT)
                            MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                        else MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Execute Button
            Button(
                onClick = { viewModel.runIntelligenceTask() },
                enabled = !uiState.isAnalyzing && (uiState.inputText.isNotBlank() || uiState.attachedImageBitmap != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("run_intelligence_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanSpark)
            ) {
                if (uiState.isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "در حال تحلیل و پردازش هوشمند…",
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
                        text = "اجرای ${uiState.selectedTool.title.substringBefore("،").substringBefore("و").trim()}",
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

            // Output Display Card
            if (uiState.isAnalyzing) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            } else if (uiState.outputResult != null) {
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
                                text = "نتیجه و خروجی تحلیل هوشمند",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanSpark
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(uiState.outputResult!!))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("نتیجه در کلیپ‌بورد کپی شد")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "کپی خروجی",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.outputResult!!,
                                style = if (uiState.selectedTool == IntelligenceTool.CODE_ARCHITECT)
                                    MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                                else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
