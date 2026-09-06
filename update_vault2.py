import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

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

content = content.replace('            if (filteredDocs.isEmpty()) {', tab_row_ui + '\n            if (filteredDocs.isEmpty()) {')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
