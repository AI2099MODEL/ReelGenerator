with open('app/src/main/java/com/example/ui/LedgerViewModel.kt', 'r') as f:
    text = f.read()
text = text.replace('MUSIC("My Music", "Music", "🎵"),\n    VAULT("Document Vault", "Vault", "🔒"),', 'MUSIC("My Music", "Music", "🎵"),\n    RINGTONES("My Ringtones", "Ringtones", "🔔"),\n    VAULT("Document Vault", "Vault", "🔒"),')
with open('app/src/main/java/com/example/ui/LedgerViewModel.kt', 'w') as f:
    f.write(text)
