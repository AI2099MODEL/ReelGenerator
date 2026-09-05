#!/bin/bash
sed -i 's/viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes)/viewModel.addVaultDocument(title, filename, uriStr, type, category, bytes, "")/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
