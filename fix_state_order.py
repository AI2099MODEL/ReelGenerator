import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# I will move the declaration of `context`, `notificationService`, `coroutineScope` ABOVE `processOcr`.

# Find the block:
context_block = """    val context = LocalContext.current
    val notificationService = LocalNotificationService.current
    val coroutineScope = rememberCoroutineScope()"""

# Remove it from its current position
content = content.replace(context_block, '')

# Add it just before createCameraUri
content = content.replace('    val createCameraUri = {', context_block + '\n    val createCameraUri = {')

# Replace the photoPickerLauncher body to just use processOcr
old_launcher = """    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // We got an image, let's run OCR
                coroutineScope.launch {
                    try {
                        showAddDialog = true
                        isScanning = true
                        
                        val responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                                android.graphics.ImageDecoder.decodeBitmap(source)
                            } else {
                                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                            }
                            
                            val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
                                modelName = "gemini-1.5-flash",
                                apiKey = com.example.BuildConfig.GEMINI_API_KEY
                            )
                            val inputContent = com.google.ai.client.generativeai.type.content {
                                image(bitmap)
                                text("Extract the text from this ID document. Please output only the extracted text, formatted cleanly. Specifically look for Name, ID Number, and DOB.")
                            }
                            val response = generativeModel.generateContent(inputContent)
                            response.text
                        }
                        
                        docTitle = if (docCategory == "Other") "Scanned Document" else "$docCategory Scan"
                        docType = "IMAGE"
                        notificationService.show("OCR Complete", "Extracted: ${responseText?.take(40)}...", NotificationType.SUCCESS)
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
    )"""

new_launcher = """    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                processOcr(uri)
            } else {
                isScanning = false
            }
        }
    )"""

content = content.replace(old_launcher, new_launcher)

# Update onAddDocument call to pass docNotes
old_on_add_call = """                                onAddDocument(
                                    docTitle,
                                    if (docType == "IMAGE") "scanned_${System.currentTimeMillis()}.jpg" else "document_${System.currentTimeMillis()}.pdf",
                                    "", // no local uri for now
                                    docType,
                                    docCategory,
                                    250000L
                                )"""
new_on_add_call = """                                onAddDocument(
                                    docTitle,
                                    if (docType == "IMAGE") "scanned_${System.currentTimeMillis()}.jpg" else "document_${System.currentTimeMillis()}.pdf",
                                    "", // no local uri for now
                                    docType,
                                    docCategory,
                                    250000L,
                                    docNotes
                                )"""

content = content.replace(old_on_add_call, new_on_add_call)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
