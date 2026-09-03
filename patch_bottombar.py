with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'r') as f:
    text = f.read()

# First, modify the order of NAV_ITEMS. 
# We want: CHAT, DIARY, TASKS, EVENTS, SOCIAL, VAULT, MUSIC, RINGTONES
# Current order: CHAT, DIARY, TASKS, EVENTS, VAULT, MUSIC, RINGTONES, SOCIAL
import re

text = re.sub(r'(NavTabItem\(\s*section = LedgerSection\.SOCIAL.*?\n\s*\))', r'', text, flags=re.DOTALL)
# It will leave a trailing comma, so let's just rewrite NAV_ITEMS completely
nav_items_block = """private val NAV_ITEMS = listOf(
    NavTabItem(
        section = LedgerSection.CHAT,
        label = "Chitto",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline
    ),
    NavTabItem(
        section = LedgerSection.DIARY,
        label = "Diary",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
    ),
    NavTabItem(
        section = LedgerSection.TASKS,
        label = "Checklist",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ),
    NavTabItem(
        section = LedgerSection.EVENTS,
        label = "MyEvents",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    NavTabItem(
        section = LedgerSection.SOCIAL,
        label = "Studio",
        selectedIcon = Icons.Filled.Podcasts,
        unselectedIcon = Icons.Outlined.Podcasts
    ),
    NavTabItem(
        section = LedgerSection.VAULT,
        label = "Vault",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock
    ),
    NavTabItem(
        section = LedgerSection.MUSIC,
        label = "Music",
        selectedIcon = Icons.Filled.MusicNote,
        unselectedIcon = Icons.Outlined.MusicNote
    ),
    NavTabItem(
        section = LedgerSection.RINGTONES,
        label = "Ringtones",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )
)"""

text = re.sub(r'private val NAV_ITEMS = listOf\(.*?\)\s*\n\*', nav_items_block + '\n/**', text, flags=re.DOTALL)

new_bottom_bar_layout = """    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = RoseQuartzContainerLowest.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, RoseQuartzContainerHighest),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            NAV_ITEMS.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowItems.forEach { item ->
                        val isSelected = currentSection == item.section
                        val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                        val tint by animateColorAsState(
                            targetValue = if (isSelected) RoseQuartzPrimary else RoseQuartzTextSecondary,
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_tint"
                        )
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) RoseQuartzPrimaryContainer else Color.Transparent,
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_bg"
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSectionSelected(item.section) }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }"""

text = re.sub(r'    Surface\(\s*modifier = modifier.*?\}\s*\}\s*\}\s*\}', new_bottom_bar_layout, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'w') as f:
    f.write(text)
