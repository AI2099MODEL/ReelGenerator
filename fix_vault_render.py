with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''            when (section) {
                LedgerSection.IMAGES,
            LedgerSection.VAULT -> {
                    ImageStudioScreen(''',
'''            when (section) {
                LedgerSection.IMAGES -> {
                    ImageStudioScreen('''
)

with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'w') as f:
    f.write(content)
