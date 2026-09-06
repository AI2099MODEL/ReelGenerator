import re

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'r') as f:
    content = f.read()

old_button = """                        Button(
                            onClick = {
                                isScanning = true
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
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
                        }"""

new_buttons = """                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isScanning = true
                                    currentCameraUri = createCameraUri()
                                    cameraLauncher.launch(currentCameraUri!!)
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera OCR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = {
                                    isScanning = true
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2438), contentColor = GoldPrimary)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Image, contentDescription = "Gallery", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery OCR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }"""

content = content.replace(old_button, new_buttons)

with open('app/src/main/java/com/example/ui/screens/VaultScreen.kt', 'w') as f:
    f.write(content)
