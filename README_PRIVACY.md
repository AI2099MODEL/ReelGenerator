# MyLyfe Privacy Policy & Data Protection Overview

*Last updated: August 2026*

This Privacy Policy explains how **MyLyfe** collects, uses, and safeguards user data. MyLyfe is engineered as an on-device personal communication ledger, encrypted vault, task organizer, and diary application built around privacy-by-design principles.

---

## 1. Core Privacy Architecture: On-Device & Local-First

- **Zero Remote Telemetry & Tracking**: MyLyfe does not maintain central servers that harvest, track, profile, or sell user chat transcripts, contact lists, notes, or uploaded documents.
- **Local Sandbox Storage**: All chat messages, vaulted files, phone contacts, events, and personal notes are stored locally in an encrypted/sandboxed SQLite (Room) database on your Android device.
- **Offline Operations**: All features—including search, category filtering, message archiving, and directory management—function 100% offline without requiring continuous internet access.

---

## 2. Regulatory Compliance (GDPR, CCPA/CPRA, & Local Data Protection Acts)

- **Right to Access & Portability (GDPR Art. 20, CCPA § 1798.100)**: Users can export their complete database into a standardized, unencrypted or password-secured JSON backup file at any time.
- **Right to Erasure / "Right to be Forgotten" (GDPR Art. 17, CCPA § 1798.105)**: Users can permanently delete individual messages, entire conversation categories, vault documents, contacts, or wipe their entire dataset using the in-app backup/restore tools.
- **Data Minimization & Auto-Archiving (GDPR Art. 5(1)(c))**: An automatic 5-day chat archiver moves messages older than 5 days out of active chat views into a protected hidden Vault section, preventing unwanted message buildup.

---

## 3. Communication Limits, Storage Rules, & File Uploads

To maintain device responsiveness, prevent database memory bloat, and protect system resources, MyLyfe enforces transparent operational boundaries:

### A. Four Core Categories & Custom Naming
- MyLyfe maintains **4 core organizational categories** (by default: *Family*, *Work*, *Personal*, and *Utility*).
- Users have full autonomy to rename any category (and customize thread names and emojis) to suit their personal workflow.

### B. Directory Limit (10 Numbers per Category)
- Each category supports up to **10 contact numbers** in this release (maximum 40 total categorized speed-dial entries).
- This prevents contact database fragmentation and keeps quick-dial drawers fast and accessible.

### C. Chat Attachment & File Upload Limits
Attachments can be shared directly into any chat thread subject to the following standard limits:
- **Images (JPG, PNG, WEBP, GIF)**: Up to **25 MB** per file.
- **Videos (MP4, MKV, 3GP, MOV)**: Up to **100 MB** per file (in line with modern private messenger standards such as Signal/Messenger).
- **Documents & Audio (PDF, DOCX, TXT, MP3, WAV)**: Up to **50 MB** per file.
- **Max Aggregate File Size per Attachment**: **100 MB**.

---

## 4. Permissions & Device Capabilities

MyLyfe only requests permissions strictly necessary for user-initiated actions:
1. **Camera (`android.permission.CAMERA`)**: Used exclusively when you open the QR Scanner to scan contact cards (`MECARD`, `VCARD`) or documents. No video or viewfinder frames are ever recorded or transmitted.
2. **Storage / Photo Picker (`ACTION_GET_CONTENT` / SAF)**: Used solely to select photos, videos, documents, or JSON backups from your local file system.
3. **Phone Dialer (`Intent.ACTION_DIAL`)**: Opens the native Android dialer with the phone number pre-filled. MyLyfe never initiates unauthorized background phone calls.
4. **Network State (`ACCESS_NETWORK_STATE`)**: Used to detect whether your device has cellular signal or Wi-Fi to synchronize locally queued offline messages.

---

## 5. Security & Backups

- **Encrypted Local Storage**: Data is protected by Android's application sandbox and Linux user isolation.
- **User-Controlled Google Drive / Local Backups**: Backups are only generated when explicitly requested by the user and can be shared to Google Drive or local storage using the Android system share sheet.

---

## 6. Contact & Support

For privacy inquiries, audit questions, or feature requests, contact the development team or refer to the project documentation within the app settings.
