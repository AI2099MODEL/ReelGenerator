import re

with open("/tmp/VaultScreen.kt", "r") as f:
    content = f.read()

# 1. State Variables
content = re.sub(
    r'var docNotes by remember \{ mutableStateOf\(""\) \}.*?var attachedFileUri by remember \{ mutableStateOf<android\.net\.Uri\?>\(null\) \}',
    'var docNotes by remember { mutableStateOf("") }\n    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }',
    content, flags=re.DOTALL
)
content = re.sub(
    r'var idValidity by remember.*?(?=\n    val createCameraUri)',
    '',
    content, flags=re.DOTALL
)

# 2. Avatar Picker and Photo Picker
content = re.sub(
    r'val avatarPickerLauncher = rememberLauncherForActivityResult.*?val photoPickerLauncher',
    'val photoPickerLauncher',
    content, flags=re.DOTALL
)

# 3. Add Dialog Body
dialog_ui_pattern = r'if \(docCategory in listOf\("Passport", "Driving Licence", "Aadhaar Card"\)\) \{.*?\} else \{\s*OutlinedTextField'
dialog_replacement = r'OutlinedTextField'
content = re.sub(dialog_ui_pattern, dialog_replacement, content, flags=re.DOTALL)

# 4. Save Logic
save_logic_pattern = r'val finalNotes = if \(docCategory in listOf\("Passport".*?docNotes\.trim\(\)\s*\}'
save_logic_replacement = 'val finalNotes = docNotes.trim()'
content = re.sub(save_logic_pattern, save_logic_replacement, content, flags=re.DOTALL)

# 5. Clear State on Save/Dismiss
clear_state_pattern = r'showAddDialog = false\s+avatarUri = null\s+idSurname = "".*?docNotes = ""'
clear_state_replacement = 'showAddDialog = false\n                                docNotes = ""'
content = re.sub(clear_state_pattern, clear_state_replacement, content, flags=re.DOTALL)

clear_dismiss_pattern = r'showAddDialog = false\s+avatarUri = null\s+idSurname = "".*?docNotes = ""'
clear_dismiss_replacement = 'showAddDialog = false\n                        docNotes = ""'
content = re.sub(clear_dismiss_pattern, clear_dismiss_replacement, content, flags=re.DOTALL)

# 6. IDCardField & IDTextField
id_card_field_pattern = r'@Composable\s*fun IDCardField.*?\}'
content = re.sub(id_card_field_pattern, '', content, flags=re.DOTALL)
id_text_field_pattern = r'@Composable\s*fun IDTextField.*?\}'
content = re.sub(id_text_field_pattern, '', content, flags=re.DOTALL)

# 7. VaultDocumentCard Simplification
card_pattern = r'fun VaultDocumentCard\(doc: VaultDocumentEntity, onDelete: \(\) -> Unit\) \{.*?var isIdCard.*?if \(isIdCard\) \{.*?\} else \{'
card_replacement = r'fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {'
content = re.sub(card_pattern, card_replacement, content, flags=re.DOTALL)

card_end_pattern = r'\}\s*\}\s*\}\s*\}\s*\}\s*\}\s*\n$'
# Remove the extra nesting from `} else {`
content = content.replace("            } else {", "")
# It's tricky to remove the closing bracket. Let's just do it manually by reading/writing correctly.

with open("/tmp/fix_vault.py_out", "w") as f:
    f.write(content)
