package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.util.DateUtils
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetBudget: (Double) -> Unit,
    onSetTheme: (String) -> Unit,
    onSetBiometricLock: (Boolean) -> Unit,
    onSetVoiceAlerts: (Boolean) -> Unit,
    onExportBackup: (Uri, String, String) -> Unit,
    onImportBackup: (Uri, String, String) -> Unit
) {
    val s = LocalAppStrings.current
    var budgetText by remember(settings.monthlyBudget) {
        mutableStateOf(settings.monthlyBudget.toInt().toString())
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackup(uri, s.backupSuccess, s.backupFailed)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackup(uri, s.restoreSuccess, s.backupFailed)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(s.settingsTitle, style = MaterialTheme.typography.headlineSmall)

            // Bilingual Switch
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.languageLabel, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = settings.languageCode == "en",
                        onClick = { onSetLanguage("en") },
                        label = { Text("English") },
                        modifier = Modifier.testTag("lang_en_chip")
                    )
                    FilterChip(
                        selected = settings.languageCode == "si",
                        onClick = { onSetLanguage("si") },
                        label = { Text("සිංහල") },
                        modifier = Modifier.testTag("lang_si_chip")
                    )
                }
            }

            // Monthly Budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text(s.monthlyBudgetLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
                Button(
                    onClick = {
                        budgetText.toDoubleOrNull()?.let { onSetBudget(it) }
                    },
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(s.save) }
            }

            // Theme Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.appearanceLabel, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.themeMode == "system",
                        onClick = { onSetTheme("system") },
                        label = { Text(s.themeSystem) }
                    )
                    FilterChip(
                        selected = settings.themeMode == "light",
                        onClick = { onSetTheme("light") },
                        label = { Text(s.themeLight) }
                    )
                    FilterChip(
                        selected = settings.themeMode == "dark",
                        onClick = { onSetTheme("dark") },
                        label = { Text(s.themeDark) }
                    )
                }
            }

            HorizontalDivider()

            // Biometric Lock & Voice Alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(s.biometricLockLabel, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = s.biometricLockDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = settings.biometricLockEnabled,
                    onCheckedChange = onSetBiometricLock,
                    modifier = Modifier.testTag("biometric_lock_switch")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(s.voiceAlertsGlobalLabel, style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = settings.voiceAlertsEnabled,
                    onCheckedChange = onSetVoiceAlerts
                )
            }

            HorizontalDivider()

            // Offline JSON Backup & Restore
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.offlineBackupTitle, style = MaterialTheme.typography.titleSmall)
                Button(
                    onClick = {
                        exportLauncher.launch("dailyapp_backup_${DateUtils.todayIso()}.json")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_backup_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(s.exportJsonBackup)
                }
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("import_backup_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(s.importJsonBackup)
                }
            }
        }
    }
}
