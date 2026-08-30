package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.LanguageManager
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.ui.theme.*

@Composable
fun AiAssistantScreen(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    isNetworkAvailable: Boolean,
    onSendMessage: (String) -> Unit,
    currentLanguage: AppLanguage,
    isPlayingAudio: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onClearChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var isListeningVoice by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    fun startListening() {
        if (!isNetworkAvailable) {
            Toast.makeText(
                context,
                LanguageManager.getString("ai_network_required_msg", currentLanguage),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Voice recognition not available on this device", Toast.LENGTH_SHORT).show()
            val fallbackPrompt = when (currentLanguage) {
                AppLanguage.MR -> "कांद्याला पुढील आठवड्यात काय भाव राहील?"
                AppLanguage.HI -> "अगले हफ्ते प्याज के भाव क्या रहेंगे?"
                AppLanguage.GU -> "આવતા સપ્તાહે ડુંગળીના ભાવ શું રહેશે?"
                AppLanguage.EN -> "What is the price forecast for Onion?"
                else -> "अगले हफ्ते प्याज के भाव क्या रहेंगे?"
            }
            inputText = fallbackPrompt
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val langLocale = when (currentLanguage) {
                AppLanguage.MR -> "mr-IN"
                AppLanguage.HI -> "hi-IN"
                AppLanguage.GU -> "gu-IN"
                AppLanguage.EN -> "en-IN"
                AppLanguage.PA -> "pa-IN"
                AppLanguage.BN -> "bn-IN"
                AppLanguage.TE -> "te-IN"
                AppLanguage.TA -> "ta-IN"
                AppLanguage.KN -> "kn-IN"
                AppLanguage.ML -> "ml-IN"
                AppLanguage.OR -> "or-IN"
                AppLanguage.AS -> "as-IN"
                AppLanguage.UR -> "ur-IN"
                AppLanguage.SA -> "sa-IN"
                AppLanguage.BHO -> "bho-IN"
                AppLanguage.MAI -> "mai-IN"
                AppLanguage.NE -> "ne-NP"
                AppLanguage.SD -> "sd-IN"
                AppLanguage.DOI -> "doi-IN"
                AppLanguage.KOK -> "kok-IN"
                AppLanguage.KS -> "ks-IN"
                AppLanguage.SAT -> "sat-IN"
                AppLanguage.ES -> "es-ES"
                AppLanguage.FR -> "fr-FR"
                AppLanguage.AR -> "ar-SA"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langLocale)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langLocale)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListeningVoice = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListeningVoice = false
                }
                override fun onError(error: Int) {
                    isListeningVoice = false
                }
                override fun onResults(results: Bundle?) {
                    isListeningVoice = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        inputText = spokenText
                        if (isNetworkAvailable) {
                            onSendMessage(spokenText)
                        }
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        inputText = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
            isListeningVoice = true
        } catch (e: Exception) {
            isListeningVoice = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, "Audio permission needed for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Clean AI Assistant Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(KisanGreenPrimary, KisanGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌾", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = LanguageManager.getString("ai_title", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KisanGreenPrimary
                        )
                        Text(
                            text = LanguageManager.getString("ai_subtitle", currentLanguage),
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Live Network Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNetworkAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                            .border(
                                1.dp,
                                if (isNetworkAvailable) Color(0xFF16A34A) else Color(0xFFEF4444),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isNetworkAvailable) Color(0xFF16A34A) else Color(0xFFDC2626))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isNetworkAvailable) "Online" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNetworkAvailable) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = LanguageManager.getString("ai_clear_chat", currentLanguage),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Offline Network Notification Banner
        if (!isNetworkAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageManager.getString("ai_network_required_msg", currentLanguage),
                        fontSize = 11.5.sp,
                        color = Color(0xFF991B1B),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                SimpleAiChatBubble(
                    message = msg,
                    currentLanguage = currentLanguage,
                    isPlayingAudio = isPlayingAudio,
                    onPlayAudio = onPlayAudio,
                    onStopAudio = onStopAudio,
                    onCopyText = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (isThinking) {
                item {
                    SimpleAiThinkingCard(currentLanguage = currentLanguage)
                }
            }
        }

        // Bottom Input Area & Quick Suggestions
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Voice Listening Pulsing Banner
                AnimatedVisibility(
                    visible = isListeningVoice,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = KisanSaffronContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(KisanSaffron)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎙️ Listening... बोला / बोलिए (${currentLanguage.displayName})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanSaffron
                            )
                        }
                    }
                }

                // Quick Question Suggestions Chips
                val quickSuggestions = when (currentLanguage) {
                    AppLanguage.MR -> listOf(
                        "🧅 कांदा बाजार भाव" to "कांद्याला पुढील काही दिवसांत काय भाव राहील?",
                        "🌱 सोयाबीन खोडकीड उपाय" to "सोयाबीनवरील खोडकीड व पिवळा मोझॅक रोगावर काय उपाय करावा?",
                        "🍅 टोमॅटो करपा नियंत्रण" to "टोमॅटो करपा (ब्लाईट) रोगावर कोणती औषध फवारणी करावी?",
                        "🏛️ महाडीबीटी सिंचन योजना" to "महाडीबीटी ८०% ठिबक सिंचन योजनेचा लाभ कसा घ्यावा?"
                    )
                    AppLanguage.HI -> listOf(
                        "🧅 प्याज मंडी भाव" to "अगले कुछ दिनों में प्याज के भाव क्या रहेंगे?",
                        "🌱 सोयाबीन कीट रोकथाम" to "सोयाबीन गर्डल बीटल और कीट नियंत्रण के लिए सही दवा बताएं।",
                        "🍅 टमाटर झुलसा रोग" to "टमाटर के झुलसा रोग के लिए कौन सा कीटनाशक छिड़कें?",
                        "🏛️ महाडीबीटी योजना" to "महाडीबीटी ड्रिप सिंचाई 80% सब्सिडी योजना की पात्रता क्या है?"
                    )
                    AppLanguage.GU -> listOf(
                        "🧅 ડુંગળી બજાર ભાવ" to "આગામી દિવસોમાં ડુંગળીના બજાર ભાવ કેવા રહેશે?",
                        "🌱 સોયાબીન રોગ નિયંત્રણ" to "સોયાબીનમાં જીવાત અને રોગ નિયંત્રણ માટે દવા જણાવો.",
                        "🍅 ટામેટાં રોગ નિયંત્રણ" to "ટામેટાંના રોગ માટે કઈ દવાનો છંટકાવ કરવો?",
                        "🏛️ સરકારી સબસિડી" to "ડ્રિપ ઇરિગેશન 80% સબસિડી કેવી રીતે મેળવવી?"
                    )
                    AppLanguage.EN -> listOf(
                        "🧅 Onion Price Forecast" to "What is the price forecast for Onion in Maharashtra mandis?",
                        "🌱 Soybean Pest Control" to "How to control stem fly and girdle beetle in Soybean?",
                        "🍅 Tomato Blight Rx" to "Best fungicide treatment for Tomato early and late blight?",
                        "🏛️ MahaDBT Drip Subsidy" to "How to apply for 80% MahaDBT drip irrigation subsidy?"
                    )
                    else -> listOf(
                        "🧅 प्याज मंडी भाव" to "अगले कुछ दिनों में प्याज के भाव क्या रहेंगे?",
                        "🌱 सोयाबीन कीट रोकथाम" to "सोयाबीन गर्डल बीटल और कीट नियंत्रण के लिए सही दवा बताएं।",
                        "🍅 टमाटर झुलसा रोग" to "टमाटर के झुलसा रोग के लिए कौन सा कीटनाशक छिड़कें?",
                        "🏛️ सरकारी योजना" to "ड्रिप सिंचाई 80% सब्सिडी योजना की पात्रता क्या है?"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSuggestions.forEach { (label, prompt) ->
                        SuggestionChip(
                            onClick = {
                                if (isNetworkAvailable) {
                                    onSendMessage(prompt)
                                } else {
                                    Toast.makeText(
                                        context,
                                        LanguageManager.getString("ai_network_required_msg", currentLanguage),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            label = { Text(text = label, fontSize = 11.sp, maxLines = 1) },
                            shape = RoundedCornerShape(16.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = KisanGreenContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Input Box + Voice Mic + Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isListeningVoice) {
                                speechRecognizer?.stopListening()
                                isListeningVoice = false
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .scale(if (isListeningVoice) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(if (isListeningVoice) Color.Red else KisanSaffron)
                            .testTag("voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListeningVoice) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = LanguageManager.getString("ask_input_hint", currentLanguage),
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                if (isNetworkAvailable) {
                                    val text = inputText
                                    inputText = ""
                                    onSendMessage(text)
                                } else {
                                    Toast.makeText(
                                        context,
                                        LanguageManager.getString("ai_network_required_msg", currentLanguage),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && isNetworkAvailable) KisanGreenPrimary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("send_ai_message_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && isNetworkAvailable) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleAiChatBubble(
    message: ChatMessage,
    currentLanguage: AppLanguage,
    isPlayingAudio: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onCopyText: (String) -> Unit
) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(KisanGreenPrimary, KisanGreenDark))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌾", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 310.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) KisanGreenPrimary else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isUser) 0.dp else 2.dp,
                shadowElevation = if (isUser) 0.dp else 1.dp,
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, KisanGreenPrimary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.messageText,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )

                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Krishi Vani AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenPrimary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Audio Speak / Stop Button
                                IconButton(
                                    onClick = {
                                        if (isPlayingAudio) {
                                            onStopAudio()
                                        } else {
                                            onPlayAudio(message.messageText)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingAudio) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Speak",
                                        tint = if (isPlayingAudio) Color.Red else KisanGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Copy to Clipboard Button
                                IconButton(
                                    onClick = { onCopyText(message.messageText) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.timestamp,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(KisanSaffronContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👨‍🌾", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SimpleAiThinkingCard(currentLanguage: AppLanguage) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_dots")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(KisanGreenPrimary, KisanGreenDark))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌾", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, KisanGreenPrimary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("ai_thinking_hint", currentLanguage),
                    fontSize = 12.5.sp,
                    color = KisanGreenPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(KisanGreenPrimary.copy(alpha = dotAlpha1))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(KisanGreenPrimary.copy(alpha = dotAlpha2))
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(KisanGreenPrimary.copy(alpha = dotAlpha3))
                    )
                }
            }
        }
    }
}
