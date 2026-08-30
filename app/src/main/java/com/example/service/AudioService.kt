package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AudioService(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingText = MutableStateFlow<String?>(null)
    val currentPlayingText: StateFlow<String?> = _currentPlayingText.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                try {
                    if (status == TextToSpeech.SUCCESS) {
                        isInitialized = true
                        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isPlaying.value = true
                            }

                            override fun onDone(utteranceId: String?) {
                                _isPlaying.value = false
                                _currentPlayingText.value = null
                            }

                            override fun onError(utteranceId: String?) {
                                _isPlaying.value = false
                                _currentPlayingText.value = null
                            }
                        })
                    } else {
                        Log.e("AudioService", "TTS Initialization failed")
                    }
                } catch (e: Exception) {
                    Log.e("AudioService", "Error during TTS listener setup: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioService", "Failed to create TextToSpeech instance: ${e.message}", e)
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized || tts == null) return

        val locale = when (language) {
            AppLanguage.MR -> Locale("mr", "IN")
            AppLanguage.HI -> Locale("hi", "IN")
            AppLanguage.GU -> Locale("gu", "IN")
            AppLanguage.PA -> Locale("pa", "IN")
            AppLanguage.BN -> Locale("bn", "IN")
            AppLanguage.TE -> Locale("te", "IN")
            AppLanguage.TA -> Locale("ta", "IN")
            AppLanguage.KN -> Locale("kn", "IN")
            AppLanguage.ML -> Locale("ml", "IN")
            AppLanguage.OR -> Locale("or", "IN")
            AppLanguage.AS -> Locale("as", "IN")
            AppLanguage.UR -> Locale("ur", "IN")
            AppLanguage.SA -> Locale("sa", "IN")
            AppLanguage.BHO -> Locale("bho", "IN")
            AppLanguage.MAI -> Locale("mai", "IN")
            AppLanguage.NE -> Locale("ne", "NP")
            AppLanguage.SD -> Locale("sd", "IN")
            AppLanguage.DOI -> Locale("doi", "IN")
            AppLanguage.KOK -> Locale("kok", "IN")
            AppLanguage.KS -> Locale("ks", "IN")
            AppLanguage.SAT -> Locale("sat", "IN")
            AppLanguage.ES -> Locale("es", "ES")
            AppLanguage.FR -> Locale("fr", "FR")
            AppLanguage.AR -> Locale("ar", "SA")
            AppLanguage.EN -> Locale.ENGLISH
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to Hindi or English if specific locale is missing on low-end device
            tts?.setLanguage(Locale("hi", "IN"))
        }

        tts?.setSpeechRate(0.9f) // Slightly slower rate for rural farmers' clarity
        tts?.setPitch(1.0f)

        _currentPlayingText.value = text
        _isPlaying.value = true

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "KisanVaniUtterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _currentPlayingText.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
