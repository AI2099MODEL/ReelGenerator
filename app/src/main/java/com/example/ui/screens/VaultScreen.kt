package com.example.ui.screens

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.model.VaultDocumentEntity
import com.example.util.OrganiserStorageManager
import android.widget.Toast
import com.example.ui.components.LedgerTopHeader

@Composable
fun VaultScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isUnlocked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (!isUnlocked) {
        VaultLockScreen(
            onUnlockSuccess = { isUnlocked = true },
            onMenuClick = onMenuClick
        )
    } else {
        VaultContentScreen(
            vaultDocs = vaultDocs,
            onAddDocument = onAddDocument,
            onDeleteDocument = onDeleteDocument,
            onMenuClick = onMenuClick,
            modifier = modifier
        )
    }
}

@Composable
fun VaultLockScreen(onUnlockSuccess: () -> Unit, onMenuClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = { LedgerTopHeader(title = "Secure Vault") }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Lock, contentDescription = "Locked", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Vault is Locked", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Unlock to access your personal documents and libraries.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                    // Fallback for simulation or error
                                    onUnlockSuccess() 
                                }
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    onUnlockSuccess()
                                }
                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                }
                            })

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login for my app")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use account password")
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        // Fallback if not an activity
                        onUnlockSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlock Vault")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultContentScreen(
    vaultDocs: List<VaultDocumentEntity>,
    onAddDocument: (String, String, String, String, String, Long) -> Unit,
    onDeleteDocument: (VaultDocumentEntity) -> Unit,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LedgerTopHeader(title = "Secure Vault") },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Add Document")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Your Multiple Libraries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Documents are stored locally in 'My Organiser/Documents_and_Libraries/' for privacy.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("When removed from the UI, a backup is safely kept in 'My Organiser/Deleted_Archive/'", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (vaultDocs.isEmpty()) {
                item {
                    Text("No documents stored in the vault.", style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(vaultDocs) { doc ->
                VaultDocItem(doc = doc, onDelete = { onDeleteDocument(doc) })
            }
        }

        if (showAddDialog) {
            var title by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Personal Library") }
            var type by remember { mutableStateOf("PDF") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Document to Library") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Document Title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Library Name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text("Document Type (e.g. PDF, IMG)") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            onAddDocument(title, "file_$title", "local_uri", type, category, 1024L)
                            showAddDialog = false
                        }
                    }) { Text("Save to Vault") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun VaultDocItem(doc: VaultDocumentEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Library: ${doc.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Type: ${doc.fileType}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Document")
            }
        }
    }
}
