package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.DecodedQrResult
import com.example.util.QRCodeGenerator

@Composable
fun QrResultDialog(
    result: DecodedQrResult,
    onDismiss: () -> Unit,
    onAddContact: (category: String, name: String, phone: String, note: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(result.contactName.ifBlank { "Scanned Contact" }) }
    var phone by remember { mutableStateOf(result.contactPhone) }
    var note by remember { mutableStateOf(result.contactNote.ifBlank { if (!result.isContact) result.rawText else "" }) }
    var category by remember { mutableStateOf(result.contactCategory.ifBlank { "Family" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoseQuartzContainerLowest,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = RoseQuartzPrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (result.isContact || phone.isNotBlank()) Icons.Filled.ContactPhone else Icons.Filled.QrCode,
                            contentDescription = null,
                            tint = RoseQuartzPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Scanned QR Code Result",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scanned Payload Summary
                Text(
                    text = "Scanned Content / Payload:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = RoseQuartzBg,
                    border = BorderStroke(1.dp, RoseQuartzContainerHighest)
                ) {
                    Text(
                        text = result.rawText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RoseQuartzTextPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (result.isUrl) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.rawText))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open URL in Browser")
                    }
                }

                Divider(color = RoseQuartzContainerHighest, thickness = 1.dp)

                Text(
                    text = "Add Mobile Number & Contact Details (Aligned with QR):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary
                )

                // Category Pill Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Family", "Work", "Personal", "Utility").forEach { cat ->
                        val isSel = category.equals(cat, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { category = cat },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) RoseQuartzPrimary else RoseQuartzBg,
                            border = BorderStroke(1.dp, if (isSel) RoseQuartzPrimary else RoseQuartzContainerHighest)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                val emoji = when (cat) {
                                    "Family" -> "🏡"
                                    "Work" -> "💼"
                                    "Personal" -> "🌿"
                                    else -> "⚡"
                                }
                                Text(text = emoji, fontSize = 13.sp)
                                Text(
                                    text = cat,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) RoseQuartzOnPrimary else RoseQuartzTextPrimary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseQuartzPrimary,
                        unfocusedBorderColor = RoseQuartzContainerHighest
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseQuartzPrimary,
                        unfocusedBorderColor = RoseQuartzContainerHighest
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Tag (QR Payload)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoseQuartzPrimary,
                        unfocusedBorderColor = RoseQuartzContainerHighest
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAddContact(category, name, phone, note)
                        Toast.makeText(context, "Added $name ($phone) to $category", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Please enter both contact name and mobile number", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Mobile Numbers")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Scanned QR Text", result.rawText))
                Toast.makeText(context, "Copied QR content to clipboard", Toast.LENGTH_SHORT).show()
                onDismiss()
            }) {
                Text("Copy Raw Text", color = RoseQuartzTextMuted)
            }
        }
    )
}

@Composable
fun ContactQrDisplayDialog(
    name: String,
    phoneNumber: String,
    category: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrPayload = "MECARD:N:$name;TEL:$phoneNumber;NOTE:Category: $category;;"
    val qrBitmap = remember(qrPayload) {
        QRCodeGenerator.generateQrCodeBitmap(qrPayload, size = 512)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoseQuartzContainerLowest,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.QrCode2, contentDescription = null, tint = RoseQuartzPrimary)
                Text(
                    text = "Contact QR Code",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = RoseQuartzTextPrimary,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Anyone can scan this QR code from their mobile phone or laptop to import this contact directly into their directory.",
                    fontSize = 11.sp,
                    color = RoseQuartzTextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, RoseQuartzContainerHighest),
                    modifier = Modifier.size(200.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "Contact QR Code",
                                modifier = Modifier.size(180.dp)
                            )
                        } else {
                            CircularProgressIndicator(color = RoseQuartzPrimary)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RoseQuartzTextPrimary
                    )
                    Text(
                        text = "$phoneNumber • $category",
                        fontSize = 12.sp,
                        color = RoseQuartzTextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Contact Details", "$name: $phoneNumber ($category)"))
                    Toast.makeText(context, "Copied contact info to clipboard", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseQuartzPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Details")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RoseQuartzTextMuted)
            }
        }
    )
}
