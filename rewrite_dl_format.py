with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# Change GridCells.Fixed(2) to GridCells.Fixed(1)
content = content.replace('columns = GridCells.Fixed(2),', 'columns = GridCells.Fixed(1),')

# Replace VaultDocumentCard
old_card_start = content.find('@Composable\nfun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {')

if old_card_start != -1:
    new_card_code = """@Composable
fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {
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
            // Background subtle gradient/pattern to look like a secure ID
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
            
            // Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left side: ID Photo placeholder
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
                
                // Right side: Data
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.65f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header: Category and delete button
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
                        // Title
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
                        
                        // ID / Number (simulated from notes or just placeholder if none)
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
                            // Dummy lines to make it look like an ID if no notes
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
                    
                    // Footer (issue date / filetype)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "<<<<<<<<<<<<<<<<<<<<",
                            color = GoldPrimary.copy(alpha = 0.3f),
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}
"""
    content = content[:old_card_start] + new_card_code

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
