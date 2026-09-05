#!/bin/bash
sed -i 's/val musicTracks by viewModel.musicTracks.collectAsStateWithLifecycle()/val tasks by viewModel.tasks.collectAsStateWithLifecycle()\n    val events by viewModel.events.collectAsStateWithLifecycle()\n    val vaultDocs by viewModel.vaultDocuments.collectAsStateWithLifecycle()/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i 's/val downloadedVideos by viewModel.downloadedVideos.collectAsStateWithLifecycle()//g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt

sed -i 's/musicTracks = musicTracks,/tasks = tasks,/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i 's/downloadedVideos = downloadedVideos,/events = events,\n                                    vaultDocs = vaultDocs,/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt

sed -i 's/musicTracks: List<com.example.data.model.MusicTrackEntity>,/tasks: List<com.example.data.model.TaskEntity>,/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt
sed -i 's/downloadedVideos: List<DownloadedVideoEntity>,/events: List<com.example.data.model.EventEntity>,\n    vaultDocs: List<com.example.data.model.VaultDocumentEntity>,/g' app/src/main/java/com/example/ui/screens/MainLedgerScreen.kt

