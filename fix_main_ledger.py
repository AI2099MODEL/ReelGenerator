with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''        listOf(
            LedgerSection.DAILY_SCHEDULE,
            LedgerSection.IMPORTANT_DATES,
            LedgerSection.REMIND_ME,
            LedgerSection.IMAGES
        )''',
'''        listOf(
            LedgerSection.DAILY_SCHEDULE,
            LedgerSection.IMPORTANT_DATES,
            LedgerSection.REMIND_ME,
            LedgerSection.VAULT,
            LedgerSection.IMAGES
        )'''
)

with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'w') as f:
    f.write(content)
