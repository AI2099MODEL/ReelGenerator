import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

target_save = """                                notificationService.show("Success", "Saved to Documents/My Organiser/Vault", NotificationType.SUCCESS)
                                showAddDialog = false
                            }
                        },"""
replace_save = """                                notificationService.show("Success", "Saved to Documents/My Organiser/Vault", NotificationType.SUCCESS)
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
                            }
                        },"""
content = content.replace(target_save, replace_save)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
