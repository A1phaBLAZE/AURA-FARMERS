package com.example.data.model

/**
 * Single data point on the price history and forecast timeline.
 */
data class TrendDataPoint(
    val dateLabel: String,         // e.g. "10 Aug", "24 Aug (Today)", "05 Sep"
    val fullDate: String,          // e.g. "2026-08-24"
    val pricePerQuintal: Int,      // INR per Quintal
    val isPrediction: Boolean,     // false for official historical, true for AI forecast
    val volumeTonnes: Int = 0,     // Mandi arrival volume if available
    val sourceName: String = "AGMARKNET (Govt of India)"
)

/**
 * Expected directional movement for the 30-day forecast.
 */
enum class TrendDirection(
    val labelEn: String,
    val labelHi: String,
    val labelMr: String,
    val symbol: String,
    val colorHex: Long
) {
    RISING("Rising (Bullish)", "बढ़त (तेजी)", "वाढ (तेजी)", "↗", 0xFF16A34A),
    FALLING("Falling (Bearish)", "गिरावट (मंदी)", "घसरण (मंदी)", "↘", 0xFFDC2626),
    STABLE("Stable (Range-bound)", "स्थिर (समान)", "स्थिर (कायम)", "➔", 0xFFD97706);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI -> labelHi
        AppLanguage.MR -> labelMr
        else -> labelEn
    }
}

/**
 * Date range filter for trend visualization.
 */
enum class TrendDateRange(val days: Int, val labelEn: String, val labelHi: String, val labelMr: String) {
    DAYS_7(7, "7 Days", "7 दिन", "७ दिवस"),
    DAYS_15(15, "15 Days", "15 दिन", "१५ दिवस"),
    DAYS_30(30, "30 Days", "30 दिन", "३० दिवस"),
    SEASON_90(90, "Season (90D)", "पूरा सीजन (90D)", "संपूर्ण हंगाम (९०D)");

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI -> labelHi
        AppLanguage.MR -> labelMr
        else -> labelEn
    }
}

/**
 * Complete analysis & forecast entity for a crop.
 */
data class CropForecastAnalysis(
    val cropId: String,
    val cropNameEn: String,
    val cropNameHi: String,
    val cropNameMr: String,
    val cropNameGu: String,
    val category: CropCategory,
    val emoji: String,
    val mandiName: String,
    val district: String,
    val state: String = "Maharashtra",
    val currentOfficialPrice: Int,       // INR / Quintal
    val minOfficialPrice: Int,
    val maxOfficialPrice: Int,
    val modalOfficialPrice: Int,
    val arrivalVolumeQuintals: Int,
    val lastUpdatedTimestamp: String,
    val officialDataSource: String = "AGMARKNET (Govt of India)",
    val isOfficialSourceBacked: Boolean = true,
    val historicalPoints: List<TrendDataPoint>,
    val predictedPoints: List<TrendDataPoint>, // 30-day forecast points
    val predictedHighestPrice: Int,
    val predictedLowestPrice: Int,
    val predictedEndPrice: Int,
    val expectedDirection: TrendDirection,
    val percentChange30Days: Double,
    val forecastConfidencePercent: Int = 94,
    val optimalSellingWindow: String,
    val keyDrivingFactors: List<String>,
    val aiRationaleEn: String,
    val aiRationaleHi: String,
    val aiRationaleMr: String
) {
    fun getCropName(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI -> cropNameHi
        AppLanguage.MR -> cropNameMr
        AppLanguage.GU -> cropNameGu
        else -> cropNameEn
    }

    fun getAiRationale(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI -> aiRationaleHi
        AppLanguage.MR -> aiRationaleMr
        else -> aiRationaleEn
    }

    /**
     * Combined timeline: Historical official points followed by 30-day AI prediction points.
     */
    val combinedTimeline: List<TrendDataPoint>
        get() = historicalPoints + predictedPoints
}

fun PriceUnit.convertPrice(pricePerQuintal: Double): Double = pricePerQuintal * multiplierFromQuintal
fun PriceUnit.getSymbol(lang: AppLanguage): String = getLabel(lang)
fun CropCategory.getDisplayName(lang: AppLanguage): String = displayName

