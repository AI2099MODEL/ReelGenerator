import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# Make sure avatarUri clears when a new scan starts or when adding is cancelled
target_dismiss = """                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {"""
replace_dismiss = """                dismissButton = {
                    TextButton(onClick = { 
                        showAddDialog = false
                        avatarUri = null
                        idSurname = ""
                        idGivenName = ""
                        idNationality = ""
                        idDOB = ""
                        idSex = ""
                        idPlaceOfBirth = ""
                        idNumber = ""
                        idAddress = ""
                        idValidity = ""
                        docNotes = ""
                    }) {"""
content = content.replace(target_dismiss, replace_dismiss)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
