package com.san3a.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.san3a.app.data.Crafts
import com.san3a.app.data.Regions
import com.san3a.app.data.UserRole
import com.san3a.app.ui.WhatsAppIcon
import com.san3a.app.ui.viewmodel.SetupUiState

@Composable
fun SetupScreen(
    ui: SetupUiState,
    phone: String,
    onRole: (UserRole) -> Unit,
    onName: (String) -> Unit,
    onPhone: (String) -> Unit,
    onWhatsapp: (String) -> Unit,
    onCraft: (String) -> Unit,
    onRegion: (String) -> Unit,
    onCaptureLocation: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            onCaptureLocation()
        }
    }

    fun ensureLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            onCaptureLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp)
    ) {
        Text(
            text = "أكمل ملفك",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (phone.isBlank()) "اختر نوع الحساب ثم أدخل بياناتك ورقم جوالك."
            else "اختر نوع الحساب ثم أدخل بياناتك. رقم الدخول: $phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleCard(
                title = "حرفي / مقدّم خدمة",
                icon = Icons.Filled.Badge,
                selected = ui.role == UserRole.CRAFTSMAN,
                onClick = { onRole(UserRole.CRAFTSMAN) },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                title = "زبون",
                icon = Icons.Filled.Person,
                selected = ui.role == UserRole.CUSTOMER,
                onClick = { onRole(UserRole.CUSTOMER) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))
        if (phone.isBlank()) {
            OutlinedTextField(
                value = ui.phone,
                onValueChange = onPhone,
                label = { Text("رقم الجوال") },
                placeholder = { Text("+9665xxxxxxxx") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = ui.name,
            onValueChange = onName,
            label = { Text("الاسم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = ui.whatsapp,
            onValueChange = onWhatsapp,
            label = { Text("رقم الواتساب (اختياري)") },
            placeholder = { Text("اتركه فارغًا لاستخدام رقم الدخول") },
            leadingIcon = { WhatsAppIcon(tint = MaterialTheme.colorScheme.primary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (ui.role == UserRole.CRAFTSMAN) {
            Spacer(Modifier.height(12.dp))
            DropdownField(
                label = "الحرفة",
                value = ui.craft,
                options = Crafts.list,
                onSelect = onCraft
            )
            Spacer(Modifier.height(12.dp))
            DropdownField(
                label = "منطقة العمل",
                value = ui.region,
                options = Regions.list,
                onSelect = onRegion
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { ensureLocationPermission() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(
                    text = if (ui.hasLocation) "تم تحديد موقعك ✓ (لقريب مني)"
                    else "استخدام موقعي الحالي (لقريب مني)",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (ui.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = ui.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSave,
            enabled = !ui.saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (ui.saving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("حفظ ومتابعة", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
