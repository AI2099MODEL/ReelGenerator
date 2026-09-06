with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''        unselectedIcon = Icons.Outlined.AutoAwesome,
    ),
    NavTabItem(
        section = LedgerSection.VAULT,''',
'''        unselectedIcon = Icons.Outlined.AutoAwesome,
        tabColor = Color(0xFFFFD700)
    ),
    NavTabItem(
        section = LedgerSection.VAULT,'''
)

with open('app/src/main/java/com/example/ui/components/BinderNavigation.kt', 'w') as f:
    f.write(content)
