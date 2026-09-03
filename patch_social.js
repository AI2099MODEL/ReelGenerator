const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SocialScreen.kt', 'utf8');
content = content.replace(/\.clickable \{ videoPickerLauncher\.launch\("video\/\*"\) \}/, `.clickable {
                        try {
                            videoPickerLauncher.launch("video/*")
                        } catch (e: Exception) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("File picker not found on this device.") }
                        }
                    }`);
fs.writeFileSync('app/src/main/java/com/example/ui/screens/SocialScreen.kt', content);
