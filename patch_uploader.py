import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# 1. Clean up state variables
states_old = """    var docNotes by remember { mutableStateOf("") }
    var idSurname by remember { mutableStateOf("") }
    var idGivenName by remember { mutableStateOf("") }
    var idNationality by remember { mutableStateOf("") }
    var idDOB by remember { mutableStateOf("") }
    var idSex by remember { mutableStateOf("") }
    var idPlaceOfBirth by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var idAddress by remember { mutableStateOf("") }
    var idValidity by remember { mutableStateOf("") }
    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var avatarUri by remember { mutableStateOf<android.net.Uri?>(null) }"""

states_new = """    var docNotes by remember { mutableStateOf("") }
    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var attachedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }"""
content = content.replace(states_old, states_new)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
