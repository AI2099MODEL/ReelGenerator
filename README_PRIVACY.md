# My Organiser / Lyfe - Privacy & Policy Documentation

*Last updated: September 2026*

This document provides a concise developer and compliance summary of **Lyfe / My Organiser**, its on-device architecture, permissions model, Google Play Store compliance, and Data Safety requirements.

---

## 1. Architectural Highlights

- **Local-First & Offline-Ready:** All primary data (daily schedules, reminder notes, contact cards, private vault documents, notes, and OCR scans) are stored in local SQLite/Room tables and internal sandbox directories.
- **Privacy-by-Design:** No GPS tracking, no remote database synchronization, user profiling, or background tracking servers.
- **Biometric Security:** Vault authentication is delegated to Android's `BiometricPrompt` framework and hardware security module (TEE).

---

## 2. Permissions Justification (Google Play Store Review)

| Manifest Permission | Justification & Usage |
| :--- | :--- |
| `android.permission.INTERNET` | Required for Google Mobile Ads delivery, consent updates via UMP, and optional AI Image Studio generation. |
| `android.permission.ACCESS_NETWORK_STATE` | Allows the app to detect connectivity before making external API or ad calls. |
| `android.permission.CAMERA` | Used solely for QR code scanning and document/receipt photography for the secure Vault. Viewfinder frames are never retained. |
| `android.permission.RECORD_AUDIO` | Used only for on-device voice-to-text prompt transcription when the user presses the microphone icon. |
| `android.permission.POST_NOTIFICATIONS` | Delivers user-requested notifications and music playback controls. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Automatically re-registers app services and notifications after device restart. |
| `android.permission.VIBRATE` | Provides haptic feedback during user interactions. |
| `android.permission.WRITE_SETTINGS` | System intent allowing users to set custom ringtones if desired. |

---

## 3. Google Play Data Safety Compliance Matrix

When completing the **Data Safety form in Google Play Console**, use the following mappings:

1. **Location**:
   - **No Data Collected**: The app does not request or collect GPS or location data.
2. **Personal Info**:
   - *Name & Phone Number*: Collected as entered in local contacts/ledger. Stored locally only. Not shared. User can delete anytime.
3. **Photos & Videos**:
   - *Photos*: User-selected Vault/OCR images. Stored locally only. User can delete anytime.
4. **Audio Files**:
   - *Voice recordings / prompts*: Ephemeral speech-to-text processing. Not stored or shared.
5. **Files & Documents**:
   - *PDFs & Notes*: Stored locally in encrypted Vault. Not shared. User can delete anytime.
6. **Device or Other IDs**:
   - *Advertising ID (AAID)*: Collected by Google Mobile Ads SDK for ad delivery and fraud prevention. Handled under Google Play Services policies with TLS 1.3 encryption.

---

## 4. Legal & Regulatory Compliance

- **GDPR / UK GDPR (EU/UK):** Full data subject rights supported (Access, Rectification, Erasure, Data Portability, and UMP Consent Management).
- **CCPA / CPRA (US):** No sale of personal data; user-controlled deletion and correction directly in the app.
- **COPPA:** General audience app, not directed to children under 13.
- **Data Deletion Policy:** Complete in-app item deletion and device-level storage clearing capabilities.

---

## 5. Contact & Support

- **Support Email:** `anuakku20138@gmail.com`
- **Application:** Lyfe / My Organiser for Android
