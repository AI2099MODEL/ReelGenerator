with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

# Update onAddDocument signatures
content = content.replace(
    'onAddDocument: (String, String, String, String, String, Long) -> Unit',
    'onAddDocument: (String, String, String, String, String, Long, String) -> Unit'
)

# Insert new state and helper functions after isScanning
new_state = """    var isScanning by remember { mutableStateOf(false) }
    var docNotes by remember { mutableStateOf("") }
    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val createCameraUri = {
        val file = java.io.File(context.cacheDir, "vault_cam_${System.currentTimeMillis()}.jpg")
        androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val processOcr = { uri: android.net.Uri ->
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
                docNotes = responseText ?: ""
                notificationService.show("OCR Complete", "Extracted: ${responseText?.take(40)}...", NotificationType.SUCCESS)
            } catch (e: Exception) {
                notificationService.show("OCR Failed", "Error: ${e.message}", NotificationType.ERROR)
            } finally {
                isScanning = false
            }
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && currentCameraUri != null) {
                processOcr(currentCameraUri!!)
            } else {
                isScanning = false
            }
        }
    )
"""
content = content.replace('    var isScanning by remember { mutableStateOf(false) }', new_state)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
