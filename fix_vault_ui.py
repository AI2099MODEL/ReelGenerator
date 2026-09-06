import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# Add Tab import if missing
if 'import androidx.compose.material3.Tab' not in content:
    content = content.replace('import androidx.compose.material3.Surface', 'import androidx.compose.material3.Surface\nimport androidx.compose.material3.Tab')

if 'import androidx.compose.material3.HorizontalDivider' not in content:
    content = content.replace('import androidx.compose.material3.Divider', 'import androidx.compose.material3.Divider\nimport androidx.compose.material3.HorizontalDivider')

# Fix Category Tabs in the main list
old_tabs = """            // Category Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = vaultTabs.indexOf(selectedTab),
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {}
                ) {
                    vaultTabs.forEachIndexed { index, tabName ->
                        val isSelected = selectedTab == tabName
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTab = tabName },
                            color = if (isSelected) GoldPrimary else Color(0xFF100D18),
                            border = BorderStroke(1.dp, if (isSelected) GoldHighlight else GoldAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = tabName,
                                color = if (isSelected) Color(0xFF241400) else GoldLight,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }"""

new_tabs = """            // Category Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = vaultTabs.indexOf(selectedTab),
                    containerColor = Color.Transparent,
                    contentColor = GoldPrimary,
                    edgePadding = 8.dp,
                    divider = {
                        androidx.compose.material3.HorizontalDivider(color = GoldAccent.copy(alpha = 0.2f))
                    }
                ) {
                    vaultTabs.forEachIndexed { index, tabName ->
                        val isSelected = selectedTab == tabName
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tabName },
                            text = { 
                                Text(
                                    text = tabName, 
                                    color = if (isSelected) GoldPrimary else GoldLight.copy(alpha = 0.7f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }"""

content = content.replace(old_tabs, new_tabs)

# Fix Category Selection in the Add Document Dialog
old_dialog_cats = """                        // Category Selection
                        Text("Category:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").forEach { cat ->
                                val isSel = docCategory == cat
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { docCategory = cat },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) GoldPrimary else Color(0xFF100D18),
                                    border = BorderStroke(0.8.dp, if (isSel) GoldHighlight else GoldAccent.copy(alpha = 0.35f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = cat,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color(0xFF241400) else GoldLight
                                        )
                                    }
                                }
                            }
                        }"""

new_dialog_cats = """                        // Category Selection
                        Text("Category:", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        ScrollableTabRow(
                            selectedTabIndex = listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").indexOf(docCategory),
                            containerColor = Color.Transparent,
                            contentColor = GoldPrimary,
                            edgePadding = 0.dp,
                            divider = {
                                androidx.compose.material3.HorizontalDivider(color = GoldAccent.copy(alpha = 0.2f))
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
                        }"""

content = content.replace(old_dialog_cats, new_dialog_cats)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
