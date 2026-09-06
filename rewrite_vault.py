import sys

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# Make sure we have LazyVerticalGrid imports
if 'import androidx.compose.foundation.lazy.grid.' not in content:
    content = content.replace('import androidx.compose.foundation.lazy.LazyColumn', 'import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.grid.GridCells\nimport androidx.compose.foundation.lazy.grid.LazyVerticalGrid\nimport androidx.compose.foundation.lazy.grid.items\nimport androidx.compose.foundation.lazy.grid.GridItemSpan')

# Find the start of Scaffold
scaffold_start = content.find('    Scaffold(\n        topBar = {\n            LedgerTopHeader(\n                title = "Secure Vault",\n                onHomeClick = onHomeClick,')

if scaffold_start == -1:
    print("Could not find scaffold start")
    sys.exit(1)

dialog_start = content.find('    // Add Document Dialog', scaffold_start)
if dialog_start == -1:
    print("Could not find dialog start")
    sys.exit(1)

old_scaffold_code = content[scaffold_start:dialog_start]

new_scaffold_code = """    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(Color(0xFF100D18), Color(0xFF1B1428))
        )
    )) {
        Scaffold(
            topBar = {
                LedgerTopHeader(
                    title = "Secure Vault",
                    onHomeClick = onHomeClick,
                    onMenuClick = onMenuClick,
                    actionIcon = Icons.Filled.Add,
                    onActionClick = { showAddDialog = true }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Vault Security Status Banner in Gold
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x771A1526),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Vault is Secured", color = GoldHighlight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Encrypted local storage", color = GoldLight.copy(alpha = 0.7f), fontSize = 10.5.sp)
                            }
                        }
                        Button(
                            onClick = onLockVault,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF241400)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = "Lock", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Small elegant category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vaultTabs.forEach { tabName ->
                        val isSelected = selectedTab == tabName
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { selectedTab = tabName },
                            color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) GoldPrimary else GoldLight.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = tabName,
                                color = if (isSelected) GoldPrimary else GoldLight.copy(alpha = 0.7f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Documents Grid
                if (filteredDocs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = GoldLight.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Documents Found", color = GoldLight.copy(alpha = 0.5f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to securely scan or add IDs.", color = GoldLight.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredDocs) { doc ->
                            VaultDocumentCard(doc = doc, onDelete = { onDeleteDocument(doc) })
                        }
                    }
                }
            }
        }
    }
"""

content = content.replace(old_scaffold_code, new_scaffold_code)

old_item_code_start = content.find('fun VaultDocumentItem(doc: VaultDocumentEntity, onDelete: () -> Unit) {')
if old_item_code_start != -1:
    new_item_code = """@Composable
fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {
    val fileIcon = when (doc.fileType.uppercase()) {
        "PDF" -> Icons.Filled.PictureAsPdf
        "IMAGE" -> Icons.Filled.Image
        "RECEIPT" -> Icons.Filled.Receipt
        else -> Icons.Filled.Description
    }

    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.85f),
        shape = RoundedCornerShape(20.dp),
        color = Color(0x442A2438), // Soft glassmorphism dark
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.3f), GoldAccent.copy(alpha = 0.1f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        fileIcon,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).offset(x = 6.dp, y = (-6).dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete Document",
                        tint = Color(0xFFFF8080).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = doc.title,
                    color = GoldHighlight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = GoldPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = doc.category,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                if (doc.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = doc.notes,
                        color = GoldLight.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
"""
    # Find the end of the old function
    old_item_code_end = content.find('}\n\n', old_item_code_start)
    if old_item_code_end == -1:
        # try end of file
        old_item_code_end = content.find('}\n', old_item_code_start) + 2
        
    content = content[:old_item_code_start] + new_item_code + content[old_item_code_end:]

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
