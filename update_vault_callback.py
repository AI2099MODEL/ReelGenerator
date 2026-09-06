with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''                        onAddDocument = { title, filename, uriStr, type, category, bytes ->
                            viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes, "")
                        },''',
'''                        onAddDocument = { title, filename, uriStr, type, category, bytes, notes ->
                            viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes, notes)
                        },'''
)

with open('app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt', 'w') as f:
    f.write(content)
