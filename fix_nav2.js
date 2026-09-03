const fs = require('fs');
let content = fs.readFileSync('/tmp/BinderNavigation.kt', 'utf8');

// 1. Fix NavWaterFlowBackground to accept modifier
content = content.replace(/fun NavWaterFlowBackground\(\) \{/g, 'fun NavWaterFlowBackground(modifier: Modifier = Modifier) {');
content = content.replace(/Canvas\(modifier = Modifier\.fillMaxSize\(\)\) \{/g, 'Canvas(modifier = modifier) {');

// 2. Pass matchParentSize() in LedgerBinderBottomBar and LedgerBinderNavRail
content = content.replace(/Box\(modifier = Modifier\.fillMaxWidth\(\)\) \{\s*NavWaterFlowBackground\(\)/g, 'Box(modifier = Modifier.fillMaxWidth()) {\n            NavWaterFlowBackground(modifier = Modifier.matchParentSize())');
content = content.replace(/Box\(modifier = Modifier\.fillMaxHeight\(\)\) \{\s*NavWaterFlowBackground\(\)/g, 'Box(modifier = Modifier.fillMaxHeight()) {\n            NavWaterFlowBackground(modifier = Modifier.matchParentSize())');

// 3. Fix BinderTabItem to remove text and spacer, and adjust padding
const oldBinderTabItem = `@Composable
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
            .testTag("nav_tab_\${section.name.lowercase()}"),
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
}`;

const newBinderTabItem = `@Composable
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
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("nav_tab_\${section.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getSectionIcon(section, isSelected),
            contentDescription = section.tabLabel,
            tint = textColor,
            modifier = Modifier.size(26.dp)
        )
    }
}`;
content = content.replace(oldBinderTabItem, newBinderTabItem);

// 4. Update LedgerBinderBottomBar to use Arrangement.SpaceEvenly
content = content.replace(/horizontalArrangement = Arrangement\.SpaceAround,/g, 'horizontalArrangement = Arrangement.SpaceEvenly,');

// 5. Update LedgerBinderNavRail to only show icons
const navRailColumnOld = `                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = getSectionIcon(section, isSelected),
                                contentDescription = section.tabLabel,
                                tint = if (isSelected) getSectionColor(section) else Gray500,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.tabLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) getSectionColor(section) else Gray500
                            )
                        }`;

const navRailColumnNew = `                        Icon(
                                imageVector = getSectionIcon(section, isSelected),
                                contentDescription = section.tabLabel,
                                tint = if (isSelected) getSectionColor(section) else Gray500,
                                modifier = Modifier.size(26.dp)
                            )`;

content = content.replace(navRailColumnOld, navRailColumnNew);

// Also remove the local LedgerSection enum from /tmp/BinderNavigation.kt since that caused issues before
content = content.replace(/enum class LedgerSection[\s\S]*?SOCIAL\("Social"\)\n\}/, 'import com.example.ui.LedgerSection');
content = content.replace(/Icons\.AutoMirrored\.Filled\.MenuBook/g, 'Icons.Filled.MenuBook');
content = content.replace(/Icons\.AutoMirrored\.Outlined\.MenuBook/g, 'Icons.Outlined.MenuBook');

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
