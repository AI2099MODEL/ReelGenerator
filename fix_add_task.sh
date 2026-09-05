#!/bin/bash
sed -i '/title = title,/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i '/description = description,/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i '/notifyMe = notify,/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i '/scheduledTimestamp = scheduledTime,/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i '/category = "General"/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i '/viewModel.addTask(task)/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
