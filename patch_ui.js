const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// Modify LedgerBinderBottomBar to match the HTML layout exactly
content = content.replace(/fun LedgerBinderBottomBar\([\s\S]*?\/\/ Home Indicator imitation[\s\S]*?\}[\s\S]*?\}/, `fun LedgerBinderBottomBar(
    currentSection: LedgerSection,
    onSectionSelected: (LedgerSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = GlassBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LedgerSection.values().forEach { section ->
                val isSelected = currentSection == section
                BinderTabItem(
                    section = section,
                    isSelected = isSelected,
                    onClick = { onSectionSelected(section) }
                )
            }
        }
    }
}`);

// Modify BinderTabItem to match the HTML styles exactly
content = content.replace(/private fun BinderTabItem\([\s\S]*?val textColor by animateColorAsState\([\s\S]*?label = "textColor"\n    \)/, `private fun BinderTabItem(
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
`);

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
