import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# Replace the OutlinedTextField for docNotes in Add Dialog with the new Form logic
old_notes_field = """                        OutlinedTextField(
                            value = docNotes,
                            onValueChange = { docNotes = it },
                            label = { Text("Extracted Text / Notes", color = GoldLight.copy(alpha = 0.8f)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoldHighlight,
                                unfocusedTextColor = GoldLight,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFF100D18),
                                unfocusedContainerColor = Color(0xFF161224)
                            )
                        )"""

new_notes_field = """                        if (docCategory in listOf("Passport", "Driving Licence", "Aadhaar Card")) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF9F5EC),
                                border = BorderStroke(1.dp, Color(0xFFDCD2C6))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if (docCategory == "Passport") Icons.Filled.Public else Icons.Filled.CreditCard, contentDescription = null, tint = Color(0xFF6C5B7B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(docCategory.uppercase(), color = Color(0xFF6C5B7B), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFDCD2C6))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(modifier = Modifier.weight(0.35f).aspectRatio(0.75f).background(Color(0xFFEBE3D5), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFC7B79E), modifier = Modifier.size(40.dp))
                                        }
                                        Column(modifier = Modifier.weight(0.65f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IDTextField("ID NUMBER", idNumber) { idNumber = it }
                                            IDTextField(if (docCategory == "Passport") "SURNAME" else "NAME", idSurname) { idSurname = it }
                                            if (docCategory == "Passport") {
                                                IDTextField("GIVEN NAME", idGivenName) { idGivenName = it }
                                                IDTextField("NATIONALITY", idNationality) { idNationality = it }
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Box(modifier = Modifier.weight(1f)) { IDTextField("DOB", idDOB) { idDOB = it } }
                                                Box(modifier = Modifier.weight(1f)) { IDTextField("SEX", idSex) { idSex = it } }
                                            }
                                            if (docCategory == "Passport") {
                                                IDTextField("PLACE OF BIRTH", idPlaceOfBirth) { idPlaceOfBirth = it }
                                            }
                                            if (docCategory == "Driving Licence") {
                                                IDTextField("VALIDITY", idValidity) { idValidity = it }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = docNotes,
                                onValueChange = { docNotes = it },
                                label = { Text("Extracted Text / Notes", color = GoldLight.copy(alpha = 0.8f)) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = GoldHighlight,
                                    unfocusedTextColor = GoldLight,
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = GoldAccent.copy(alpha = 0.5f),
                                    focusedContainerColor = Color(0xFF100D18),
                                    unfocusedContainerColor = Color(0xFF161224)
                                )
                            )
                        }"""

content = content.replace(old_notes_field, new_notes_field)

# Replace the save logic to encode JSON
old_save = """                                val filename = "${docTitle.trim().replace(" ", "_")}.${docType.lowercase()}"
                                onAddDocument(
                                    docTitle.trim(),
                                    filename,
                                    "file://vault/$filename",
                                    docType,
                                    docCategory,
                                    1024L,
                                    docNotes.trim()
                                )"""

new_save = """                                val filename = "${docTitle.trim().replace(" ", "_")}.${docType.lowercase()}"
                                val finalNotes = if (docCategory in listOf("Passport", "Driving Licence", "Aadhaar Card")) {
                                    val json = org.json.JSONObject()
                                    json.put("Surname", idSurname)
                                    json.put("Given Name", idGivenName)
                                    json.put("Nationality", idNationality)
                                    json.put("DOB", idDOB)
                                    json.put("Sex", idSex)
                                    json.put("Place of Birth", idPlaceOfBirth)
                                    json.put("ID Number", idNumber)
                                    json.put("Address", idAddress)
                                    json.put("Validity", idValidity)
                                    json.toString()
                                } else {
                                    docNotes.trim()
                                }
                                onAddDocument(
                                    docTitle.trim(),
                                    filename,
                                    "file://vault/$filename",
                                    docType,
                                    docCategory,
                                    1024L,
                                    finalNotes
                                )"""

content = content.replace(old_save, new_save)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
