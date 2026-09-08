import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# 1. State Vars
state_vars = """    var docNotes by remember { mutableStateOf("") }
    var idSurname by remember { mutableStateOf("") }
    var idGivenName by remember { mutableStateOf("") }
    var idNationality by remember { mutableStateOf("") }
    var idDOB by remember { mutableStateOf("") }
    var idSex by remember { mutableStateOf("") }
    var idPlaceOfBirth by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var idAddress by remember { mutableStateOf("") }
    var idValidity by remember { mutableStateOf("") }"""
content = content.replace('    var docNotes by remember { mutableStateOf("") }', state_vars)

# 2. OCR Update
old_ocr = """                    val inputContent = com.google.ai.client.generativeai.type.content {
                        image(bitmap)
                        text("Extract the text from this ID document. Please output only the extracted text, formatted cleanly. Specifically look for Name, ID Number, and DOB.")
                    }
                    val response = generativeModel.generateContent(inputContent)
                    response.text
                }
                
                docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                docType = "IMAGE"
                docNotes = responseText ?: ""
                notificationService.show("OCR Complete", "Extracted: ${responseText?.take(40)}...", NotificationType.SUCCESS)"""

new_ocr = """                    val inputContent = com.google.ai.client.generativeai.type.content {
                        image(bitmap)
                        text("Extract details from this ID document. Output ONLY a valid JSON object with these keys (leave blank if not found): Surname, Given Name, Nationality, DOB, Sex, Place of Birth, ID Number, Address, Validity. Do not output anything else.")
                    }
                    val response = generativeModel.generateContent(inputContent)
                    response.text
                }
                
                docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                docType = "IMAGE"
                
                var parsedOk = false
                try {
                    val cleanText = responseText?.replace("```json", "")?.replace("```", "")?.trim()
                    if (!cleanText.isNullOrEmpty()) {
                        val json = org.json.JSONObject(cleanText)
                        idSurname = json.optString("Surname", "")
                        idGivenName = json.optString("Given Name", "")
                        idNationality = json.optString("Nationality", "")
                        idDOB = json.optString("DOB", "")
                        idSex = json.optString("Sex", "")
                        idPlaceOfBirth = json.optString("Place of Birth", "")
                        idNumber = json.optString("ID Number", "")
                        idAddress = json.optString("Address", "")
                        idValidity = json.optString("Validity", "")
                        parsedOk = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                if (!parsedOk) {
                    docNotes = responseText ?: ""
                }
                
                notificationService.show("OCR Complete", "Extracted details successfully.", NotificationType.SUCCESS)"""

content = content.replace(old_ocr, new_ocr)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
