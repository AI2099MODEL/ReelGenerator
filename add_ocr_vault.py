import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

launcher_code = """
    val coroutineScope = rememberCoroutineScope()
    val notificationService = LocalNotificationService.current
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // We got an image, let's run OCR
                coroutineScope.launch {
                    try {
                        showAddDialog = true
                        isScanning = true
                        
                        val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                            android.graphics.ImageDecoder.decodeBitmap(source)
                        } else {
                            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                        
                        // Use Gemini REST API or Firebase AI for OCR
                        // For prototyping, we can mock it here if Gemini setup is too complex, but wait, the instructions said NO MOCKS.
                        // I will use Retrofit directly to call Gemini API if it's not setup. 
                        // Wait, let's just use Google Generative AI SDK since it's in build.gradle.kts!
                        val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
                            modelName = "gemini-3.1-flash-preview",
                            apiKey = com.example.BuildConfig.GEMINI_API_KEY
                        )
                        val inputContent = com.google.ai.client.generativeai.type.content {
                            image(bitmap)
                            text("Extract the text from this ID document. Please output only the extracted text, formatted cleanly. Specifically look for Name, ID Number, and DOB.")
                        }
                        val response = generativeModel.generateContent(inputContent)
                        
                        docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                        docType = "IMAGE"
                        // we can append OCR to title or save it somewhere. Since there's no "OCR content" field, we can just show a success.
                        notificationService.show("OCR Complete", "Extracted: ${response.text?.take(40)}...", NotificationType.SUCCESS)
                    } catch (e: Exception) {
                        notificationService.show("OCR Failed", "Error: ${e.message}", NotificationType.ERROR)
                    } finally {
                        isScanning = false
                    }
                }
            } else {
                isScanning = false
            }
        }
    )
"""

content = content.replace('    val notificationService = LocalNotificationService.current', launcher_code)

# Replace the button to just launch the picker
scan_button_old = """                            onClick = {
                                isScanning = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    val extractedText = "OCR Success: Name: John Doe, DOB: 01-01-1990"
                                    docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                                    docType = "IMAGE"
                                    notificationService.show("OCR Complete", "Data extracted from ID", NotificationType.SUCCESS)
                                    isScanning = false
                                }
                            },"""
scan_button_new = """                            onClick = {
                                isScanning = true
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },"""
content = content.replace(scan_button_old, scan_button_new)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
