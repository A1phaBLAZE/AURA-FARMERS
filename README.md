# Kisan Vani 2.0 (किसान वाणी)
### Production-Grade Digital Mandi & Agricultural Advisory Platform for Maharashtra

Kisan Vani 2.0 empowers farmers, FPOs, and APMC traders with transparent price discovery, direct institutional lot bidding, escrow safety, dynamic weather alerts, and official **Live AGMARKNET Mandi Price Feeds** from data.gov.in (Government of India).

---

## 🇮🇳 Official Government of India Live Mandi Feature (AGMARKNET)

The application integrates with the official data.gov.in AGMARKNET Daily Prices Resource:
- **API Endpoint:** `https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070`
- **Source Attribution:** *Source: AGMARKNET via data.gov.in, Government of India.*
- **Data Attributes:** State, District, Market (Mandi), Commodity, Variety, Grade, Arrival Date, Min Price, Modal Price, Max Price in ₹/Quintal.

### Key Capabilities
1. **5-Level Cascading Hierarchy Filters:**
   - 1. State (Default: *Maharashtra*, with all Indian states supported)
   - 2. District (All 36 Maharashtra districts)
   - 3. Mandi / Market (APMC search & filter)
   - 4. Commodity (Onion, Cotton, Soybean, Wheat, Tomato, Gram, etc.)
   - 5. Variety (Optional variety specification)
2. **15-Minute Response Caching:** In-memory client/proxy cache prevents redundant API calls and rate-limiting.
3. **Delayed Data Detection:** Automatic detection when official government arrival records are older than 2 days, displaying an informative buffer alert.
4. **Anti-Spam Refresh Cooldown:** 15-second debounce countdown to avoid quota exhaustion.
5. **Local Audio Bulletins:** Multilingual audio readout of mandi modal prices in Marathi, Hindi, Gujarati, and English.

---

## 🔑 Setup & API Key Configuration

To run the Live AGMARKNET feature locally or in AI Studio:

### 1. Obtain a data.gov.in API Key
1. Visit the Open Government Data (OGD) Platform India: [https://data.gov.in](https://data.gov.in)
2. Register for a free account or log in.
3. Navigate to **My Account** -> **API Key** -> **Create API Key**.
4. Copy your generated personal API key.

### 2. Configure Environment Variable
Add `DATA_GOV_API_KEY` to your environment or AI Studio Secrets panel:

**In AI Studio:**
- Open the **Secrets panel** on the right side.
- Add `DATA_GOV_API_KEY` with your copied key.

**Locally (via `.env`):**
Create a `.env` file in the project root:
```properties
DATA_GOV_API_KEY=your_actual_data_gov_in_api_key_here
GEMINI_API_KEY=your_gemini_api_key_here
```

### 3. Build & Run
```bash
gradle assembleDebug
```
Launch the application and tap **Mandi Rates** -> **Govt AGMARKNET Live** tab to view real-time official arrival prices.

---

## 🔒 Security & Privacy Architecture
- **No Hardcoded Keys:** `DATA_GOV_API_KEY` is loaded through Gradle `BuildConfig` and the Secrets Gradle Plugin; it is never exposed in logs or user interface.
- **Data Leakage Prevention (DLP):** Includes Android `FLAG_SECURE` screen capture guard, 25s clipboard auto-purge, and PII masking.
- **Hardware Enclave (TEE):** Digital signature verification for direct farmer-buyer trade contracts.
