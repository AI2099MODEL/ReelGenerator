import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# Replace the entire VaultDocumentCard
old_card = content[content.find("@Composable\nfun VaultDocumentCard("):content.find("@Composable\nfun IDTextField(")]

new_card = """@Composable
fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {
    var isIdCard = doc.category in listOf("Passport", "Driving Licence", "Aadhaar Card")
    var parsedJson: org.json.JSONObject? = null
    if (isIdCard && doc.notes.trim().startsWith("{")) {
        try {
            parsedJson = org.json.JSONObject(doc.notes)
        } catch (e: Exception) {}
    }

    if (isIdCard) {
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.58f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF9F5EC),
            border = BorderStroke(1.dp, Color(0xFFDCD2C6)),
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Delete button overlay
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFFF8080).copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
                }

                Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (doc.category == "Passport") Icons.Filled.Public else Icons.Filled.CreditCard, contentDescription = null, tint = Color(0xFF6C5B7B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(doc.category.uppercase(), color = Color(0xFF6C5B7B), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFDCD2C6))
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(0.35f).fillMaxHeight().background(Color(0xFFEBE3D5), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFC7B79E), modifier = Modifier.size(48.dp))
                        }
                        Column(modifier = Modifier.weight(0.65f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
                            IDCardField("ID NUMBER", parsedJson?.optString("ID Number") ?: "")
                            IDCardField(if (doc.category == "Passport") "SURNAME" else "NAME", parsedJson?.optString(if (doc.category == "Passport") "Surname" else "Name") ?: parsedJson?.optString("Surname") ?: "")
                            if (doc.category == "Passport") {
                                IDCardField("GIVEN NAME", parsedJson?.optString("Given Name") ?: "")
                                IDCardField("NATIONALITY", parsedJson?.optString("Nationality") ?: "")
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) { IDCardField("DOB", parsedJson?.optString("DOB") ?: "") }
                                Box(modifier = Modifier.weight(1f)) { IDCardField("SEX", parsedJson?.optString("Sex") ?: "") }
                            }
                            if (doc.category == "Passport") {
                                IDCardField("PLACE OF BIRTH", parsedJson?.optString("Place of Birth") ?: "")
                            }
                            if (doc.category == "Driving Licence") {
                                IDCardField("VALIDITY", parsedJson?.optString("Validity") ?: "")
                            }
                        }
                    }
                }
            }
        }
    } else {
        val fileIcon = when (doc.fileType.uppercase()) {
            "PDF" -> Icons.Filled.PictureAsPdf
            "IMAGE" -> Icons.Filled.Image
            "RECEIPT" -> Icons.Filled.Receipt
            else -> Icons.Filled.Description
        }
    
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.58f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E182A), // Darker base for contrast
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GoldPrimary.copy(alpha = 0.03f),
                                    Color.Transparent
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.35f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2438))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.PersonOutline,
                                contentDescription = null,
                                tint = GoldPrimary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(
                                fileIcon,
                                contentDescription = null,
                                tint = GoldPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.65f),
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
                            } else {
                                Column {
                                    Text(
                                        text = "DOCUMENT ID",
                                        color = GoldLight.copy(alpha = 0.5f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "XXXX-XXXX-XXXX",
                                        color = GoldLight.copy(alpha = 0.3f),
                                        fontSize = 12.sp,
                                        letterSpacing = 2.sp
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
}

@Composable
fun IDCardField(label: String, value: String) {
    Column {
        Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8C7A6B))
        Text(value.ifBlank { "—" }, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C241B), maxLines = 1, overflow = TextOverflow.Ellipsis)
        HorizontalDivider(color = Color(0xFFC7B79E), thickness = 0.5.dp, modifier = Modifier.padding(top=1.dp))
    }
}

"""

content = content.replace(old_card, new_card)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
