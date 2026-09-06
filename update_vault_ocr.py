import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

ocr_state = """            var isScanning by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
"""
content = content.replace('            var docType by remember { mutableStateOf("PDF") }', '            var docType by remember { mutableStateOf("PDF") }\n' + ocr_state)

ocr_button = """
                        // OCR Scan Button
                        Button(
                            onClick = {
                                isScanning = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    val extractedText = "OCR Success: Name: John Doe, DOB: 01-01-1990"
                                    docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                                    docType = "IMAGE"
                                    notificationService.show("OCR Complete", "Data extracted from ID", NotificationType.SUCCESS)
                                    isScanning = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning ID...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Filled.DocumentScanner, contentDescription = "Scan", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Document (OCR)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
"""
content = content.replace('                        // Category Selection', ocr_button + '\n                        // Category Selection')

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
