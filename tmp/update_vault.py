import sys

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

old_dialog_start = '        // ==========================================\n        // UI FORM: Upload Document Dialog'
old_dialog_end = '                                Spacer(modifier = Modifier.width(6.dp))\n                                Text("Upload Document", fontWeight = FontWeight.Bold)\n                            }\n                        }\n                    }\n                }\n            }\n        }'

new_dialog = '''        // ==========================================
        // UI FORM: Upload Document Dialog
        // Blue colored and handwriting cursive style matching Reminders plus button dialog
        // ==========================================
        if (showUploadDialog) {
            val dialogBackgroundBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFC),
                    Color(0xFFEFF6FF)
                )
            )

            Dialog(
                onDismissRequest = {
                    showUploadDialog = false
                    resetForm()
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(26.dp),
                                ambientColor = Color(0xFF0284C7).copy(alpha = 0.25f),
                                spotColor = Color(0xFF0284C7).copy(alpha = 0.35f)
                            ),
                        shape = RoundedCornerShape(26.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFBAE6FD))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(dialogBackgroundBrush)
                                .padding(horizontal = 18.dp, vertical = 18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Dialog Header: Cloud icon in circular blue badge, Cursive title, and Close button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CloudUpload,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Upload Document",
                                            color = Color(0xFF0369A1),
                                            fontSize = 22.sp,
                                            fontFamily = FontFamily.Cursive,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            showUploadDialog = false
                                            resetForm()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Close",
                                            tint = Color(0xFF0369A1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFBAE6FD).copy(alpha = 0.7f), thickness = 1.dp)

                                // Attach Files Section (Single button, up to 4 files)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Attach Files (Up to 4 files):",
                                        color = Color(0xFF0369A1),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Button(
                                        onClick = {
                                            if (selectedFiles.size >= 4) {
                                                notificationService.show(
                                                    "Limit Reached",
                                                    "Maximum 4 files reached. Remove a file to choose another.",
                                                    NotificationType.INFO
                                                )
                                            } else {
                                                try {
                                                    filePickerLauncher.launch("*/*")
                                                } catch (e: Exception) {
                                                    notificationService.show(
                                                        "File Picker Unavailable",
                                                        "Could not open file picker: ${e.localizedMessage ?: \\"No app available\\"}",
                                                        NotificationType.ALERT
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF0F9FF),
                                            contentColor = Color(0xFF0369A1)
                                        ),
                                        border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AddCircleOutline,
                                            contentDescription = "Add Files",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (selectedFiles.isEmpty()) "Select Files / Photos (Up to 4)" else "Add More Files (${selectedFiles.size}/4)",
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Cursive,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0369A1)
                                        )
                                    }
                                }

                                // Selected files list
                                if (selectedFiles.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Selected (${selectedFiles.size}/4):",
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Cursive,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7)
                                            )
                                            TextButton(
                                                onClick = { selectedFiles = emptyList() },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    "Clear all",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Cursive,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            }
                                        }

                                        selectedFiles.forEachIndexed { index, fileItem ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF0F9FF),
                                                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (fileItem.fileType.contains("IMAGE", ignoreCase = true)) Icons.Filled.Image else Icons.Filled.Description,
                                                            contentDescription = null,
                                                            tint = Color(0xFF0284C7),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(
                                                                text = fileItem.name,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color(0xFF0F172A),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "${fileItem.fileType} • ${formatFileSize(fileItem.size)}",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF64748B)
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedFiles = selectedFiles.filterIndexed { i, _ -> i != index }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Close,
                                                            contentDescription = "Remove file",
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Document Title Section with Cursive font
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Document Title *:",
                                        color = Color(0xFF0369A1),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = docTitle,
                                        onValueChange = { docTitle = it },
                                        placeholder = {
                                            Text(
                                                "Enter document name",
                                                color = Color(0xFF94A3B8),
                                                fontFamily = FontFamily.Cursive,
                                                fontSize = 15.sp
                                            )
                                        },
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Cursive,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF1E293B),
                                            focusedBorderColor = Color(0xFF0284C7),
                                            unfocusedBorderColor = Color(0xFFBAE6FD),
                                            focusedContainerColor = Color(0xFFF0F9FF),
                                            unfocusedContainerColor = Color(0xFFF8FAFC),
                                            cursorColor = Color(0xFF0284C7)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                // Notes Section with Cursive font
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Notes / Description (Optional):",
                                        color = Color(0xFF0369A1),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Cursive,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = docNotes,
                                        onValueChange = { docNotes = it },
                                        placeholder = {
                                            Text(
                                                "Add any notes about this document",
                                                color = Color(0xFF94A3B8),
                                                fontFamily = FontFamily.Cursive,
                                                fontSize = 15.sp
                                            )
                                        },
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Cursive,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 72.dp),
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF1E293B),
                                            focusedBorderColor = Color(0xFF0284C7),
                                            unfocusedBorderColor = Color(0xFFBAE6FD),
                                            focusedContainerColor = Color(0xFFF0F9FF),
                                            unfocusedContainerColor = Color(0xFFF8FAFC),
                                            cursorColor = Color(0xFF0284C7)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Action buttons with Cursive font
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            showUploadDialog = false
                                            resetForm()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.2.dp, Color(0xFFBAE6FD)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0369A1))
                                    ) {
                                        Text(
                                            "Cancel",
                                            color = Color(0xFF0369A1),
                                            fontFamily = FontFamily.Cursive,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (docTitle.isNotBlank()) {
                                                val cleanTitle = docTitle.trim()
                                                val cleanFile = if (selectedFiles.isNotEmpty()) {
                                                    selectedFiles.joinToString("||") { it.name }
                                                } else {
                                                    "${cleanTitle.replace(\\" \\", \\"_\\")}.pdf"
                                                }
                                                val uriStr = if (selectedFiles.isNotEmpty()) {
                                                    selectedFiles.joinToString("||") { it.uri.toString() }
                                                } else {
                                                    "file://documents/$cleanFile"
                                                }
                                                val totalSize = selectedFiles.sumOf { it.size }
                                                val fileType = when {
                                                    selectedFiles.size > 1 -> "${selectedFiles.size} FILES"
                                                    selectedFiles.size == 1 -> selectedFiles.first().fileType
                                                    else -> "DOCUMENT"
                                                }

                                                onAddDocument(
                                                    cleanTitle,
                                                    cleanFile,
                                                    uriStr,
                                                    fileType,
                                                    "Document",
                                                    totalSize,
                                                    docNotes.trim()
                                                )
                                                val fileCountMsg = if (selectedFiles.size > 1) " (${selectedFiles.size} files)" else ""
                                                notificationService.show(
                                                    "Upload Successful",
                                                    "Saved \\"$cleanTitle\\"$fileCountMsg",
                                                    NotificationType.SUCCESS
                                                )
                                                showUploadDialog = false
                                                resetForm()
                                            }
                                        },
                                        enabled = docTitle.isNotBlank(),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = Color(0xFF0284C7).copy(alpha = 0.4f),
                                                spotColor = Color(0xFF0284C7).copy(alpha = 0.5f)
                                            ),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF0284C7),
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFFE0F2FE),
                                            disabledContentColor = Color(0xFF94A3B8)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CloudUpload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Save Document",
                                            color = Color.White,
                                            fontFamily = FontFamily.Cursive,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }'''

start_idx = content.find(old_dialog_start)
end_idx = content.find(old_dialog_end)

if start_idx == -1 or end_idx == -1:
    print('Indices not found!', start_idx, end_idx)
    sys.exit(1)

end_idx += len(old_dialog_end)
content = content[:start_idx] + new_dialog + content[end_idx:]

old_end_marker = '''                                Text("Delete", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}'''

new_fab_addition = '''                                Text("Delete", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Reminders-style Blue Floating Action Plus Button for Upload Document
        FloatingActionButton(
            onClick = {
                resetForm()
                showUploadDialog = true
            },
            containerColor = Color(0xFF0284C7),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .size(54.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF0284C7).copy(alpha = 0.35f),
                    spotColor = Color(0xFF0284C7).copy(alpha = 0.45f)
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Upload Document",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}'''

if old_end_marker in content:
    content = content.replace(old_end_marker, new_fab_addition, 1)
    print('FAB added successfully!')
else:
    print('FAB marker not found!')
    sys.exit(1)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
print('Done updating VaultScreen.kt!')
