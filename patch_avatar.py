import re

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "r") as f:
    content = f.read()

# 1. Add avatarUri to state
state_vars_old = """    var idValidity by remember { mutableStateOf("") }
    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }"""
state_vars_new = """    var idValidity by remember { mutableStateOf("") }
    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var avatarUri by remember { mutableStateOf<android.net.Uri?>(null) }"""
content = content.replace(state_vars_old, state_vars_new)

# 2. Add avatarPickerLauncher
picker_old = """                isScanning = false
            }
        }
    )
    val photoPickerLauncher"""
picker_new = """                isScanning = false
            }
        }
    )
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                avatarUri = uri
            }
        }
    )
    val photoPickerLauncher"""
content = content.replace(picker_old, picker_new)

# 3. Replace Avatar Box in Form
avatar_box_old = """                                        Box(modifier = Modifier.weight(0.35f).aspectRatio(0.75f).background(Color(0xFFEBE3D5), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFC7B79E), modifier = Modifier.size(40.dp))
                                        }"""
avatar_box_new = """                                        Box(
                                            modifier = Modifier
                                                .weight(0.35f)
                                                .aspectRatio(0.75f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFEBE3D5))
                                                .clickable { 
                                                    avatarPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (avatarUri != null) {
                                                coil.compose.AsyncImage(
                                                    model = avatarUri,
                                                    contentDescription = "Avatar",
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFC7B79E), modifier = Modifier.size(40.dp))
                                                    Text("Add Photo", fontSize = 8.sp, color = Color(0xFFC7B79E), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }"""
content = content.replace(avatar_box_old, avatar_box_new)

# 4. Add AvatarUri to JSON
json_old = """                                    json.put("Validity", idValidity)
                                    json.toString()"""
json_new = """                                    json.put("Validity", idValidity)
                                    json.put("AvatarUri", avatarUri?.toString() ?: "")
                                    json.toString()"""
content = content.replace(json_old, json_new)

# 5. Replace Avatar Box in Card
card_box_old = """                        Box(modifier = Modifier.weight(0.35f).fillMaxHeight().background(Color(0xFFEBE3D5), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFC7B79E), modifier = Modifier.size(48.dp))
                        }"""
card_box_new = """                        val savedAvatar = parsedJson?.optString("AvatarUri", "") ?: ""
                        Box(modifier = Modifier.weight(0.35f).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(Color(0xFFEBE3D5)), contentAlignment = Alignment.Center) {
                            if (savedAvatar.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = savedAvatar,
                                    contentDescription = "Avatar",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFC7B79E), modifier = Modifier.size(48.dp))
                            }
                        }"""
content = content.replace(card_box_old, card_box_new)

with open("app/src/main/java/com/example/ui/screens/VaultScreen.kt", "w") as f:
    f.write(content)
