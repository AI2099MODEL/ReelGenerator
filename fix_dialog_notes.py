import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

old_category_end = """                                            color = if (isSel) Color(0xFF241400) else GoldLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }"""

new_category_end = """                                            color = if (isSel) Color(0xFF241400) else GoldLight
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Extracted Text / Notes
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
                    }
                }"""

content = content.replace(old_category_end, new_category_end)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
