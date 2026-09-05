#!/bin/bash
sed -i '/onMenuClick = { coroutineScope.launch { drawerState.open() } }/d' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
