package com.example.service

import com.example.data.model.AppLanguage
import com.example.data.model.ForecastDay
import com.example.data.model.WeatherAdvisory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AgriDistrict(
    val nameEn: String,
    val nameMr: String,
    val nameHi: String,
    val nameGu: String,
    val state: String = "Maharashtra",
    val latitude: Double,
    val longitude: Double,
    val primaryCrops: String
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> nameEn
        AppLanguage.MR -> nameMr
        AppLanguage.HI -> nameHi
        AppLanguage.GU -> nameGu
        else -> nameEn
    }
}

class LiveWeatherService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val availableDistricts = listOf(
        // Maharashtra
        AgriDistrict("Nashik", "नाशिक", "नासिक", "નાસિક", "Maharashtra", 20.00, 73.78, "Onion, Grapes, Tomato"),
        AgriDistrict("Pune", "पुणे", "पुणे", "પુણે", "Maharashtra", 18.52, 73.85, "Vegetables, Sugarcane, Flowers"),
        AgriDistrict("Latur", "लातूर", "लातूर", "લાતૂર", "Maharashtra", 18.40, 76.58, "Soybean, Tur (Pigeon Pea), Gram"),
        AgriDistrict("Ahmednagar", "अहिल्यानगर (अहमदनगर)", "अहमदनगर", "અહમદનગર", "Maharashtra", 19.09, 74.74, "Sugarcane, Onion, Bajra"),
        AgriDistrict("Nagpur", "नागपूर", "नागपुर", "નાગપુર", "Maharashtra", 21.14, 79.08, "Nagpur Orange, Cotton, Paddy"),
        AgriDistrict("Kolhapur", "कोल्हापूर", "कोल्हापुर", "કોલ્હાપુર", "Maharashtra", 16.70, 74.24, "Sugarcane, Jaggery, Rice"),
        AgriDistrict("Chhatrapati Sambhaji Nagar", "छत्रपती संभाजीनगर", "छत्रपति संभाजीनगर", "સંભાજીનગર", "Maharashtra", 19.87, 75.34, "Cotton, Maize, Sweet Lime"),
        AgriDistrict("Solapur", "सोलापूर", "सोलापुर", "સોલાપુર", "Maharashtra", 17.65, 75.90, "Pomegranate, Jowar, Tur"),
        AgriDistrict("Amravati", "अमरावती", "अमरावती", "અમરાવતી", "Maharashtra", 20.93, 77.75, "Soybean, Cotton, Orange"),
        AgriDistrict("Jalgaon", "जळगाव", "जलगांव", "જળગાંવ", "Maharashtra", 21.00, 75.56, "Banana, Cotton, Pulses"),
        // Punjab & Haryana
        AgriDistrict("Ludhiana", "लुधियाना", "लुधियाना", "લુધિયાણા", "Punjab", 30.90, 75.85, "Wheat, Paddy (Basmati), Maize"),
        AgriDistrict("Amritsar", "अमृतसर", "अमृतसर", "અમૃતસર", "Punjab", 31.63, 74.87, "Basmati Rice, Vegetables, Mustard"),
        AgriDistrict("Karnal", "करनाल", "करनाल", "કરનાલ", "Haryana", 29.68, 76.99, "Basmati Rice, Wheat, Dairy"),
        // Uttar Pradesh & Bihar
        AgriDistrict("Agra", "आगरा", "आगरा", "આગ્રા", "Uttar Pradesh", 27.18, 78.00, "Potato (Chipsona), Mustard, Bajra"),
        AgriDistrict("Varanasi", "वाराणसी", "वाराणसी", "વારાણસી", "Uttar Pradesh", 25.31, 82.97, "Vegetables, Rice, Mustard"),
        AgriDistrict("Purnea", "पूर्णिया", "पूर्णिया", "પૂર્ણિયા", "Bihar", 25.77, 87.47, "Maize, Jute, Makhana"),
        // Gujarat & Rajasthan
        AgriDistrict("Mehsana (Unjha)", "मेहसाणा (ऊंझा)", "मेहसाणा (ऊंझा)", "મહેસાણા (ઊંઝા)", "Gujarat", 23.60, 72.40, "Cumin (Jeera), Fennel, Mustard"),
        AgriDistrict("Rajkot (Gondal)", "राजकोट (गोंडल)", "राजकोट (गोंडल)", "રાજકોટ (ગોંડલ)", "Gujarat", 22.30, 70.80, "Groundnut, Cotton, Sesame"),
        AgriDistrict("Bharatpur", "भरतपुर", "भरतपुर", "ભરતપુર", "Rajasthan", 27.21, 77.49, "Mustard, Wheat, Bajra"),
        // Madhya Pradesh
        AgriDistrict("Indore", "इंदौर", "इंदौर", "ઇન્દોર", "Madhya Pradesh", 22.71, 75.85, "Soybean, Wheat, Garlic"),
        AgriDistrict("Neemuch", "नीमच", "नीमच", "નીમચ", "Madhya Pradesh", 24.47, 74.87, "Garlic, Coriander, Ashwagandha"),
        // South India
        AgriDistrict("Guntur", "गुंटूर", "गुंटूर", "ગુંટુર", "Andhra Pradesh", 16.30, 80.43, "Chilli (Teja), Cotton, Tobacco"),
        AgriDistrict("Shivamogga", "शिवमोग्गा", "शिवमोग्गा", "શિવમોગ્ગા", "Karnataka", 13.92, 75.56, "Arecanut, Paddy, Ginger"),
        AgriDistrict("Erode", "इरोड", "इरोड", "ઇરોડ", "Tamil Nadu", 11.34, 77.72, "Turmeric, Coconut, Sugarcane"),
        // East & North East
        AgriDistrict("Hooghly", "हुगली", "हुगली", "હૂગલી", "West Bengal", 22.90, 88.39, "Raw Jute, Paddy, Potato"),
        AgriDistrict("Bargarh", "बरगढ़", "बरगढ़", "બરગઢ", "Odisha", 21.33, 83.62, "Paddy (Swarna), Pulses, Vegetables"),
        AgriDistrict("Kamrup (Guwahati)", "कामरूप", "कामरूप", "કામરૂપ", "Assam", 26.14, 91.73, "Ginger, Black Rice, Tea")
    )

    suspend fun fetchLiveWeather(district: AgriDistrict): Result<WeatherAdvisory> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=${district.latitude}&longitude=${district.longitude}" +
                    "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                    "&timezone=Asia%2FKolkata"

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "KisanVani-Android/1.0 (Agri Weather Service)")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body?.string() ?: throw Exception("Empty response from live weather service")
            val json = JSONObject(body)

            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m").toInt()
            val humidity = current.getInt("relative_humidity_2m")
            val weatherCode = current.getInt("weather_code")
            val windSpeed = current.getDouble("wind_speed_10m").toInt()
            val precip = current.optDouble("precipitation", 0.0)

            val daily = json.getJSONObject("daily")
            val dailyCodes = daily.getJSONArray("weather_code")
            val maxTemps = daily.getJSONArray("temperature_2m_max")
            val minTemps = daily.getJSONArray("temperature_2m_min")
            val rainProbs = daily.optJSONArray("precipitation_probability_max")

            val currentRainProb = if (rainProbs != null && rainProbs.length() > 0) rainProbs.optInt(0, if (precip > 0) 80 else 15) else (if (precip > 0) 75 else 20)

            val conditionMapping = mapWmoCode(weatherCode)

            // Parse 5-day forecast
            val dayNames = listOf("Today", "Tomorrow", getRelativeDayName(2), getRelativeDayName(3), getRelativeDayName(4))
            val forecastList = mutableListOf<ForecastDay>()

            val count = minOf(5, dailyCodes.length())
            for (i in 0 until count) {
                val code = dailyCodes.getInt(i)
                val dayMap = mapWmoCode(code)
                val maxT = maxTemps.getDouble(i).toInt()
                val minT = minTemps.getDouble(i).toInt()
                val rainP = rainProbs?.optInt(i, if (code >= 51) 65 else 10) ?: (if (code >= 51) 60 else 10)

                forecastList.add(
                    ForecastDay(
                        dayName = dayNames.getOrElse(i) { "Day ${i + 1}" },
                        tempMax = maxT,
                        tempMin = minT,
                        rainProbPercent = rainP,
                        iconEmoji = dayMap.emoji,
                        condition = dayMap.en
                    )
                )
            }

            // Build Multilingual Spoken Advisories
            val bulletinEn = buildSpokenBulletin(
                district = district.nameEn,
                temp = temp,
                condition = conditionMapping.en,
                rainProb = currentRainProb,
                humidity = humidity,
                wind = windSpeed,
                lang = AppLanguage.EN
            )
            val bulletinMr = buildSpokenBulletin(
                district = district.nameMr,
                temp = temp,
                condition = conditionMapping.mr,
                rainProb = currentRainProb,
                humidity = humidity,
                wind = windSpeed,
                lang = AppLanguage.MR
            )
            val bulletinHi = buildSpokenBulletin(
                district = district.nameHi,
                temp = temp,
                condition = conditionMapping.hi,
                rainProb = currentRainProb,
                humidity = humidity,
                wind = windSpeed,
                lang = AppLanguage.HI
            )
            val bulletinGu = buildSpokenBulletin(
                district = district.nameGu,
                temp = temp,
                condition = conditionMapping.gu,
                rainProb = currentRainProb,
                humidity = humidity,
                wind = windSpeed,
                lang = AppLanguage.GU
            )

            val fetchTimestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
            val coordinateStr = "${String.format(Locale.ENGLISH, "%.2f", district.latitude)}° N, ${String.format(Locale.ENGLISH, "%.2f", district.longitude)}° E"

            val advisory = WeatherAdvisory(
                district = district.nameEn,
                tempCelsius = temp,
                conditionEn = conditionMapping.en,
                conditionMr = conditionMapping.mr,
                conditionHi = conditionMapping.hi,
                conditionGu = conditionMapping.gu,
                rainChancePercent = currentRainProb,
                humidityPercent = humidity,
                windKmh = windSpeed,
                audioBulletinEn = bulletinEn,
                audioBulletinMr = bulletinMr,
                audioBulletinHi = bulletinHi,
                audioBulletinGu = bulletinGu,
                forecast5Days = forecastList,
                dataSource = "Open-Meteo API",
                coordinates = coordinateStr,
                lastUpdated = fetchTimestamp,
                isLiveSuccess = true
            )

            Result.success(advisory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class WeatherConditionDesc(
        val en: String,
        val mr: String,
        val hi: String,
        val gu: String,
        val emoji: String
    )

    private fun mapWmoCode(code: Int): WeatherConditionDesc {
        return when (code) {
            0 -> WeatherConditionDesc("Clear Sky & Sunny", "निरभ्र व स्वच्छ सूर्यप्रकाश", "साफ़ आसमान व धूप", "ચોખ્ખું આકાશ અને તડકો", "☀️")
            1, 2 -> WeatherConditionDesc("Partly Cloudy", "अंशतः ढगाळ वातावरण", "आंशिक रूप से बादल", "ભાગ્યે વાદળછાયું", "⛅")
            3 -> WeatherConditionDesc("Overcast Sky", "पूर्णतः ढगाळ हवामान", "घने बादल", "સંપૂર્ણ વાદળછાયું", "☁️")
            45, 48 -> WeatherConditionDesc("Misty Foggy Weather", "धुके व थंड हवामान", "कोहरा व धुंध", "ઝાકળ અને ધુમ્મસ", "🌫️")
            51, 53, 55 -> WeatherConditionDesc("Light Drizzle", "हलक्या पावसाच्या सरी (रिमझिम)", "हल्की बूंदाबांदी", "હળવા વરસાદી છાંટા", "🌦️")
            61, 63 -> WeatherConditionDesc("Moderate Rain", "मध्यम स्वरूपाचा पाऊस", "मध्यम बारिश", "મધ્યમ વરસાદ", "🌧️")
            65 -> WeatherConditionDesc("Heavy Monsoon Rain", "मुसळधार पाऊस", "भारी बारिश", "ભારે વરસાદ", "🌧️")
            80, 81, 82 -> WeatherConditionDesc("Scattered Rain Showers", "विखुरलेल्या पावसाच्या जोरदार सरी", "रुक-रुक कर तेज बारिश", "વરસાદી ઝાપટાં", "🌧️")
            95, 96, 99 -> WeatherConditionDesc("Thunderstorm & Gusty Winds", "विजांच्या कडकडाटासह वादळी पाऊस", "गरज-चमक के साथ आंधी-तूफ़ान", "વીજળીના કડાકા સાથે વાવાઝોડું", "⛈️")
            else -> WeatherConditionDesc("Fair Weather", "अनुकूल हवामान", "सामान्य मौसम", "સામાન્ય હવામાન", "🌤️")
        }
    }

    private fun getRelativeDayName(offsetDays: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, offsetDays)
        val sdf = SimpleDateFormat("EEE (dd MMM)", Locale.ENGLISH)
        return sdf.format(cal.time)
    }

    private fun buildSpokenBulletin(
        district: String,
        temp: Int,
        condition: String,
        rainProb: Int,
        humidity: Int,
        wind: Int,
        lang: AppLanguage
    ): String {
        return when (lang) {
            AppLanguage.MR -> {
                val sprayAdvice = if (rainProb > 50) {
                    "पावसाची शक्यता ${rainProb} टक्के असल्याने आज औषध फवारणी पुढे ढकलावी."
                } else if (wind > 20) {
                    "वाऱ्याचा वेग ${wind} किमी प्रति तास असल्याने फवारणी करताना काळजी घ्यावी."
                } else {
                    "हवामान फवारणी व शेतीकामांसाठी अत्यंत अनुकूल आहे."
                }
                "किसान वाणी थेट हवामान वृत्त: $district येथे सध्याचे तापमान $temp अंश सेल्सिअस असून, $condition आहे. आर्द्रता $humidity टक्के आणि वाऱ्याचा वेग $wind किमी प्रति तास आहे. $sprayAdvice"
            }
            AppLanguage.HI -> {
                val sprayAdvice = if (rainProb > 50) {
                    "बारिश की संभावना ${rainProb} प्रतिशत होने के कारण कीटनाशक छिड़काव स्थगित रखें।"
                } else if (wind > 20) {
                    "हवा की गति ${wind} किमी/घंटा है, इसलिए सावधानी से काम लें।"
                } else {
                    "मौसम कृषि कार्यों एवं छिड़काव के लिए बहुत उपयुक्त है।"
                }
                "किसान वाणी लाइव मौसम बुलेटिन: $district में वर्तमान तापमान $temp डिग्री सेल्सियस है, मौसम $condition है। आर्द्रता $humidity% और हवा की गति $wind किमी/घंटा है। $sprayAdvice"
            }
            AppLanguage.GU -> {
                val sprayAdvice = if (rainProb > 50) {
                    "વરસાદની શક્યતા ${rainProb}% હોવાથી દવાનો છંટકાવ મોકૂફ રાખો."
                } else {
                    "હવામાન ખેતીકામ માટે અનુકૂળ છે."
                }
                "કિસાન વાણી લાઈવ હવામાન: $district માં તાપમાન $temp ડિગ્રી છે, વાતાવરણ $condition છે. ભેજ $humidity% અને પવનની ઝડપ $wind કિમી/કલાક છે. $sprayAdvice"
            }
            AppLanguage.EN -> {
                val sprayAdvice = if (rainProb > 50) {
                    "Rain probability is high at ${rainProb}%. Postpone foliar spraying."
                } else if (wind > 20) {
                    "Wind speed is ${wind} km/h; exercise drift caution during pesticide spraying."
                } else {
                    "Weather conditions are optimal for spraying, weeding, and intercultural field operations."
                }
                "Kisan Vani Live Weather Advisory for $district: Current temperature is $temp degrees Celsius with $condition. Humidity is $humidity% with wind speed at $wind km/h. $sprayAdvice"
            }
            else -> {
                val sprayAdvice = if (rainProb > 50) {
                    "बारिश की संभावना ${rainProb}% होने के कारण छिड़काव स्थगित रखें।"
                } else {
                    "मौसम कृषि कार्यों के लिए उपयुक्त है।"
                }
                "किसान वाणी लाइव मौसम बुलेटिन: $district में तापमान $temp°C है, मौसम $condition है। $sprayAdvice"
            }
        }
    }

    fun getDistrictsForState(state: String): List<AgriDistrict> {
        val stateDistrictsInList = availableDistricts.filter { it.state.equals(state, ignoreCase = true) }
        if (stateDistrictsInList.isNotEmpty()) {
            return stateDistrictsInList
        }
        val districtNames = com.example.data.model.STATE_DISTRICTS_MAP[state] ?: emptyList()
        return districtNames.map { name ->
            val geo = com.example.data.model.DISTRICT_GEO_COORDS[name] ?: Triple(20.0, 75.0, "Major Kharif & Rabi Crops")
            AgriDistrict(
                nameEn = name,
                nameMr = name,
                nameHi = name,
                nameGu = name,
                state = state,
                latitude = geo.first,
                longitude = geo.second,
                primaryCrops = geo.third
            )
        }
    }

    fun getDistrictForStateAndName(state: String, districtName: String? = null): AgriDistrict {
        val list = getDistrictsForState(state)
        if (!districtName.isNullOrBlank()) {
            val found = list.find { it.nameEn.equals(districtName, ignoreCase = true) }
            if (found != null) return found
            val geo = com.example.data.model.DISTRICT_GEO_COORDS[districtName]
            if (geo != null) {
                return AgriDistrict(
                    nameEn = districtName,
                    nameMr = districtName,
                    nameHi = districtName,
                    nameGu = districtName,
                    state = state,
                    latitude = geo.first,
                    longitude = geo.second,
                    primaryCrops = geo.third
                )
            }
        }
        return list.firstOrNull() ?: availableDistricts[0]
    }
}
