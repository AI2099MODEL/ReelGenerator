package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.InputStream
import java.util.EnumMap

data class DecodedQrResult(
    val rawText: String,
    val isContact: Boolean = false,
    val contactName: String = "",
    val contactPhone: String = "",
    val contactCategory: String = "Family",
    val contactNote: String = "",
    val isUrl: Boolean = false,
    val title: String = "Decoded QR Code"
)

object QRCodeDecoder {

    fun decodeFromUri(context: Context, uri: Uri): DecodedQrResult? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                decodeFromBitmap(bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeFromBitmap(bitmap: Bitmap): DecodedQrResult? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }

        val reader = MultiFormatReader()
        return try {
            val result = reader.decode(binaryBitmap, hints)
            parsePayload(result.text)
        } catch (e: NotFoundException) {
            // Try inverted source if standard decode failed
            try {
                val invertedSource = source.invert()
                val invertedBinaryBitmap = BinaryBitmap(HybridBinarizer(invertedSource))
                val result = reader.decode(invertedBinaryBitmap, hints)
                parsePayload(result.text)
            } catch (ex: Exception) {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parsePayload(raw: String): DecodedQrResult {
        val trimmed = raw.trim()

        // 1. MECARD Format: MECARD:N:John Doe;TEL:+123456789;NOTE:Friend;;
        if (trimmed.startsWith("MECARD:", ignoreCase = true)) {
            var name = ""
            var phone = ""
            var note = ""
            var category = "Personal"

            val body = trimmed.substringAfter("MECARD:").trimEnd(';')
            val parts = body.split(";")
            for (part in parts) {
                val p = part.trim()
                if (p.startsWith("N:", ignoreCase = true)) {
                    name = p.substring(2).trim()
                } else if (p.startsWith("TEL:", ignoreCase = true)) {
                    phone = p.substring(4).trim()
                } else if (p.startsWith("NOTE:", ignoreCase = true)) {
                    note = p.substring(5).trim()
                } else if (p.startsWith("ORG:", ignoreCase = true)) {
                    category = "Work"
                    if (note.isEmpty()) note = p.substring(4).trim()
                }
            }

            return DecodedQrResult(
                rawText = raw,
                isContact = true,
                contactName = name.ifBlank { "Scanned Contact" },
                contactPhone = phone,
                contactCategory = category,
                contactNote = note,
                title = "Contact Card: ${name.ifBlank { phone }}"
            )
        }

        // 2. VCARD Format: BEGIN:VCARD ... FN:John ... TEL: ... END:VCARD
        if (trimmed.contains("BEGIN:VCARD", ignoreCase = true)) {
            var name = ""
            var phone = ""
            var note = ""
            var category = "Personal"

            val lines = trimmed.lines()
            for (line in lines) {
                val l = line.trim()
                if (l.startsWith("FN:", ignoreCase = true) || l.startsWith("N:", ignoreCase = true)) {
                    if (name.isBlank()) name = l.substringAfter(":").trim()
                } else if (l.startsWith("TEL", ignoreCase = true)) {
                    if (phone.isBlank()) phone = l.substringAfter(":").trim()
                } else if (l.startsWith("NOTE:", ignoreCase = true)) {
                    note = l.substringAfter(":").trim()
                } else if (l.startsWith("ORG:", ignoreCase = true)) {
                    category = "Work"
                }
            }

            return DecodedQrResult(
                rawText = raw,
                isContact = true,
                contactName = name.ifBlank { "Scanned Contact" },
                contactPhone = phone,
                contactCategory = category,
                contactNote = note,
                title = "Contact vCard: ${name.ifBlank { phone }}"
            )
        }

        // 3. TEL Link: tel:+1234567890
        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.substring(4).trim()
            return DecodedQrResult(
                rawText = raw,
                isContact = true,
                contactName = "Mobile Number",
                contactPhone = phone,
                contactCategory = "Personal",
                contactNote = "Imported from QR scan",
                title = "Mobile Number: $phone"
            )
        }

        // 4. Check if raw text looks like a phone number: e.g. "+1 (555) 123-4567" or "9876543210"
        val cleanDigits = trimmed.filter { it.isDigit() }
        if (cleanDigits.length in 7..15 && trimmed.all { it.isDigit() || it in "+ -()." }) {
            return DecodedQrResult(
                rawText = raw,
                isContact = true,
                contactName = "Mobile Contact",
                contactPhone = trimmed,
                contactCategory = "Family",
                contactNote = "Direct QR phone number",
                title = "Mobile Number: $trimmed"
            )
        }

        // 5. URL
        val isUrl = trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)

        return DecodedQrResult(
            rawText = raw,
            isContact = false,
            isUrl = isUrl,
            title = if (isUrl) "Scanned Web Link" else "Scanned QR Text"
        )
    }
}
