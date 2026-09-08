# Privacy Policy

**Effective Date:** September 8, 2026  
**Last Updated:** September 8, 2026  
**Applicability:** Global, including the European Union / European Economic Area (GDPR), United Kingdom (UK GDPR), and United States (CCPA/CPRA, VCDPA, CPA, CTDPA, UCPA, COPPA).

---

## 1. Introduction & Privacy Commitment

Welcome to **Lyfe / My Organiser** ("the App", "we", "us", or "our"). We are committed to protecting your personal data and upholding your right to privacy. 

Our application is built on an **Offline-First, Privacy-by-Design** architectural model:
- **Local Device Storage:** Your personal daily schedules, reminders, important dates, notes, audio recordings, contact cards, and vault documents are stored and processed **locally on your device** in a sandboxed local database.
- **No Remote Tracking or Profiling:** We do not track your activity across apps or websites, do not sell your personal data, and do not maintain cloud user profile tracking servers.

---

## 2. Information We Collect and Process

### A. Information Stored Exclusively on Your Device
- **Daily Schedules & Tasks:** Task titles, time slots, priorities, and completion statuses.
- **Important Dates & Reminders:** Birthday, anniversary, and customized reminder dates with alarm notifications.
- **My Vault Documents & Files:** Encrypted local storage for uploaded documents, PDFs, photos, and scanned items.
- **Contact Cards & QR Codes:** Contact names, phone numbers, notes, and generated/scanned QR codes.
- **Biometric Authentication Records:** Utilizes the Android `BiometricPrompt` system API (Fingerprint/Face Unlock) to secure access to the Vault. **Biometric data is processed solely by the device hardware security module (TEE/Secure Enclave) and is never accessible to or transmitted by the App.**

### B. Device Permissions Requested (On-Device Use Only)
- **Camera (`CAMERA`):** Used exclusively when you choose to take photos for the Vault/Image Studio or scan QR codes.
- **Microphone / Speech Recognition (`RECORD_AUDIO`):** Used solely when you tap the microphone button for speech-to-text prompt input. Audio is transcribed via device speech services and is not stored remotely.
- **Storage / Media Access:** Used to let you select images or documents to import into your Vault.
- **Notifications (`POST_NOTIFICATIONS`):** Used to display scheduled reminder alarms and timer notifications.

### C. Third-Party AI Services (Optional User-Initiated Features)
- **AI Image Studio:** When you explicitly request image generation or prompt transformation in the Image Studio, the prompt text and selected style preferences are transmitted securely (over HTTPS/TLS) to external AI inference services (e.g., Pollinations AI / Google Gemini API) to generate your image. No account identifiers, contacts, or vault records are attached to these requests.

---

## 3. European Union & UK General Data Protection Regulation (GDPR / UK GDPR)

If you are a resident of the European Economic Area (EEA), European Union (EU), or United Kingdom (UK), the following provisions apply to you under Regulation (EU) 2016/679 (GDPR) and the UK Data Protection Act:

### A. Legal Bases for Processing (Article 6 GDPR)
- **Contractual Necessity / Performance:** Storing and organizing your schedule, tasks, and documents as requested by you.
- **Consent (Article 6(1)(a)):** For optional camera/microphone access, AI image generation requests, and optional notification delivery. You may revoke consent at any time in your Android system settings.
- **Legitimate Interests (Article 6(1)(f)):** Ensuring application stability, local database integrity, and device-level data protection.

### B. Your GDPR Data Subject Rights
Under Articles 15 through 22 of the GDPR, you have the following rights:
1. **Right of Access (Article 15):** You have full access to all your personal data directly within the application's screens and local backup manager.
2. **Right to Rectification (Article 16):** You can edit or update any entry, task, reminder, or contact at any time directly in the user interface.
3. **Right to Erasure / "Right to be Forgotten" (Article 17):** You can permanently delete any document, schedule item, or vault record at any time. You can also delete all app data completely via Android Settings > Apps > Lyfe > Clear Storage / Uninstall.
4. **Right to Data Portability (Article 20):** You can export your entire data archive using the in-app Backup & Export feature.
5. **Right to Restriction of Processing (Article 18) & Right to Object (Article 21):** Because processing occurs locally on your device under your direct control, you can cease processing at any time by modifying settings or uninstalling the app.
6. **Right to Withdraw Consent:** You can disable permissions (Camera, Microphone, Notifications) at any time through Android Device Settings.
7. **Right to Lodge a Complaint:** You have the right to lodge a complaint with a Data Protection Supervisory Authority in your EU/EEA member state or the UK Information Commissioner's Office (ICO).

### C. International Data Transfers
When using AI generation features, prompts are sent encrypted via HTTPS to server endpoints. If servers are located outside the EEA/UK, transfers comply with adequacy decisions or Standard Contractual Clauses (SCCs).

---

## 4. United States Privacy Rights (CCPA / CPRA & State Privacy Laws)

If you are a resident of California, Virginia, Colorado, Connecticut, Utah, or other US states with applicable privacy regulations, this section describes your rights:

### A. California Consumer Privacy Act (CCPA) / California Privacy Rights Act (CPRA)
- **Categories of Information Collected:** Identifiers (contact names entered by you), Audio/Visual Information (photos/speech for prompt entry, processed on-device), and Sensitive Personal Information (biometric unlock via system API).
- **No Sale or Sharing of Personal Information:** We **DO NOT SELL** your personal information and **DO NOT SHARE** your personal information for cross-context behavioral advertising (as defined under the CCPA/CPRA).
- **Right to Know and Access:** You have the right to know what personal data is processed and to access it within the application.
- **Right to Delete:** You can delete your personal data directly in the app or by clearing app storage.
- **Right to Correct:** You can correct inaccurate personal information directly through the in-app edit tools.
- **Right to Limit Use of Sensitive Personal Information:** Sensitive biometrics are never collected by our servers.
- **Non-Discrimination:** We will never discriminate against you for exercising any of your privacy rights.

### B. Children’s Online Privacy Protection Act (COPPA)
Our App is not directed to children under the age of 13. We do not knowingly collect personal information from children under 13.

---

## 5. Data Security & Storage Architecture

- **Local Sandbox:** All files and SQLite/Room databases are protected by Android's application sandboxing, preventing unauthorized third-party apps from reading your data.
- **Biometric Security:** Vault authentication is verified via Android KeyStore hardware cryptographic primitives.
- **Encryption in Transit:** Any optional API communications (e.g., AI image creation) utilize standard TLS 1.3 encryption.

---

## 6. Data Retention & Deletion

- Your data remains stored exclusively on your device for as long as you maintain the application.
- **Instant Deletion:** Deleting an item in the app immediately purges it from the local database.
- **Complete App Reset:** Clearing storage or uninstalling the app permanently purges all local data.

---

## 7. Changes to This Privacy Policy

We may update our Privacy Policy periodically to reflect new features or legal requirements. Updated versions will be published with a revised "Last Updated" date.

---

## 8. Data Controller & Contact Information

If you have questions, comments, or requests regarding this Privacy Policy or your data protection rights under GDPR, CCPA, or other regional regulations, please contact:

- **Data Controller:** Lyfe Application Team
- **Contact Email:** `anuakku20138@gmail.com`
- **Official Policy URL:** `https://ais-pre-7va6et5cmfr2bqdzzsb25k-202411574583.asia-southeast1.run.app/privacy-policy.html`
- **Application:** Lyfe / My Organiser for Android
