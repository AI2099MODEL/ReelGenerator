import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# Add states for tabs
tabs_state = """    var selectedTab by remember { mutableStateOf("All") }
    val vaultTabs = listOf("All", "Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other")
    val filteredDocs = if (selectedTab == "All") vaultDocs else vaultDocs.filter { it.category == selectedTab }
"""
content = content.replace('    var showAddDialog by remember { mutableStateOf(false) }', '    var showAddDialog by remember { mutableStateOf(false) }\n' + tabs_state)

# Add TabRow UI
tab_row_ui = """
            // Category Tabs
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
            }
"""
content = content.replace('            // Vault Security Status Banner in Gold', '            // Vault Security Status Banner in Gold')
# insert tab_row_ui after the Security Status Banner item. 
# It's an item { Surface { Row { Row { Box { Icon ... Text "Bank-Grade..." } } } } }
# It's easier to find the end of the item.

content = re.sub(r'(Text\(\s*text = "Bank-Grade.*?\n.*?\}\n.*?\}\n.*?\}\n.*?\})', r'\1\n' + tab_row_ui, content, flags=re.DOTALL)

# Replace the items(vaultDocs) with items(filteredDocs)
content = content.replace('items(vaultDocs, key = { it.id })', 'items(filteredDocs, key = { it.id })')
content = content.replace('if (vaultDocs.isEmpty()) {', 'if (filteredDocs.isEmpty()) {')

# Update Add Dialog categories
content = content.replace('var docCategory by remember { mutableStateOf("Personal") }', 'var docCategory by remember { mutableStateOf("Passport") }')
content = content.replace('listOf("Personal", "Financial", "Legal", "Health").forEach { cat ->', 'listOf("Passport", "Driving Licence", "Aadhaar Card", "Voter Id", "Other").forEach { cat ->')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
