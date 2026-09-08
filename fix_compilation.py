import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# 1. Fix missing closing bracket in VaultScreen
content = content.replace("    } else {\n        VaultContentScreen(\n            vaultDocs = vaultDocs,\n            onAddDocument = onAddDocument,\n            onDeleteDocument = onDeleteDocument,\n            onLockVault = { isLocked = true },\n            onHomeClick = onHomeClick,\n            onMenuClick = onMenuClick,\n            modifier = modifier\n        )\n}", "    } else {\n        VaultContentScreen(\n            vaultDocs = vaultDocs,\n            onAddDocument = onAddDocument,\n            onDeleteDocument = onDeleteDocument,\n            onLockVault = { isLocked = true },\n            onHomeClick = onHomeClick,\n            onMenuClick = onMenuClick,\n            modifier = modifier\n        )\n    }\n}")

# Let's fix the idSurname etc. which are somehow referenced but undefined
# We need to make sure we don't have idSurname in the whole file
content = re.sub(r'idSurname\s*=\s*""', '', content)
content = re.sub(r'idGivenName\s*=\s*""', '', content)
content = re.sub(r'idNationality\s*=\s*""', '', content)
content = re.sub(r'idDOB\s*=\s*""', '', content)
content = re.sub(r'idSex\s*=\s*""', '', content)
content = re.sub(r'idPlaceOfBirth\s*=\s*""', '', content)
content = re.sub(r'idNumber\s*=\s*""', '', content)
content = re.sub(r'idAddress\s*=\s*""', '', content)
content = re.sub(r'idValidity\s*=\s*""', '', content)

# Check for VaultDocumentCard usage
# It was deleted? No, we had VaultDocumentCard at the bottom, but wait, the compile error says "Unresolved reference 'VaultDocumentCard'"
# That means I might have accidentally deleted it! Let's just create a very simple VaultDocumentCard at the end.

card_impl = """
@Composable
fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.58f),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E182A), // Darker base for contrast
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = doc.category.uppercase(),
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 8.dp, y = (-8).dp)
                        ) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF8080).copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = "DOCUMENT NAME",
                                color = GoldLight.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = doc.title,
                                color = GoldHighlight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        if (doc.notes.isNotBlank()) {
                            Column {
                                Text(
                                    text = "DETAILS / NOTES",
                                    color = GoldLight.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = doc.notes,
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "DATE ADDED",
                                color = GoldLight.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(doc.dateAddedTimestamp)),
                                color = GoldLight.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = doc.fileType,
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .background(Color(0xFF2A2438), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
"""

# Let's replace whatever was left of VaultDocumentCard (if any) with this one.
content = re.sub(r'@Composable\s*fun VaultDocumentCard.*', card_impl, content, flags=re.DOTALL)

if "fun VaultDocumentCard" not in content:
    content += "\n" + card_impl

# AlertDialog errors: "Syntax error: Expecting ')'"
# "Expecting an element"
# Let's check AlertDialog implementation in the content, it's probably missing a bracket or something.
# We'll just replace the whole Add Document Dialog.
# I'll find `if (showAddDialog) {` and replace up to `}` of the if block.

dialog_pattern = r'if \(showAddDialog\) \{.*?AlertDialog.*?\}\s*\}\s*\}'

new_dialog = """        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddDialog = false 
                    docNotes = "" 
                },
                containerColor = Color(0xFF1E182A),
                titleContentColor = GoldPrimary,
                textContentColor = GoldLight,
                title = { Text("Add Document to Vault", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isScanning = true
                                    currentCameraUri = createCameraUri()
                                    cameraLauncher.launch(currentCameraUri!!)
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = {
                                    isScanning = true
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Image, contentDescription = "Gallery", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { docTitle = it },
                            label = { Text("Document Title *", color = GoldLight.copy(alpha = 0.8f)) },
                            placeholder = { Text("e.g. Passport Copy, House Deed", color = GoldLight.copy(alpha = 0.4f)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoldHighlight,
                                unfocusedTextColor = GoldLight,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFF100D18),
                                unfocusedContainerColor = Color(0xFF161224)
                            )
                        )
                        
                        Text("Category:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ScrollableTabRow(
                            selectedTabIndex = listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").indexOf(docCategory),
                            containerColor = Color.Transparent,
                            contentColor = GoldPrimary,
                            edgePadding = 0.dp,
                            divider = {
                                HorizontalDivider(color = GoldAccent.copy(alpha = 0.2f))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").forEach { cat ->
                                val isSel = docCategory == cat
                                Tab(
                                    selected = isSel,
                                    onClick = { docCategory = cat },
                                    text = {
                                         Text(
                                            text = cat, 
                                            color = if (isSel) GoldPrimary else GoldLight.copy(alpha = 0.7f),
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = docNotes,
                            onValueChange = { docNotes = it },
                            label = { Text("Extracted Text / Notes", color = GoldLight.copy(alpha = 0.8f)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoldHighlight,
                                unfocusedTextColor = GoldLight,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFF100D18),
                                unfocusedContainerColor = Color(0xFF161224)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (docTitle.isNotBlank()) {
                                val filename = "${docTitle.trim().replace(" ", "_")}.${docType.lowercase()}"
                                val finalNotes = docNotes.trim()
                                
                                onAddDocument(
                                    docTitle.trim(),
                                    filename,
                                    "file://vault/$filename",
                                    docType,
                                    docCategory,
                                    1024L,
                                    finalNotes
                                )
                                notificationService.show("Success", "Saved to Documents/My Organiser/Vault", NotificationType.SUCCESS)
                                showAddDialog = false
                                docNotes = ""
                            }
                        },
                        enabled = docTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF241400))
                    ) {
                        Text("Save to Vault", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAddDialog = false
                        docNotes = ""
                    }) {
                        Text("Cancel", color = GoldLight)
                    }
                }
            )
        }
    }
}
"""

# Replace the whole if(showAddDialog) {} and close VaultContentScreen
content = re.sub(r'if\s*\(showAddDialog\)\s*\{.*?\}\s*\}\s*\}\s*$', new_dialog + "\n" + card_impl, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
