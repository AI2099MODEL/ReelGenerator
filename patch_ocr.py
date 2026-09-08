import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

ocr_old = """                        text("Extract details from this ID document. Output ONLY a valid JSON object with these keys (leave blank if not found): Surname, Given Name, Nationality, DOB, Sex, Place of Birth, ID Number, Address, Validity. Do not output anything else.")
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

ocr_new = """                        text("Extract the text from this document clearly and concisely.")
                    }
                    val response = generativeModel.generateContent(inputContent)
                    response.text
                }
                
                if (docTitle.isBlank()) {
                    docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                }
                docType = "IMAGE"
                docNotes = responseText ?: ""
                
                notificationService.show("OCR Complete", "Extracted text successfully.", NotificationType.SUCCESS)"""

content = content.replace(ocr_old, ocr_new)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
