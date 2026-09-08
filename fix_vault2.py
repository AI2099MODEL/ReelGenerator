import re

with open("/tmp/VaultScreen.kt", "r") as f:
    content = f.read()

# 1. State Variables
content = re.sub(
    r'var docNotes by remember \{ mutableStateOf\(""\) \}.*?var avatarUri by remember \{ mutableStateOf<android\.net\.Uri\?>\(null\) \}',
    'var docNotes by remember { mutableStateOf("") }\n    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }',
    content, flags=re.DOTALL
)

# Also remove unused idVars if they still exist.
content = re.sub(
    r'\n\s*var idSurname.*?\n\s*var idValidity by remember \{ mutableStateOf\(""\) \}',
    '',
    content, flags=re.DOTALL
)


# 2. Avatar Picker and Photo Picker
content = re.sub(
    r'val avatarPickerLauncher = rememberLauncherForActivityResult.*?\n\s*val photoPickerLauncher',
    'val photoPickerLauncher',
    content, flags=re.DOTALL
)

# 3. Add Dialog Body
dialog_ui_pattern = r'if \(docCategory in listOf\("Passport", "Driving Licence", "Aadhaar Card"\)\) \{.*?\} else \{\s*OutlinedTextField'
dialog_replacement = r'OutlinedTextField'
content = re.sub(dialog_ui_pattern, dialog_replacement, content, flags=re.DOTALL)

# 4. Save Logic
save_logic_pattern = r'val finalNotes = if \(docCategory in listOf\("Passport".*?docNotes\.trim\(\)\n\s*\}'
save_logic_replacement = 'val finalNotes = docNotes.trim()'
content = re.sub(save_logic_pattern, save_logic_replacement, content, flags=re.DOTALL)

# 5. Clear State on Save/Dismiss
clear_state_pattern = r'showAddDialog = false\n\s*avatarUri = null\n.*?docNotes = ""'
clear_state_replacement = 'showAddDialog = false\n                                docNotes = ""'
content = re.sub(clear_state_pattern, clear_state_replacement, content, flags=re.DOTALL)

# 6. IDCardField & IDTextField
id_card_field_pattern = r'@Composable\nfun IDCardField.*?\}\n\}'
content = re.sub(id_card_field_pattern, '', content, flags=re.DOTALL)
id_text_field_pattern = r'@Composable\nfun IDTextField.*?\}\n\}'
content = re.sub(id_text_field_pattern, '', content, flags=re.DOTALL)

# 7. VaultDocumentCard Simplification
card_pattern = r'fun VaultDocumentCard\(doc: VaultDocumentEntity, onDelete: \(\) -> Unit\) \{\n\s*var isIdCard.*?if \(isIdCard\) \{.*?\} else \{'
card_replacement = r'fun VaultDocumentCard(doc: VaultDocumentEntity, onDelete: () -> Unit) {'
content = re.sub(card_pattern, card_replacement, content, flags=re.DOTALL)

# 8. Extra bracket matching
def remove_closing_bracket(text):
    # VaultDocumentCard ends with } from the else block. We need to remove the matching } of the else block.
    # A simple hack: just remove the extra indent block.
    # The `} else {` we removed means there is an extra `}` at the end of the VaultDocumentCard
    return text.replace("    }\n}\n\n", "}\n\n")

content = remove_closing_bracket(content)

with open("/tmp/fix_vault.py_out", "w") as f:
    f.write(content)
