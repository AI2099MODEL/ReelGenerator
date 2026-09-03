with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'r') as f:
    text = f.read()

nav_item_insert = """    NavTabItem(
        section = LedgerSection.RINGTONES,
        label = "Ringtones",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    ),"""

if "LedgerSection.RINGTONES" not in text:
    text = text.replace('        unselectedIcon = Icons.Outlined.MusicNote\n    ),', '        unselectedIcon = Icons.Outlined.MusicNote\n    ),\n' + nav_item_insert)

with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'w') as f:
    f.write(text)
