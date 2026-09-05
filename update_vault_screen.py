with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# Add OrganiserStorageManager import and info banner
if "import com.example.util.OrganiserStorageManager" not in content:
    content = content.replace("import com.example.data.model.VaultDocumentEntity", "import com.example.data.model.VaultDocumentEntity\nimport com.example.util.OrganiserStorageManager\nimport android.widget.Toast")

target = """                Text("Your Multiple Libraries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Documents are stored offline for your privacy.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))"""

replacement = """                Text("Your Multiple Libraries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Documents are stored locally in 'My Organiser/Documents_and_Libraries/' for privacy.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("When removed from the UI, a backup is safely kept in 'My Organiser/Deleted_Archive/'", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)

print("VaultScreen updated")
