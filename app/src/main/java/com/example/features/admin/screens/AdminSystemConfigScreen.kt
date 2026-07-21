package com.example.features.admin.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.admin.providers.AdminSystemConfigViewModel
import com.example.features.admin.providers.AdminSystemConfigViewModelFactory
import com.example.core.repository.AdminRepositoryImpl
import com.example.CamsApplication

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminSystemConfigScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminSystemConfigViewModel = viewModel(factory = AdminSystemConfigViewModelFactory(AdminRepositoryImpl(CamsApplication.instance.container.apiService)))
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Settings saved", android.widget.Toast.LENGTH_SHORT).show()
            showEditDialog = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "System Configuration",
        currentRoute = "/admin/system-config",
        onNavigate = onNavigate
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth().weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Institution Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = { showEditDialog = true }, enabled = uiState.settings != null) { Text("Edit") }
                }
            }
            item {
                CamsCard {
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else if (uiState.error != null) {
                        // Without this the screen rendered empty on failure — no message,
                        // no retry — indistinguishable from genuinely having no data.
                        com.example.core.ui.NetworkErrorView(
                            message = uiState.error ?: "Failed to load system config",
                            onRetry = { viewModel.fetchConfig() }
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SettingRow("College Name", uiState.settings?.collegeName)
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            SettingRow("Address", uiState.settings?.address)
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            SettingRow("Affiliation No.", uiState.settings?.affiliationNumber)
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            SettingRow("AICTE / UGC Code", uiState.settings?.aicteUgcCode)
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            SettingRow("Accreditation", uiState.settings?.accreditationBody)
                        }
                    }
                }
            }

            item {
                Text("Bank Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                CamsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingRow("Bank Name", uiState.settings?.bankName)
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        SettingRow("Account No.", uiState.settings?.bankAccountNo)
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        SettingRow("IFSC", uiState.settings?.bankIfsc)
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        SettingRow("Branch", uiState.settings?.bankBranch)
                    }
                }
            }

            item {
                Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                CamsCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Email Notifications", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Receive account and approval emails", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.emailNotificationsEnabled,
                            onCheckedChange = { viewModel.setEmailNotifications(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CamsNavy, checkedTrackColor = CamsNavy.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        uiState.settings?.let { current ->
            EditInstitutionDialog(
                current = current,
                isSaving = uiState.isSaving,
                onDismiss = { showEditDialog = false },
                onSave = { viewModel.saveSettings(it) }
            )
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(
            value?.takeIf { it.isNotBlank() } ?: "Not set",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun EditInstitutionDialog(
    current: com.example.features.admin.models.AdminSystemSettings,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (com.example.features.admin.models.AdminSystemSettings) -> Unit
) {
    var collegeName by remember { mutableStateOf(current.collegeName) }
    var address by remember { mutableStateOf(current.address) }
    var affiliationNumber by remember { mutableStateOf(current.affiliationNumber) }
    var aicteUgcCode by remember { mutableStateOf(current.aicteUgcCode) }
    var accreditationBody by remember { mutableStateOf(current.accreditationBody) }
    var bankName by remember { mutableStateOf(current.bankName) }
    var bankAccountNo by remember { mutableStateOf(current.bankAccountNo) }
    var bankIfsc by remember { mutableStateOf(current.bankIfsc) }
    var bankBranch by remember { mutableStateOf(current.bankBranch) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Institution Details") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 420.dp)) {
                item { OutlinedTextField(value = collegeName, onValueChange = { collegeName = it }, label = { Text("College Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = affiliationNumber, onValueChange = { affiliationNumber = it }, label = { Text("Affiliation Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = aicteUgcCode, onValueChange = { aicteUgcCode = it }, label = { Text("AICTE / UGC Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = accreditationBody, onValueChange = { accreditationBody = it }, label = { Text("Accreditation Body") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = bankAccountNo, onValueChange = { bankAccountNo = it }, label = { Text("Bank Account No.") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = bankIfsc, onValueChange = { bankIfsc = it }, label = { Text("IFSC") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = bankBranch, onValueChange = { bankBranch = it }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = collegeName.isNotBlank() && !isSaving,
                onClick = {
                    onSave(
                        current.copy(
                            collegeName = collegeName.trim(),
                            address = address.trim(),
                            affiliationNumber = affiliationNumber.trim(),
                            aicteUgcCode = aicteUgcCode.trim(),
                            accreditationBody = accreditationBody.trim(),
                            bankName = bankName.trim(),
                            bankAccountNo = bankAccountNo.trim(),
                            bankIfsc = bankIfsc.trim(),
                            bankBranch = bankBranch.trim()
                        )
                    )
                }
            ) { Text(if (isSaving) "Saving..." else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
