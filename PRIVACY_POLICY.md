# Privacy Policy

**Effective Date:** September 9, 2026  
**Last Updated:** September 9, 2026  
**Applicability:** Global, including the European Union / European Economic Area (GDPR), United Kingdom (UK GDPR), United States (CCPA/CPRA, VCDPA, CPA, CTDPA, UCPA), and Google Play Developer Program Policies.

---

## 1. Introduction & Privacy Commitment

Welcome to **Lyfe / My Organiser** ("the App", "we", "us", or "our"). We are committed to protecting your personal data, maintaining transparency, and upholding your fundamental right to privacy.

Our application is built upon a strict **Privacy-by-Design and Local-First Architecture**:
- **On-Device Sandboxed Storage:** Your daily schedules, tasks, notes, important dates, ledger entries, contacts, audio recordings, OCR scans, and vaulted documents are stored and processed **locally on your device** in a private, sandboxed SQLite/Room database.
- **No GPS Tracking or Location Access:** The App does not request, track, or access device GPS or location hardware.
- **No Remote User Profiling:** We do not track you across apps or websites, do not sell your personal data, and do not maintain cloud user profile databases.
- **User Autonomy:** You have full ownership and control over your data, including the ability to backup, export, or permanently erase all records at any time.

---

## 2. Information We Collect, Process, and Disclose

### A. Information Stored Locally on Your Device
The following data categories are created by you and stored strictly in your device's protected internal app storage:
- **Daily Tasks & Schedules:** Task titles, time blocks, category tags, priority levels, and completion statuses.
- **Important Dates & Reminders:** Birthdays, anniversaries, bill due dates, and custom notes.
- **My Vault & Documents:** Securely stored photos, scanned PDFs, receipts, and personal documents.
- **Ledger & Contact Cards:** Contact names, phone numbers, notes, and generated/scanned QR codes.
- **Biometric Authentication:** When unlocking the Vault using fingerprint or face unlock, authentication is handled entirely by Android's hardware security module (`BiometricPrompt` / TEE). **Biometric credentials never leave your device and are never accessible to the App.**

---

### B. Device Permissions & System Capabilities
The App requests only minimal runtime permissions strictly necessary for user-initiated functionality:

| Permission | Purpose | Data Handling |
| :--- | :--- | :--- |
| `android.permission.CAMERA` | Scan QR codes and capture photos for the Vault/OCR. | Processed locally in real time; viewfinder streams are never saved or transmitted remotely. |
| `android.permission.RECORD_AUDIO` | Speech-to-text input for quick note/prompt entry. | Transcribed via system speech services; raw audio is not stored or uploaded. |
| `android.permission.POST_NOTIFICATIONS` | Deliver user-requested notifications and music player status. | Generated locally on-device. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Restore app services and notification state after device reboot. | Handled locally on-device. |
| `android.permission.VIBRATE` | Haptic feedback for notifications and UI interaction. | Device hardware control only. |
| `android.permission.WRITE_SETTINGS` | Optional setting of custom system ringtones via system intent. | Requires explicit system settings approval. |

---

### C. Third-Party Services & SDK Disclosures

To support application features and monetization, the App integrates vetted industry-standard SDKs:

#### 1. Google Mobile Ads (AdMob) & User Messaging Platform (UMP)
- **Purpose:** Display banner, interstitial, or rewarded advertisements within the App.
- **Data Processed:** Google Mobile Ads may collect device identifiers (such as the Google Advertising ID / AAID), IP addresses (for coarse country determination), diagnostic logs, and ad interaction metrics to deliver and measure advertisements.
- **Consent Management:** In accordance with Google's EU User Consent Policy and regional privacy laws, European Economic Area (EEA) and UK users are presented with a User Messaging Platform (UMP) consent form to customize ad personalization and cookie/ID preferences.
- **Opt-Out:** You can reset or delete your Advertising ID or opt out of personalized ads anytime via **Android Settings > Google > Ads**.
- **Privacy Policy:** [Google Privacy & Terms](https://policies.google.com/technologies/ads)

#### 2. AI Image Studio & Generative Services (Optional)
- **Purpose:** When you explicitly choose to use the AI Image Studio to generate images or refine prompts, your prompt text and style preferences are sent via encrypted HTTPS to external inference APIs (e.g., Pollinations AI / Google Gemini API).
- **Data Protection:** No contact lists, personal vault documents, or device identities are included in these requests.

---

## 3. Google Play Data Safety Summary

In compliance with Google Play Store Data Safety requirements:

| Data Type | Collected / Shared | Ephemeral / Stored | Encrypted in Transit | User Deletion Available |
| :--- | :--- | :--- | :--- | :--- |
| **Location Data** | **Not Collected** (No GPS or Location Access) | N/A | N/A | N/A |
| **Personal Info (Name, Phone)** | Collected only as entered by user | Stored locally on-device only | N/A (Local) | Yes (In-app delete) |
| **Photos & Videos** | Collected only when added to Vault/OCR | Stored locally on-device only | N/A (Local) | Yes (In-app delete) |
| **Audio (Voice prompts)** | Processed for speech-to-text | Ephemeral (not stored) | N/A (Local) | Yes |
| **Files & Documents (PDFs, Notes)**| Collected only when added to Vault | Stored locally on-device only | N/A (Local) | Yes (In-app delete) |
| **Device or other IDs (AAID)** | Shared with Google AdMob for ad delivery | Managed by Google Play Services | Yes (TLS 1.3) | Yes (Reset AAID in Android Settings) |
| **App Diagnostics / Performance** | Collected for ad delivery & stability | Processed by Google Mobile Ads SDK| Yes (TLS 1.3) | Managed by OS |

---

## 4. European Union & UK Privacy Rights (GDPR / UK GDPR)

If you are located in the European Economic Area (EEA), European Union (EU), or United Kingdom (UK), Regulation (EU) 2016/679 (GDPR) and the Data Protection Act 2018 grant you the following rights:

- **Legal Bases for Processing (Article 6 GDPR):**
  - **Performance of a Contract:** Storing, organizing, and securing your personal notes, tasks, and files.
  - **Consent (Article 6(1)(a)):** For optional camera, microphone access, and personalized advertising via UMP.
  - **Legitimate Interests (Article 6(1)(f)):** Ensuring application stability, local database integrity, and security.

- **Data Subject Rights:**
  - **Right of Access (Article 15):** You can access all your data directly inside the App screens.
  - **Right to Rectification (Article 16):** Edit any record, contact, or note instantly in the UI.
  - **Right to Erasure (Article 17):** Delete any individual item, or perform a total wipe via **Settings > Apps > Lyfe / My Organiser > Clear Storage**.
  - **Right to Data Portability (Article 20):** Export your complete database using the built-in Backup & Export feature.
  - **Right to Withdraw Consent:** Revoke permissions anytime in Android System Settings or update Ad Consent via the in-app settings menu.
  - **Right to Lodge a Complaint:** You have the right to contact your national Data Protection Authority (DPA) or the UK Information Commissioner's Office (ICO).

---

## 5. United States State Privacy Rights (CCPA / CPRA & State Laws)

Residents of California, Virginia, Colorado, Connecticut, Utah, and other US states have specific statutory protections:

- **Categories of Personal Information Collected:** Identifiers (contact names entered into local ledger), Audio/Visual information (photos/voice prompts processed on-device), and Internet/Network Activity (ad telemetry via Google AdMob).
- **Sale & Sharing of Personal Data:** We **DO NOT SELL** your personal information. AdMob advertising identifiers are processed in accordance with CCPA service provider terms and user consent choices.
- **Right to Know, Delete, and Correct:** You may view, correct, or delete your data directly within the application.
- **Non-Discrimination:** We will never penalize or discriminate against any user for exercising their statutory privacy rights.

---

## 6. Children’s Privacy (COPPA & Google Play Families Policy)

**Lyfe / My Organiser** is intended for a general audience and is **not directed to children under the age of 13** (or under 16 in certain European jurisdictions). We do not knowingly collect personal information from children. If you believe that a child has provided us with personal information, please contact us immediately.

---

## 7. Data Security, Retention, and Deletion Architecture

- **Application Sandboxing:** All local files, SQLite databases, and cached assets are isolated within Android's protected application sandbox, inaccessible to other apps.
- **Biometric Cryptographic Isolation:** Vault access keys leverage Android KeyStore hardware primitives.
- **Zero Remote Retention of Personal Content:** Because personal data resides on your device, we retain zero copies on cloud servers.
- **Data Deletion Mechanism:** 
  - To delete specific records: Tap the delete icon on any task, schedule, document, or contact.
  - To delete all application data: Go to **Android Settings > Apps > Lyfe / My Organiser > Storage & Cache > Clear Storage**, or simply uninstall the application.

---

## 8. Changes to This Privacy Policy

We may periodically update this Privacy Policy to reflect application updates, SDK revisions, or evolving legal standards. Any modifications will be posted with a revised "Last Updated" date.

---

## 9. Contact Information & Developer Entity

If you have questions, feedback, or requests regarding this Privacy Policy or your data protection rights, please contact:

- **Application:** Lyfe / My Organiser for Android
- **Developer / Controller:** Lyfe Application Development Team
- **Contact Email:** `anuakku20138@gmail.com`
- **Official Policy URL:** `https://ais-pre-7va6et5cmfr2bqdzzsb25k-202411574583.asia-southeast1.run.app/privacy-policy.html`
