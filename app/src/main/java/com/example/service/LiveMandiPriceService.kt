package com.example.service

import com.example.data.model.AppLanguage
import com.example.data.model.CropItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveMandiTickerItem(
    val id: String,
    val cropName: String,
    val mandiName: String,
    val currentPrice: Int,
    val deltaInr: Int,
    val percentChange: Double,
    val timestamp: String,
    val arrivalSpikeQuintals: Int
)

data class LiveMarketMonitorState(
    val isDemoBenchmark: Boolean = true,
    val lastSyncTimestamp: String = "Benchmark Directory",
    val totalLiveCommodities: Int = 20,
    val liveTickerFeed: List<LiveMandiTickerItem> = listOf(
        LiveMandiTickerItem("t1", "Onion (Red)", "Lasalgaon APMC", 2850, +120, +6.4, "Benchmark", 12500),
        LiveMandiTickerItem("t2", "Cotton (Shankar-6)", "Akola APMC", 7450, +180, +3.2, "Benchmark", 8200),
        LiveMandiTickerItem("t3", "Soybean (Yellow)", "Latur APMC", 4680, -40, -1.8, "Benchmark", 15400),
        LiveMandiTickerItem("t4", "Tomato (Hybrid)", "Pimpalgaon APMC", 2100, +250, +11.2, "Benchmark", 6700),
        LiveMandiTickerItem("t5", "Tur (Pigeon Pea)", "Solapur APMC", 10450, +150, +2.1, "Benchmark", 4300)
    ),
    val topGainerCrop: String = "Onion (Lasalgaon)",
    val topGainerPct: Double = +6.4
)

class LiveMandiPriceService {

    private val _monitorState = MutableStateFlow(LiveMarketMonitorState())
    val monitorState: StateFlow<LiveMarketMonitorState> = _monitorState.asStateFlow()

    fun startLiveMonitoring() {
        // No simulated jitter loops; static APMC benchmarks retained
    }

    fun stopLiveMonitoring() {
        // No-op
    }

    suspend fun performManualRefresh(currentCrops: List<CropItem>): List<CropItem> {
        // Returns benchmark crop directory without random price jitter
        return currentCrops
    }

    fun generateSpokenPriceBulletin(crops: List<CropItem>, lang: AppLanguage): String {
        val top3 = crops.take(4)
        return when (lang) {
            AppLanguage.MR -> {
                val items = top3.joinToString("। ") {
                    "${it.nameMr} ${it.mandiName} येथे ₹${it.currentPrice} प्रति क्विंटल"
                }
                "किसान वाणी कृषी उत्पन्न बाजार समिती दर: $items. अधिक माहितीसाठी पिकावर टॅप करा."
            }
            AppLanguage.HI -> {
                val items = top3.joinToString("। ") {
                    "${it.nameHi} ${it.mandiName} में ₹${it.currentPrice} प्रति क्विंटल"
                }
                "किसान वाणी कृषि उपज मंडी भाव: $items. अधिक विवरण के लिए फसल पर टैप करें।"
            }
            AppLanguage.GU -> {
                val items = top3.joinToString("। ") {
                    "${it.nameGu} ${it.mandiName} માં ₹${it.currentPrice} પ્રતિ ક્વિન્ટલ"
                }
                "કિસાન વાણી માર્કેટ યાર્ડ ભાવ: $items."
            }
            AppLanguage.EN -> {
                val items = top3.joinToString(". ") {
                    "${it.nameEn} at ${it.mandiName} is ₹${it.currentPrice}/qtl"
                }
                "Kisan Vani APMC Market Summary: $items. Tap any crop for price details and trend chart."
            }
            else -> {
                val items = top3.joinToString("। ") {
                    "${it.nameHi} ${it.mandiName} में ₹${it.currentPrice} प्रति क्विंटल"
                }
                "किसान वाणी कृषि उपज मंडी भाव: $items."
            }
        }
    }
}
