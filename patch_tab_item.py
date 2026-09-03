import re

with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'r') as f:
    content = f.read()

# Replace BinderTabItem
new_tab_item = """@Composable
private fun BinderTabItem(
    section: LedgerSection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionColor = getSectionColor(section)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) sectionColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) sectionColor else Gray500,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("nav_tab_${section.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getSectionIcon(section, isSelected),
                contentDescription = section.tabLabel,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = section.tabLabel,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}"""

content = re.sub(r'@Composable\s*private fun BinderTabItem\([\s\S]*?\}\s*\}', new_tab_item, content)

with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'w') as f:
    f.write(content)
