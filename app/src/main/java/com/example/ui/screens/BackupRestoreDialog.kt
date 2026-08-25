package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.ui.PlantViewModel
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreDialog(
    viewModel: PlantViewModel,
    onDismiss: () -> Unit
) {
    var jsonExportText by remember { mutableStateOf("") }
    var jsonImportInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⚙️ SETTINGS & PREFERENCES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = "Preferences & Backup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. App Visual Theme Selector Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (uiState.themeMode) {
                                com.example.ui.theme.ThemeMode.SYSTEM -> Icons.Outlined.AutoMode
                                com.example.ui.theme.ThemeMode.LIGHT -> Icons.Outlined.LightMode
                                com.example.ui.theme.ThemeMode.DARK -> Icons.Outlined.DarkMode
                            },
                            contentDescription = null,
                            tint = ForestGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Visual Theme", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        com.example.ui.theme.ThemeMode.entries.forEach { mode ->
                            val selected = uiState.themeMode == mode
                            Button(
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) ForestGreen else MaterialTheme.colorScheme.surface,
                                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = mode.label.split(" ").first(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            var backupPassphrase by remember { mutableStateOf("") }
            var isEncryptedMode by remember { mutableStateOf(true) }

            // Export Section (Encrypted / JSON)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEncryptedMode) "🔐 Encrypted Database Backup" else "Export Plant Jungle JSON",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AES-256 Encryption Mode", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = isEncryptedMode,
                            onCheckedChange = { isEncryptedMode = it }
                        )
                    }

                    if (isEncryptedMode) {
                        OutlinedTextField(
                            value = backupPassphrase,
                            onValueChange = { backupPassphrase = it },
                            label = { Text("Encryption Password / Key") },
                            placeholder = { Text("Enter secret passphrase...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (isEncryptedMode) {
                                val pass = backupPassphrase.ifBlank { "PlantPulseKey" }
                                viewModel.exportEncryptedBackup(pass) { encrypted ->
                                    jsonExportText = encrypted
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("PlantPulse Encrypted Backup", encrypted)
                                    clipboard.setPrimaryClip(clip)
                                }
                            } else {
                                viewModel.exportBackupJson { json ->
                                    jsonExportText = json
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("PlantPulse Backup JSON", json)
                                    clipboard.setPrimaryClip(clip)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEncryptedMode) "Generate & Copy Encrypted Backup" else "Generate & Copy JSON Backup")
                    }

                    if (jsonExportText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isEncryptedMode) "✅ Encrypted backup package copied to clipboard!" else "✅ Backup JSON copied to clipboard!",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Import Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Database from Backup", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = jsonImportInput,
                        onValueChange = { jsonImportInput = it },
                        placeholder = { Text("Paste exported JSON or encrypted backup code here...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (jsonImportInput.contains("PLANT_PULSE_ENCRYPTED_BACKUP")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = backupPassphrase,
                            onValueChange = { backupPassphrase = it },
                            label = { Text("Decryption Password") },
                            placeholder = { Text("Enter password used to encrypt...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (jsonImportInput.isNotBlank()) {
                                if (jsonImportInput.contains("PLANT_PULSE_ENCRYPTED_BACKUP")) {
                                    val pass = backupPassphrase.ifBlank { "PlantPulseKey" }
                                    viewModel.importEncryptedBackup(pass, jsonImportInput) { success ->
                                        if (success) onDismiss()
                                    }
                                } else {
                                    viewModel.importBackupJson(jsonImportInput)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = jsonImportInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore Plant Data")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Onboarding Replay
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ForestGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Interactive App Guide", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.resetOnboarding()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Replay Onboarding Guide 🔄")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
