package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Normalized representation of an official AGMARKNET record from data.gov.in
 */
data class AgmarknetMandiRecord(
    val id: String,
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val variety: String,
    val grade: String,
    val arrivalDate: String, // Original string from govt record e.g. "26/08/2026"
    val arrivalTimestamp: Long, // Epoch ms for sorting
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val unit: String = "₹/Quintal"
)

/**
 * Filter parameters for the AGMARKNET data.gov.in resource
 */
data class AgmarknetFilters(
    val state: String = "Maharashtra",
    val district: String = "",
    val market: String = "",
    val commodity: String = "",
    val variety: String = "",
    val limit: Int = 100,
    val offset: Int = 0
) {
    fun hasActiveFilters(): Boolean {
        return state.isNotBlank() && state != "All" ||
                district.isNotBlank() ||
                market.isNotBlank() ||
                commodity.isNotBlank() ||
                variety.isNotBlank()
    }
}

/**
 * Parsed and validated response from data.gov.in AGMARKNET API
 */
data class AgmarknetResult(
    val records: List<AgmarknetMandiRecord>,
    val totalCount: Int,
    val limit: Int,
    val offset: Int,
    val fetchedAtIst: String,
    val isCached: Boolean = false,
    val isDelayed: Boolean = false,
    val delayMessage: String? = null
)

/**
 * UI State for the Live Mandi Prices feature
 */
data class AgmarknetUiState(
    val isLoading: Boolean = false,
    val records: List<AgmarknetMandiRecord> = emptyList(),
    val cachedFallbackRecords: List<AgmarknetMandiRecord> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val pageSize: Int = 50,
    val filters: AgmarknetFilters = AgmarknetFilters(),
    val errorMessage: String? = null,
    val isApiKeyConfigured: Boolean = true,
    val lastFetchedIst: String = "",
    val isDelayed: Boolean = false,
    val delayNotice: String? = null,
    val isUsingCache: Boolean = false,
    val cooldownSecondsRemaining: Int = 0
)

/**
 * Helper utilities for formatting and date parsing
 */
object AgmarknetDateUtils {
    private val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    )

    fun parseArrivalDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        val trimmed = dateStr.trim()
        for (format in dateFormats) {
            try {
                format.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                val parsed = format.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
                // Try next pattern
            }
        }
        return 0L
    }

    fun getCurrentIstTimestamp(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a 'IST'", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        return sdf.format(Date())
    }

    fun checkDataDelay(records: List<AgmarknetMandiRecord>): Pair<Boolean, String?> {
        if (records.isEmpty()) return Pair(false, null)
        val newestRecord = records.maxByOrNull { it.arrivalTimestamp } ?: return Pair(false, null)
        if (newestRecord.arrivalTimestamp <= 0L) return Pair(false, null)

        val nowMs = System.currentTimeMillis()
        val diffMs = nowMs - newestRecord.arrivalTimestamp
        val diffDays = diffMs / (1000L * 60 * 60 * 24)

        return if (diffDays > 2) {
            Pair(
                true,
                "Data may be delayed: Latest government record is from ${newestRecord.arrivalDate} (${diffDays} days ago). APMC markets may be closed on weekends/holidays or reporting on a slight processing buffer."
            )
        } else {
            Pair(false, null)
        }
    }
}
