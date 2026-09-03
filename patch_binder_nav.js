const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'utf8');

// Add getSectionColor function
const getSectionColorStr = `
private fun getSectionColor(section: LedgerSection): Color {
    return when (section) {
        LedgerSection.CHAT -> NavChatBlue
        LedgerSection.DIARY -> NavDiaryGold
        LedgerSection.EVENTS -> NavEventsEmerald
        LedgerSection.VAULT -> NavVaultCrimson
        LedgerSection.TASKS -> NavTasksViolet
    }
}
`;

if (!content.includes('fun getSectionColor')) {
    content = content.replace(/private fun getSectionIcon/, getSectionColorStr + '\nprivate fun getSectionIcon');
}

// Modify BinderTabItem colors
content = content.replace(
    /val bgColor by animateColorAsState\(\s*targetValue = if \(isSelected\) GlassNavActiveBg else Color\.Transparent,\s*animationSpec = tween\(durationMillis = 200\),\s*label = "bgColor"\s*\)/g,
    `val sectionColor = getSectionColor(section)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) sectionColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "bgColor"
    )`
);

content = content.replace(
    /val textColor by animateColorAsState\(\s*targetValue = if \(isSelected\) RoseCustom else Gray500,\s*animationSpec = tween\(durationMillis = 200\),\s*label = "textColor"\s*\)/g,
    `val textColor by animateColorAsState(
        targetValue = if (isSelected) sectionColor else Gray500,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )`
);

content = content.replace(
    /\.background\(RoseCustom, RoundedCornerShape\(bottomStart = 4\.dp, bottomEnd = 4\.dp\)\)/g,
    `.background(sectionColor, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))`
);

content = content.replace(
    /color = if \(isSelected\) RoseCustom else Gray500/g,
    `color = if (isSelected) sectionColor else Gray500`
);

// Modify LedgerBinderNavRail colors
content = content.replace(
    /color = if \(isSelected\) GlassNavActiveBg else Color\.Transparent,/g,
    `color = if (isSelected) getSectionColor(section).copy(alpha = 0.15f) else Color.Transparent,`
);

content = content.replace(
    /tint = if \(isSelected\) RoseCustom else Gray500,/g,
    `tint = if (isSelected) getSectionColor(section) else Gray500,`
);

// We need to fix the Text color in LedgerBinderNavRail too, but the regex above might catch it if it matched exactly, wait.
// Let's be safer and just write a custom replace for LedgerBinderNavRail surface & children.

fs.writeFileSync('app/src/main/java/com/example/ui/components/BinderNavigation.kt', content);
