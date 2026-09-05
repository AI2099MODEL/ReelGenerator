#!/bin/bash
sed -i 's/onDismiss = { showGlobalSettingsDialog = false },/onDismissRequest = { showGlobalSettingsDialog = false },\n            onToggleLocation = {},\n            onSetLocation = { _, _, _, _ -> },\n            onToggleTranslation = {},\n            onSetLanguage = { _, _ -> },\n            onToggleAutoTranslate = {},\n            onSetTranslationEngine = {}/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i 's/onSave = { updated ->/ /g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i '/viewModel.globalSettings.value = updated/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i 's/showGlobalSettingsDialog = false//g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt

