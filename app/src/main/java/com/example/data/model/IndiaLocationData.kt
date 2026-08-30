package com.example.data.model

// Shared list of Indian States across Mandi & Weather modules
// Comprehensive list of all Indian States and Union Territories (28 States + 8 UTs)
val INDIAN_STATES = listOf(
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chhattisgarh",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
    "Andaman and Nicobar Islands",
    "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi (NCT)",
    "Jammu and Kashmir",
    "Ladakh",
    "Lakshadweep",
    "Puducherry"
)

// Comprehensive Mapping of Indian States & UTs to their Respective Agricultural Districts
val STATE_DISTRICTS_MAP: Map<String, List<String>> = mapOf(
    "Maharashtra" to listOf(
        "Nashik", "Pune", "Ahmednagar", "Solapur", "Latur", "Kolhapur", "Akola",
        "Nagpur", "Amravati", "Jalgaon", "Chhatrapati Sambhajinagar", "Jalna", "Beed",
        "Nanded", "Yavatmal", "Buldhana", "Satara", "Sangli", "Washim", "Parbhani",
        "Dharashiv (Osmanabad)", "Dhule", "Nandurbar", "Chandrapur", "Wardha", "Gondia",
        "Bhandara", "Gadchiroli", "Hingoli", "Palghar", "Thane", "Raigad", "Ratnagiri", "Sindhudurg", "Baramati"
    ),
    "Gujarat" to listOf(
        "Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Jamnagar", "Junagadh",
        "Gandhinagar", "Anand", "Bharuch", "Banaskantha", "Sabarkantha", "Mehsana",
        "Patan", "Kutch", "Amreli", "Porbandar", "Navsari", "Valsad", "Morbi",
        "Surendranagar", "Dahod", "Panchmahal", "Gir Somnath", "Botad", "Devbhumi Dwarka",
        "Aravalli", "Mahisagar", "Chhota Udaipur", "Tapi", "Narmada", "Dang"
    ),
    "Madhya Pradesh" to listOf(
        "Indore", "Bhopal", "Ujjain", "Jabalpur", "Gwalior", "Sagar", "Dewas", "Ratlam",
        "Mandsaur", "Neemuch", "Narmadapuram (Hoshangabad)", "Sehore", "Khargone", "Khandwa",
        "Dhar", "Chhindwara", "Rewa", "Satna", "Morena", "Vidisha", "Harda", "Betul",
        "Shajapur", "Rajgarh", "Raisen", "Guna", "Shivpuri", "Ashoknagar", "Damoh", "Panna",
        "Chhatarpur", "Tikamgarh", "Katni", "Narsinghpur", "Balaghat", "Seoni", "Mandla"
    ),
    "Rajasthan" to listOf(
        "Jaipur", "Jodhpur", "Kota", "Bikaner", "Ajmer", "Udaipur", "Alwar",
        "Sri Ganganagar", "Bharatpur", "Sikar", "Nagaur", "Hanumangarh", "Pali",
        "Bhilwara", "Tonk", "Chittorgarh", "Jhalawar", "Baran", "Bundi", "Dausa",
        "Jhunjhunu", "Churu", "Sawai Madhopur", "Karauli", "Dholpur", "Rajsamand",
        "Dungarpur", "Banswara", "Pratapgarh", "Sirohi", "Jalore", "Barmer", "Jaisalmer"
    ),
    "Punjab" to listOf(
        "Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda", "Sangrur",
        "Hoshiarpur", "Firozpur", "Mansa", "Moga", "Muktsar", "Fazilka", "Kapurthala",
        "Gurdaspur", "Barnala", "Fatehgarh Sahib", "Faridkot", "Rupnagar",
        "Shahid Bhagat Singh Nagar", "Tarn Taran", "Pathankot", "SAS Nagar (Mohali)", "Malerkotla"
    ),
    "Haryana" to listOf(
        "Karnal", "Kurukshetra", "Ambala", "Hisar", "Rohtak", "Sirsa", "Panipat",
        "Sonipat", "Jind", "Yamunanagar", "Kaithal", "Fatehabad", "Bhiwani", "Rewari",
        "Palwal", "Gurugram", "Faridabad", "Jhajjar", "Mahendragarh", "Charkhi Dadri", "Panchkula", "Nuh"
    ),
    "Uttar Pradesh" to listOf(
        "Lucknow", "Kanpur Nagar", "Agra", "Varanasi", "Prayagraj", "Meerut", "Bareilly",
        "Aligarh", "Moradabad", "Saharanpur", "Gorakhpur", "Jhansi", "Muzaffarnagar",
        "Mathura", "Bulandshahr", "Hardoi", "Lakhimpur Kheri", "Sitapur", "Barabanki",
        "Ayodhya", "Badaun", "Shahjahanpur", "Pilibhit", "Rampur", "Bijnor", "Sambhal",
        "Hapur", "Shamli", "Gautam Buddha Nagar", "Ghaziabad", "Firozabad", "Mainpuri",
        "Etawah", "Banda", "Mirzapur", "Azamgarh", "Jaunpur", "Ghazipur", "Deoria", "Basti"
    ),
    "Karnataka" to listOf(
        "Bengaluru Rural", "Bengaluru Urban", "Belagavi", "Mysuru", "Dharwad", "Ballari",
        "Vijayapura", "Davanagere", "Shivamogga", "Tumakuru", "Kalaburagi", "Raichur",
        "Bagalkot", "Mandya", "Hassan", "Haveri", "Kolar", "Gadag", "Chikkamagaluru",
        "Chitradurga", "Udupi", "Dakshina Kannada", "Uttara Kannada", "Bidar", "Yadgir", "Koppal"
    ),
    "Andhra Pradesh" to listOf(
        "Guntur", "Krishna", "Kurnool", "Visakhapatnam", "East Godavari", "West Godavari",
        "Anantapur", "Chittoor", "Prakasam", "Nellore", "YSR Kadapa", "Srikakulam",
        "Vizianagaram", "Kakinada", "Konaseema", "Eluru", "NTR", "Bapatla", "Palnadu", "Tirupati"
    ),
    "Telangana" to listOf(
        "Hyderabad", "Warangal Urban", "Warangal Rural", "Nizamabad", "Karimnagar", "Khammam",
        "Mahabubnagar", "Nalgonda", "Adilabad", "Medak", "Rangareddy", "Sangareddy",
        "Siddipet", "Suryapet", "Jagtial", "Peddapalli", "Mancherial", "Kamareddy", "Bhadradri Kothagudem"
    ),
    "Tamil Nadu" to listOf(
        "Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem", "Tiruppur",
        "Erode", "Vellore", "Thanjavur", "Dindigul", "Theni", "Namakkal", "Tirunelveli",
        "Cuddalore", "Villupuram", "Kanchipuram", "Tiruvallur", "Dharmapuri", "Krishnagiri", "Kanyakumari"
    ),
    "Bihar" to listOf(
        "Patna", "Gaya", "Muzaffarpur", "Bhagalpur", "Purnia", "Darbhanga", "Begusarai",
        "Samastipur", "Rohtas", "Nalanda", "Vaishali", "Saran", "Katihar", "Saharsa",
        "Madhubani", "Siwan", "Gopalganj", "East Champaran", "West Champaran", "Bhojpur"
    ),
    "West Bengal" to listOf(
        "Kolkata", "North 24 Parganas", "South 24 Parganas", "Hooghly", "Howrah",
        "Murshidabad", "Purba Bardhaman", "Paschim Bardhaman", "Paschim Medinipur",
        "Purba Medinipur", "Nadia", "Malda", "Jalpaiguri", "Bankura", "Birbhum", "Darjeeling", "Cooch Behar"
    ),
    "Odisha" to listOf(
        "Khordha", "Cuttack", "Ganjam", "Balasore", "Sambalpur", "Puri", "Bargarh",
        "Bhadrak", "Jajpur", "Mayurbhanj", "Angul", "Bolangir", "Kalahandi", "Koraput",
        "Sundargarh", "Kendrapara", "Jagatsinghpur", "Dhenkanal", "Nayagarh", "Rayagada", "Kandhamal"
    ),
    "Chhattisgarh" to listOf(
        "Raipur", "Bilaspur", "Durg", "Rajnandgaon", "Korba", "Raigarh", "Bastar (Jagdalpur)",
        "Janjgir-Champa", "Kanker", "Mahasamund", "Dhamtari", "Balod", "Bemetara", "Kawardha", "Surguja"
    ),
    "Jharkhand" to listOf(
        "Ranchi", "Dhanbad", "East Singhbhum (Jamshedpur)", "Bokaro", "Hazaribagh",
        "Deoghar", "Giridih", "Ramgarh", "Palamu", "Dumka", "Garhwa", "Chatra"
    ),
    "Himachal Pradesh" to listOf(
        "Shimla", "Kangra", "Mandi", "Solan", "Kullu", "Sirmaur", "Hamirpur", "Una", "Bilaspur", "Chamba", "Kinnaur"
    ),
    "Uttarakhand" to listOf(
        "Dehradun", "Haridwar", "Udham Singh Nagar", "Nainital", "Almora", "Pauri Garhwal", "Tehri Garhwal", "Pithoragarh"
    ),
    "Assam" to listOf(
        "Kamrup", "Dibrugarh", "Nagaon", "Sonitpur", "Cachar", "Jorhat", "Barpeta", "Dhubri", "Tinsukia", "Karbi Anglong"
    ),
    "Kerala" to listOf(
        "Thiruvananthapuram", "Ernakulam", "Kozhikode", "Palakkad", "Thrissur", "Kollam", "Kottayam", "Malappuram", "Wayanad", "Idukki", "Alappuzha", "Kasargod"
    ),
    "Delhi (NCT)" to listOf("Central Delhi", "East Delhi", "New Delhi", "North Delhi", "North East Delhi", "North West Delhi", "Shahdara", "South Delhi", "South East Delhi", "South West Delhi", "West Delhi"),
    "Jammu and Kashmir" to listOf("Srinagar", "Jammu", "Anantnag", "Baramulla", "Pulwama", "Shopian", "Kulgam", "Budgam", "Kathua", "Udhampur"),
    "Goa" to listOf("North Goa", "South Goa"),
    "Tripura" to listOf("West Tripura", "Sepahijala", "Khowai", "Gomati", "South Tripura", "Unakoti", "North Tripura", "Dhalai"),
    "Meghalaya" to listOf("East Khasi Hills", "West Khasi Hills", "Ri Bhoi", "West Garo Hills", "East Garo Hills"),
    "Manipur" to listOf("Imphal East", "Imphal West", "Bishnupur", "Thoubal", "Churachandpur", "Kakching"),
    "Nagaland" to listOf("Kohima", "Dimapur", "Mokokchung", "Tuensang", "Wokha", "Mon"),
    "Mizoram" to listOf("Aizawl", "Lunglei", "Champhai", "Kolasib", "Serchhip"),
    "Arunachal Pradesh" to listOf("Papum Pare", "Changlang", "West Kameng", "East Siang", "Tawang", "Lohit"),
    "Sikkim" to listOf("East Sikkim (Gangtok)", "West Sikkim (Gyalshing)", "North Sikkim (Mangan)", "South Sikkim (Namchi)"),
    "Chandigarh" to listOf("Chandigarh"),
    "Puducherry" to listOf("Puducherry", "Karaikal", "Mahe", "Yanam"),
    "Ladakh" to listOf("Leh", "Kargil"),
    "Andaman and Nicobar Islands" to listOf("South Andaman", "North and Middle Andaman", "Nicobar"),
    "Dadra and Nagar Haveli and Daman and Diu" to listOf("Daman", "Diu", "Dadra and Nagar Haveli"),
    "Lakshadweep" to listOf("Kavaratti", "Agatti", "Andrott", "Minicoy")
)

// Default geographic coordinates & crops fallback for Indian districts across all states
val DISTRICT_GEO_COORDS: Map<String, Triple<Double, Double, String>> = mapOf(
    // Maharashtra
    "Nashik" to Triple(20.00, 73.78, "Onion, Grapes, Tomato"),
    "Pune" to Triple(18.52, 73.85, "Vegetables, Sugarcane, Flowers"),
    "Ahmednagar" to Triple(19.09, 74.74, "Sugarcane, Onion, Bajra"),
    "Solapur" to Triple(17.65, 75.90, "Pomegranate, Jowar, Tur"),
    "Latur" to Triple(18.40, 76.58, "Soybean, Tur (Pigeon Pea), Gram"),
    "Kolhapur" to Triple(16.70, 74.24, "Sugarcane, Jaggery, Rice"),
    "Akola" to Triple(20.70, 77.00, "Cotton, Soybean, Wheat"),
    "Nagpur" to Triple(21.14, 79.08, "Nagpur Orange, Cotton, Paddy"),
    "Amravati" to Triple(20.93, 77.75, "Soybean, Cotton, Orange"),
    "Jalgaon" to Triple(21.00, 75.56, "Banana, Cotton, Pulses"),
    "Chhatrapati Sambhajinagar" to Triple(19.87, 75.34, "Cotton, Maize, Sweet Lime"),
    "Jalna" to Triple(19.84, 75.88, "Sweet Lime, Cotton, Pulses"),
    "Beed" to Triple(18.99, 75.76, "Cotton, Bajra, Soybean"),
    "Nanded" to Triple(19.15, 77.31, "Cotton, Banana, Tur"),
    "Yavatmal" to Triple(20.39, 78.13, "Cotton, Soybean, Jowar"),
    "Buldhana" to Triple(20.53, 76.18, "Soybean, Cotton, Maize"),
    "Satara" to Triple(17.68, 73.99, "Strawberry, Ginger, Jowar"),
    "Sangli" to Triple(16.85, 74.58, "Turmeric, Grapes, Raisins"),
    "Washim" to Triple(20.11, 77.13, "Soybean, Cotton, Gram"),
    "Parbhani" to Triple(19.27, 76.77, "Cotton, Soybean, Jowar"),
    "Dharashiv (Osmanabad)" to Triple(18.17, 76.04, "Soybean, Gram, Sugarcane"),
    "Dhule" to Triple(20.90, 74.77, "Cotton, Bajra, Maize"),
    "Nandurbar" to Triple(21.37, 74.24, "Chilli, Papaya, Maize"),
    "Chandrapur" to Triple(19.95, 79.29, "Paddy, Cotton, Soybean"),
    "Wardha" to Triple(20.74, 78.60, "Cotton, Soybean, Gram"),
    "Gondia" to Triple(21.46, 80.20, "Paddy (Rice), Pulses"),
    "Bhandara" to Triple(21.17, 79.65, "Paddy, Sugarcane, Vegetables"),
    "Gadchiroli" to Triple(20.18, 80.00, "Paddy, Forest Produce"),
    "Hingoli" to Triple(19.72, 77.15, "Turmeric, Cotton, Soybean"),
    "Palghar" to Triple(19.69, 72.76, "Rice, Sapota (Chiku), Flowers"),
    "Thane" to Triple(19.21, 72.97, "Vegetables, Rice, Floriculture"),
    "Raigad" to Triple(18.51, 73.18, "Paddy, Coconut, Betel Nut"),
    "Ratnagiri" to Triple(16.99, 73.30, "Alphonso Mango, Cashew, Paddy"),
    "Sindhudurg" to Triple(16.12, 73.68, "Mango, Cashew, Coconut"),
    "Baramati" to Triple(18.15, 74.58, "Sugarcane, Dairy, Grapes"),

    // Gujarat
    "Ahmedabad" to Triple(23.02, 72.57, "Cotton, Wheat, Vegetables"),
    "Surat" to Triple(21.17, 72.83, "Sugarcane, Paddy, Banana"),
    "Vadodara" to Triple(22.30, 73.18, "Cotton, Tobacco, Banana"),
    "Rajkot" to Triple(22.30, 70.80, "Groundnut, Cotton, Cumin"),
    "Bhavnagar" to Triple(21.76, 72.15, "Onion, Cotton, Groundnut"),
    "Jamnagar" to Triple(22.47, 70.06, "Groundnut, Cotton, Garlic"),
    "Junagadh" to Triple(21.52, 70.45, "Kesar Mango, Groundnut, Sesame"),
    "Gandhinagar" to Triple(23.21, 72.63, "Wheat, Vegetables, Castor"),
    "Anand" to Triple(22.56, 72.92, "Tobacco, Banana, Dairy"),
    "Bharuch" to Triple(21.70, 72.99, "Cotton, Banana, Sugarcane"),
    "Banaskantha" to Triple(24.17, 72.43, "Potato, Mustard, Dairy"),
    "Sabarkantha" to Triple(23.58, 72.96, "Groundnut, Cotton, Maize"),
    "Mehsana" to Triple(23.58, 72.36, "Cumin, Fennel, Castor"),
    "Patan" to Triple(23.85, 72.12, "Mustard, Castor, Cumin"),
    "Kutch" to Triple(23.24, 69.66, "Dates, Cotton, Pomegranate"),
    "Amreli" to Triple(21.60, 71.22, "Cotton, Groundnut, Sesame"),
    "Porbandar" to Triple(21.64, 69.60, "Groundnut, Gram, Wheat"),
    "Navsari" to Triple(20.95, 72.93, "Mango, Sugarcane, Chiku"),
    "Valsad" to Triple(20.61, 72.93, "Mango (Valsadi Hapus), Sapota"),
    "Morbi" to Triple(22.82, 70.83, "Cotton, Sesame, Groundnut"),
    "Surendranagar" to Triple(22.72, 71.63, "Cotton (Shankar-6), Cumin"),

    // Madhya Pradesh
    "Indore" to Triple(22.71, 75.85, "Soybean, Wheat, Potato"),
    "Bhopal" to Triple(23.25, 77.41, "Wheat, Gram, Garlic"),
    "Ujjain" to Triple(23.17, 75.78, "Soybean, Wheat, Gram"),
    "Jabalpur" to Triple(23.18, 79.98, "Peas, Rice, Wheat"),
    "Gwalior" to Triple(26.22, 78.18, "Mustard, Wheat, Potato"),
    "Sagar" to Triple(23.83, 78.71, "Wheat, Soybean, Gram"),
    "Dewas" to Triple(22.96, 76.05, "Soybean, Wheat, Maize"),
    "Ratlam" to Triple(23.33, 75.03, "Garlic, Soybean, Wheat, Grapes"),
    "Mandsaur" to Triple(24.07, 75.06, "Garlic, Mustard, Onion"),
    "Neemuch" to Triple(24.47, 74.87, "Garlic, Coriander, Wheat"),
    "Narmadapuram (Hoshangabad)" to Triple(22.75, 77.72, "Wheat (Sharbati), Moong"),
    "Sehore" to Triple(23.20, 77.08, "Sharbati Wheat, Soybean"),
    "Khargone" to Triple(21.82, 75.61, "Cotton, Chilli, Maize"),
    "Khandwa" to Triple(21.83, 76.35, "Cotton, Soybean, Wheat"),
    "Dhar" to Triple(22.59, 75.30, "Soybean, Wheat, Cotton"),
    "Chhindwara" to Triple(22.05, 78.93, "Corn (Maize), Potato, Ginger"),
    "Rewa" to Triple(24.53, 81.30, "Paddy, Wheat, Pulses"),
    "Harda" to Triple(22.34, 77.09, "Wheat, Moong, Soybean"),

    // Rajasthan
    "Jaipur" to Triple(26.91, 75.78, "Mustard, Barley, Vegetables"),
    "Jodhpur" to Triple(26.23, 73.02, "Cumin, Castor, Isabgol"),
    "Kota" to Triple(25.21, 75.86, "Soybean, Mustard, Coriander"),
    "Bikaner" to Triple(28.02, 73.31, "Guar, Groundnut, Moth Bean"),
    "Ajmer" to Triple(26.45, 74.64, "Rose, Onion, Moong"),
    "Udaipur" to Triple(24.58, 73.71, "Maize, Wheat, Mustard"),
    "Alwar" to Triple(27.55, 76.63, "Mustard, Onion, Bajra"),
    "Sri Ganganagar" to Triple(29.90, 73.87, "Kinnow, Wheat, Cotton, Mustard"),
    "Bharatpur" to Triple(27.21, 77.48, "Mustard (Sarson), Wheat"),
    "Sikar" to Triple(27.61, 75.14, "Onion, Bajra, Mustard"),
    "Nagaur" to Triple(27.20, 73.74, "Fenugreek (Methi), Cumin, Moong"),
    "Hanumangarh" to Triple(29.58, 74.32, "Cotton, Wheat, Mustard, Paddy"),

    // Punjab
    "Ludhiana" to Triple(30.90, 75.85, "Wheat, Paddy, Dairy"),
    "Amritsar" to Triple(31.63, 74.87, "Basmati Rice, Wheat, Vegetables"),
    "Jalandhar" to Triple(31.32, 75.57, "Potato, Wheat, Maize"),
    "Patiala" to Triple(30.33, 76.38, "Wheat, Paddy, Mustard"),
    "Bathinda" to Triple(30.21, 74.94, "Cotton, Wheat, Mustard"),
    "Sangrur" to Triple(30.24, 75.84, "Paddy, Wheat, Sugarcane"),
    "Fazilka" to Triple(30.40, 74.02, "Kinnow, Cotton, Wheat"),
    "Khanna" to Triple(30.70, 76.22, "Wheat, Basmati Rice, Maize"),

    // Haryana
    "Karnal" to Triple(29.68, 76.99, "Basmati Rice, Wheat, Dairy"),
    "Kurukshetra" to Triple(29.96, 76.87, "Paddy, Wheat, Sunflower"),
    "Ambala" to Triple(30.37, 76.77, "Wheat, Paddy, Mustard"),
    "Hisar" to Triple(29.14, 75.72, "Cotton, Wheat, Mustard"),
    "Rohtak" to Triple(28.89, 76.60, "Wheat, Bajra, Sugarcane"),
    "Sirsa" to Triple(29.53, 75.02, "Cotton, Wheat, Guar"),

    // Uttar Pradesh
    "Lucknow" to Triple(26.84, 80.94, "Mango (Dussehri), Paddy, Wheat"),
    "Kanpur Nagar" to Triple(26.44, 80.33, "Wheat, Potato, Pulses"),
    "Agra" to Triple(27.17, 78.00, "Potato, Mustard, Wheat"),
    "Varanasi" to Triple(25.31, 82.97, "Paddy, Vegetables, Mango"),
    "Prayagraj" to Triple(25.43, 81.84, "Guava (Allahabadi), Paddy, Wheat"),
    "Meerut" to Triple(28.98, 77.70, "Sugarcane, Vegetables, Wheat"),
    "Bareilly" to Triple(28.36, 79.43, "Sugarcane, Rice, Wheat"),
    "Gorakhpur" to Triple(26.76, 83.37, "Sugarcane, Paddy, Vegetables"),
    "Muzaffarnagar" to Triple(29.47, 77.70, "Sugarcane, Jaggery, Wheat"),
    "Hardoi" to Triple(27.39, 80.12, "Wheat, Paddy, Mustard, Potato"),
    "Aligarh" to Triple(27.89, 78.08, "Mustard, Wheat, Potato"),

    // Karnataka
    "Bengaluru Rural" to Triple(13.23, 77.58, "Ragi, Flowers, Vegetables"),
    "Bengaluru Urban" to Triple(12.97, 77.59, "Floriculture, Vegetables"),
    "Belagavi" to Triple(15.84, 74.49, "Sugarcane, Maize, Vegetables"),
    "Mysuru" to Triple(12.29, 76.63, "Paddy, Sugarcane, Betel Leaf"),
    "Vijayapura" to Triple(16.83, 75.71, "Grapes, Lemon, Tur, Jowar"),
    "Ballari" to Triple(15.13, 76.92, "Cotton, Chilli, Paddy"),
    "Kalaburagi" to Triple(17.32, 76.83, "Tur (Red Gram), Soybean"),
    "Chikkamagaluru" to Triple(13.31, 75.77, "Coffee (Arabica & Robusta), Pepper, Cardamom"),

    // Andhra Pradesh & Telangana
    "Guntur" to Triple(16.30, 80.43, "Chilli (Teja & Sannam), Cotton, Tobacco, Paddy"),
    "Krishna" to Triple(16.18, 81.13, "Paddy (BPT), Sugarcane, Mango"),
    "Kurnool" to Triple(15.82, 78.03, "Onion, Groundnut, Cotton"),
    "Visakhapatnam" to Triple(17.68, 83.21, "Paddy, Sugarcane, Cashew"),
    "Warangal Urban" to Triple(17.96, 79.59, "Cotton, Chilli, Paddy, Turmeric"),
    "Nizamabad" to Triple(18.67, 78.09, "Turmeric, Paddy, Soybean"),
    "Karimnagar" to Triple(18.43, 79.12, "Paddy, Cotton, Maize"),
    "Khammam" to Triple(17.24, 80.15, "Chilli, Cotton, Mango"),

    // Tamil Nadu
    "Coimbatore" to Triple(11.01, 76.95, "Coconut, Tea, Vegetables"),
    "Erode" to Triple(11.34, 77.72, "Turmeric, Coconut, Sugarcane"),
    "Theni" to Triple(10.01, 77.47, "Banana (Grand Naine), Grapes, Cotton"),
    "Thanjavur" to Triple(10.78, 79.13, "Paddy (Ponni), Coconut, Banana"),
    "Salem" to Triple(11.66, 78.14, "Mango (Salem Gundu), Tapioca, Coffee"),

    // Bihar & West Bengal & Odisha
    "Patna" to Triple(25.59, 85.13, "Paddy, Wheat, Maize, Vegetables"),
    "Muzaffarpur" to Triple(26.12, 85.39, "Shahi Litchi, Mango, Maize"),
    "Purnia" to Triple(25.77, 87.47, "Maize (Corn), Jute, Paddy"),
    "Darbhanga" to Triple(26.15, 85.90, "Makhana (Foxnut), Fish, Mango"),
    "Gaya" to Triple(24.79, 85.00, "Paddy, Wheat, Pulses"),
    "Kolkata" to Triple(22.57, 88.36, "Vegetables, Betel Leaf, Jute"),
    "Purba Bardhaman" to Triple(23.23, 87.86, "Rice (Gobindobhog & Swarna), Potato"),
    "Hooghly" to Triple(22.90, 88.39, "Potato (Jyoti), Jute, Paddy"),
    "Murshidabad" to Triple(24.18, 88.26, "Jute, Silk, Paddy, Mustard"),
    "Sambalpur" to Triple(21.46, 83.97, "Paddy, Pulses, Vegetables"),
    "Bargarh" to Triple(21.33, 83.61, "Paddy (Rice Bowl of Odisha), Groundnut"),
    "Cuttack" to Triple(20.46, 85.88, "Paddy, Pulses, Jute"),
    "Kandhamal" to Triple(20.44, 84.14, "Kandhamal Turmeric (GI), Ginger"),

    // Assam & North East
    "Kamrup" to Triple(26.20, 91.73, "Areca Nut, Rice, Mustard"),
    "Jorhat" to Triple(26.75, 94.20, "Assam Tea, Rice, Vegetables"),
    "Dibrugarh" to Triple(27.47, 94.91, "Tea, Rice, Bamboo"),
    "Karbi Anglong" to Triple(26.00, 93.30, "Organic Ginger, Turmeric, Pineapple"),

    // Kerala
    "Wayanad" to Triple(11.68, 76.13, "Black Pepper, Coffee, Cardamom, Tea"),
    "Idukki" to Triple(9.85, 76.98, "Cardamom (Small), Pepper, Tea, Cocoa"),
    "Palakkad" to Triple(10.78, 76.65, "Paddy (Matta Rice), Banana, Groundnut"),

    // Delhi & Central
    "Delhi (NCT)" to Triple(28.70, 77.10, "Azadpur Mandi Wholesale Produce, Wheat, Veg")
)

/**
 * Helper object providing nationwide hierarchy resolution:
 * State/UT -> District -> Mandi/APMC -> Commodity -> Variety -> Grade
 */
object IndiaLocationRegistry {

    fun getAllStates(): List<String> = INDIAN_STATES

    fun getDistrictsForState(stateName: String): List<String> {
        if (stateName.isBlank() || stateName.equals("All", ignoreCase = true) || stateName.equals("All States", ignoreCase = true)) {
            return STATE_DISTRICTS_MAP.values.flatten().distinct().sorted()
        }
        return STATE_DISTRICTS_MAP[stateName] ?: emptyList()
    }

    val COMMODITY_VARIETY_MAP: Map<String, List<String>> = mapOf(
        "Onion" to listOf("Red (Garwa)", "White (Desi)", "Pusa Red", "Nashik Special", "Bellary Onion", "Bangalore Rose"),
        "Soybean" to listOf("JS 335", "JS 9560", "Yellow Seeded", "NRC 37", "Certified Bold"),
        "Wheat" to listOf("Sharbati (MP)", "Lokwan", "Pusa Tejas (HI 8759)", "PBW 343", "HD 2967", "Kalyan Sona", "Desi Wheat"),
        "Rice / Paddy" to listOf("Basmati (Pusa 1121)", "Basmati 1509", "Gobindobhog (GI)", "BPT 5204 (Samba Mahsuri)", "Swarna", "IR 64", "Common / FAQ"),
        "Cotton" to listOf("Shankar-6 (Gujarat)", "BT Cotton", "Bunny", "MCU-5", "Desi Kapas", "DCH-32"),
        "Tomato" to listOf("Hybrid (Abhinav)", "Himsona", "Desi Tamatar", "Roma", "Vaishali"),
        "Potato" to listOf("Kufri Pukhraj", "Kufri Jyoti", "Kufri Chipsona", "Desi Red", "Lal Gulab"),
        "Mustard / Rapeseed" to listOf("Pusa Bold", "Varuna", "Kanti", "Giriraj", "Black Mustard"),
        "Maize / Corn" to listOf("Yellow Corn (Kargil)", "Sweet Corn", "White Maize", "Hybrid Pioneer"),
        "Tur / Arhar (Red Gram)" to listOf("Maruti (ICP 8863)", "Asha (ICPL 87119)", "Gulbarga Bold", "Desi Lal Tur"),
        "Gram / Chana (Bengal Gram)" to listOf("Desi Chana", "Kabuli Chana (Dollar)", "JG 11", "Vishal", "Vijay"),
        "Garlic" to listOf("G-282 (Bold White)", "Ooty Garlic", "Yamuna Safed (G-1)", "Desi Lasan"),
        "Turmeric" to listOf("Salem (Tamil Nadu)", "Rajapuri (Sangli)", "Nizamabad Special", "Kandhamal Haldi (GI)", "Prathibha"),
        "Chilli (Red / Green)" to listOf("Guntur Sannam", "Teja (Khammam)", "Byadgi (Karnataka)", "Jwala", "Bullet Chilli"),
        "Pomegranate" to listOf("Bhagwa Super Red", "Arakta", "Ganesh", "Ruby"),
        "Banana" to listOf("Grand Naine (G9)", "Robusta", "Nendran (Kerala)", "Yellaki / Elaichi", "Basrai"),
        "Grapes" to listOf("Thomson Seedless", "Tas-A-Ganesh", "Sharad Seedless (Black)", "Sonaka", "Manik Chaman"),
        "Sugarcane" to listOf("Co 0238", "Co 86032", "CoM 0265 (Phule 265)", "Co 0118"),
        "Groundnut" to listOf("GG 20", "TAG 24", "Kadiri 6", "Bold Kernels"),
        "Makhana (Foxnut)" to listOf("Superior Mithila Bold", "Rasgulla Grade", "Standard Raw"),
        "Jute" to listOf("Tossa Jute", "White Jute", "Mesta"),
        "Tea" to listOf("Assam CTC Premium", "Darjeeling Orthodox (First Flush)", "Nilgiri Green"),
        "Coffee" to listOf("Arabica Plantation A", "Robusta Parchment", "Monsooned Malabar"),
        "Black Pepper" to listOf("Malabar Garbled (MG1)", "Tellicherry Extra Bold (TGSEB)", "Panniyur-1"),
        "Cardamom" to listOf("Alleppey Green Extra Bold (AGEB)", "Green Gold 8mm+", "Small FAQ")
    )

    val GRADE_OPTIONS: List<String> = listOf(
        "Grade A+ (Export / Superior)",
        "Grade A (FAQ - Fair Average Quality)",
        "Grade B (Commercial / Processing)",
        "Grade C (Local / Small Size)",
        "FAQ Standard (Government Verified)"
    )

    fun getVarietiesForCommodity(commodity: String): List<String> {
        val matchedKey = COMMODITY_VARIETY_MAP.keys.firstOrNull { it.contains(commodity, ignoreCase = true) || commodity.contains(it, ignoreCase = true) }
        return matchedKey?.let { COMMODITY_VARIETY_MAP[it] } ?: listOf("Standard FAQ Variety", "Hybrid Variety", "Desi Local Selection")
    }
}
