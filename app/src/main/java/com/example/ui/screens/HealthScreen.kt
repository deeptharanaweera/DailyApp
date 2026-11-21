package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.AppSettings
import com.example.data.local.HealthVaultEntity
import com.example.data.local.MedicationEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LocalImagePreviewDialog
import com.example.ui.components.SegmentedPillBar
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.RoseAlert
import com.example.util.DateUtils
import com.example.util.DomainLabels
import com.example.util.LocalAppStrings
import com.example.util.LocalFileStorage
import kotlinx.coroutines.launch

private val presetMedTimes = listOf("07:00", "08:00", "12:00", "13:00", "18:00", "20:00", "22:00")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HealthScreen(
    medications: List<MedicationEntity>,
    healthRecords: List<HealthVaultEntity>,
    settings: AppSettings,
    openAddMedOnLaunch: Boolean = false,
    onConsumeLaunchFlags: () -> Unit,
    parseStringList: (String) -> List<String>,
    onAddMedication: (medName: String, dosage: String, times: List<String>, member: String, voiceAlert: Boolean) -> Unit,
    onToggleDoseTaken: (MedicationEntity, String, String) -> Unit,
    onSpeakMedication: (MedicationEntity) -> Unit,
    onDeleteMedication: (MedicationEntity) -> Unit,
    onSaveAttachmentLocally: suspend (Uri) -> String?,
    onAddHealthRecord: (
        title: String,
        doctor: String,
        member: String,
        tags: String,
        billAmount: Double,
        autoLogExpense: Boolean,
        filePath: String?
    ) -> Unit,
    onDeleteHealthRecord: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val todayIso = remember { DateUtils.todayIso() }

    var selectedSubTab by remember { mutableIntStateOf(0) }
    var selectedMember by remember { mutableStateOf("All") }
    var showAddMedSheet by remember { mutableStateOf(false) }
    var showAddRecordSheet by remember { mutableStateOf(false) }
    var previewImagePair by remember { mutableStateOf<Pair<String, String>?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(openAddMedOnLaunch) {
        if (openAddMedOnLaunch) {
            selectedSubTab = 0
            showAddMedSheet = true
            onConsumeLaunchFlags()
        }
    }

    val filteredMeds = remember(medications, selectedMember) {
        if (selectedMember == "All") medications else medications.filter { it.memberName == selectedMember }
    }
    val filteredRecords = remember(healthRecords, selectedMember) {
        if (selectedMember == "All") healthRecords else healthRecords.filter { it.memberName == selectedMember }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        SegmentedPillBar(
            options = listOf(s.medicationsTab, "${s.healthVaultTab} (${healthRecords.size})"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            testTagPrefix = "health_subtab"
        )
        Spacer(Modifier.height(10.dp))

        // Family Member Folder Chips (Self, Mom, Dad, Family)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedMember == "All",
                    onClick = { selectedMember = "All" },
                    label = { Text(s.allMembers) }
                )
            }
            items(DomainLabels.familyMembers) { member ->
                FilterChip(
                    selected = selectedMember == member,
                    onClick = { selectedMember = member },
                    label = { Text(DomainLabels.memberLabel(member, s.isSinhala)) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        if (selectedSubTab == 0) {
            // MEDICATIONS & VOICE REMINDERS TAB
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!granted) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            showAddMedSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_medication_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s.addMedication)
                    }
                }

                if (filteredMeds.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Medication,
                            title = s.noMedsYet,
                            subtitle = s.noMedsYetHint,
                            accentColor = ClinicalTeal
                        )
                    }
                } else {
                    items(filteredMeds, key = { it.id }) { med ->
                        val times = parseStringList(med.scheduleTimesJson)
                        val takenSet = parseStringList(med.takenDatesJson).toSet()
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(ClinicalTeal.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Medication, contentDescription = null, tint = ClinicalTeal)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(med.medName, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${med.dosage} • ${DomainLabels.memberLabel(med.memberName, s.isSinhala)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onSpeakMedication(med) },
                                        modifier = Modifier.testTag("speak_med_${med.id}")
                                    ) {
                                        Icon(
                                            Icons.Rounded.VolumeUp,
                                            contentDescription = s.speakNow,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteMedication(med) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = s.delete,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    times.forEach { slot ->
                                        val isTaken = "$todayIso|$slot" in takenSet
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { onToggleDoseTaken(med, todayIso, slot) },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isTaken) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isTaken) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                    contentDescription = if (isTaken) s.taken else s.markTaken,
                                                    tint = if (isTaken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "$slot • ${if (isTaken) s.taken else s.markTaken}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (isTaken) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // HEALTH VAULT (MEDICAL REPORTS, PRESCRIPTIONS & AUTO-EXPENSE LOGGING)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Button(
                        onClick = { showAddRecordSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_health_record_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s.addHealthRecord)
                    }
                }

                if (filteredRecords.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.FolderShared,
                            title = s.noHealthRecords,
                            subtitle = s.noHealthRecordsHint,
                            accentColor = RoseAlert
                        )
                    }
                } else {
                    items(filteredRecords, key = { it.id }) { record ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(RoseAlert.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Description, contentDescription = null, tint = RoseAlert)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = buildString {
                                                append(DomainLabels.memberLabel(record.memberName, s.isSinhala))
                                                if (record.doctorName.isNotBlank()) append(" • Dr. ${record.doctorName}")
                                                append(" • ${DateUtils.formatPrettyDate(record.date)}")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteHealthRecord(record.id) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = s.delete,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (record.tags.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = record.tags,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        if (record.billAmount > 0.0) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = DateUtils.formatCurrency(record.billAmount, settings.currencySymbol),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (!record.filePath.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val path = record.filePath
                                                if (path.endsWith(".pdf", ignoreCase = true)) {
                                                    val uri = LocalFileStorage.getFileProviderUri(context, path)
                                                    if (uri != null) {
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            setDataAndType(uri, "application/pdf")
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }
                                                        runCatching { context.startActivity(intent) }
                                                    }
                                                } else {
                                                    previewImagePair = record.title to path
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(s.openAttachment)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Medication Modal Sheet
    if (showAddMedSheet) {
        var medName by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var member by remember { mutableStateOf(if (selectedMember == "All") "Self" else selectedMember) }
        var selectedTimes by remember { mutableStateOf(setOf("08:00", "20:00")) }
        var voiceAlert by remember { mutableStateOf(true) }

        ModalBottomSheet(
            onDismissRequest = { showAddMedSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(s.addMedication, style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text(s.medNameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(s.dosageLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_dosage_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Text(s.familyMemberLabel, style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DomainLabels.familyMembers) { m ->
                        FilterChip(
                            selected = member == m,
                            onClick = { member = m },
                            label = { Text(DomainLabels.memberLabel(m, s.isSinhala)) }
                        )
                    }
                }

                Text(s.scheduleTimesLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetMedTimes.forEach { t ->
                        val chosen = t in selectedTimes
                        FilterChip(
                            selected = chosen,
                            onClick = {
                                selectedTimes = if (chosen && selectedTimes.size > 1) selectedTimes - t
                                else selectedTimes + t
                            },
                            label = { Text(t) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s.enableVoiceAlert, style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = voiceAlert, onCheckedChange = { voiceAlert = it })
                }

                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            onAddMedication(medName, dosage, selectedTimes.sorted(), member, voiceAlert)
                            showAddMedSheet = false
                        }
                    },
                    enabled = medName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_medication_button"),
                    shape = RoundedCornerShape(16.dp)
                ) { Text(s.save) }
            }
        }
    }

    // Add Health Vault Record Modal Sheet (with Photo/PDF attachment + Auto-Expense Logging)
    if (showAddRecordSheet) {
        AddHealthRecordSheet(
            defaultMember = if (selectedMember == "All") "Self" else selectedMember,
            onDismiss = { showAddRecordSheet = false },
            onSaveAttachmentLocally = onSaveAttachmentLocally,
            onSave = { title, doctor, member, tags, bill, autoLog, path ->
                onAddHealthRecord(title, doctor, member, tags, bill, autoLog, path)
                showAddRecordSheet = false
            }
        )
    }

    previewImagePair?.let { (title, path) ->
        LocalImagePreviewDialog(
            filePath = path,
            title = title,
            onDismiss = { previewImagePair = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHealthRecordSheet(
    defaultMember: String,
    onDismiss: () -> Unit,
    onSaveAttachmentLocally: suspend (Uri) -> String?,
    onSave: (String, String, String, String, Double, Boolean, String?) -> Unit
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var member by remember { mutableStateOf(defaultMember) }
    var tags by remember { mutableStateOf("") }
    var billText by remember { mutableStateOf("") }
    var autoLogExpense by remember { mutableStateOf(true) }
    var savedFilePath by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch { savedFilePath = onSaveAttachmentLocally(uri) }
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch { savedFilePath = onSaveAttachmentLocally(uri) }
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = pendingCameraUri
        if (ok && uri != null) {
            scope.launch { savedFilePath = onSaveAttachmentLocally(uri) }
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, _) = LocalFileStorage.createCameraCaptureUri(context, "health")
            pendingCameraUri = uri
            takePicture.launch(uri)
        }
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
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(s.addHealthRecord, style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(s.recordTitleLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("health_record_title_input"),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text(s.doctorNameLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Text(s.familyMemberLabel, style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DomainLabels.familyMembers) { m ->
                    FilterChip(
                        selected = member == m,
                        onClick = { member = m },
                        label = { Text(DomainLabels.memberLabel(m, s.isSinhala)) }
                    )
                }
            }

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text(s.tagsLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // SMART INTERCONNECTION #3: Medical Bill Cost -> Automatic Health Expense
            OutlinedTextField(
                value = billText,
                onValueChange = { billText = it },
                label = { Text(s.billAmountOptional) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            if ((billText.toDoubleOrNull() ?: 0.0) > 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = s.autoLogHealthExpense,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = autoLogExpense, onCheckedChange = { autoLogExpense = it })
                }
            }

            Text(s.attachReportFile, style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            val (uri, _) = LocalFileStorage.createCameraCaptureUri(context, "health")
                            pendingCameraUri = uri
                            takePicture.launch(uri)
                        } else {
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.takePhoto, maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.pickPhoto, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PDF", maxLines = 1)
                }
            }

            if (savedFilePath != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(s.fileAttached, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title,
                            doctor,
                            member,
                            tags,
                            billText.toDoubleOrNull() ?: 0.0,
                            autoLogExpense,
                            savedFilePath
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_health_record_button"),
                shape = RoundedCornerShape(16.dp)
            ) { Text(s.save) }
        }
    }
}
