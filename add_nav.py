with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'r') as f:
    content = f.read()

new_tab = """    ),
    NavTabItem(
        section = LedgerSection.VAULT,
        label = "Vault",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock,
        tabColor = Color(0xFFFFD700)
    )
)"""

content = content.replace('        tabColor = Color(0xFFFFD700)\n    )\n)', new_tab)

with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'w') as f:
    f.write(content)
