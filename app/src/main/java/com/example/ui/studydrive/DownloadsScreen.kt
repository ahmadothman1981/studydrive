package com.example.ui.studydrive

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadedFile
import com.example.data.model.DriveFile
import com.example.ui.components.FileTypeIcon

@Composable
fun DownloadsScreen(
    downloadedFiles: List<DownloadedFile>,
    totalStorageBytes: Long,
    allFiles: List<DriveFile>,
    isSuperAdmin: Boolean = false,
    onOpenFile: (Context, DriveFile) -> Unit,
    onShareFile: (Context, DriveFile) -> Unit,
    onDeleteDownload: (fileId: String, fileName: String) -> Unit,
    onEditMaterial: ((file: DriveFile, newTitle: String, newCategory: String, newDescription: String) -> Unit)? = null,
    onDeleteMaterialFromPortal: ((fileId: String) -> Unit)? = null,
    onPreviewFile: ((DriveFile) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = com.example.ui.locale.LocalAppStrings.current

    val formattedTotalStorage = rememberFormattedStorage(totalStorageBytes)

    var fileToEdit by remember { mutableStateOf<DriveFile?>(null) }
    var fileToDelete by remember { mutableStateOf<Pair<DownloadedFile, DriveFile>?>(null) }
    var offlineDownloadToDelete by remember { mutableStateOf<DownloadedFile?>(null) }

    fileToEdit?.let { file ->
        EditMaterialDialog(
            file = file,
            onDismiss = { fileToEdit = null },
            onSave = { newName, newCat, newDesc ->
                onEditMaterial?.invoke(file, newName, newCat, newDesc)
                fileToEdit = null
            }
        )
    }

    fileToDelete?.let { (download, driveFile) ->
        SuperAdminDeleteOptionsDialog(
            fileName = driveFile.name.ifBlank { download.fileName },
            onDismiss = { fileToDelete = null },
            onDeleteOfflineOnly = {
                onDeleteDownload(download.fileId, download.fileName)
                fileToDelete = null
            },
            onDeleteFromPortal = {
                onDeleteMaterialFromPortal?.invoke(download.fileId)
                fileToDelete = null
            }
        )
    }

    offlineDownloadToDelete?.let { target ->
        val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
        com.example.ui.components.SweetAlertConfirmDialog(
            type = com.example.ui.components.SweetAlertType.DELETE,
            title = if (isArabic) "حذف الملف من التنزيلات" else "Delete Downloaded File?",
            targetItemName = target.fileName,
            message = if (isArabic)
                "هل أنت متأكد من حذف هذا الملف المحمل من هاتفك؟ يمكنك إعادة تحميله لاحقاً في أي وقت."
            else
                "Are you sure you want to delete this downloaded file from your device? You can re-download it anytime.",
            confirmButtonText = if (isArabic) "نعم، حذف الملف" else "Yes, Delete File",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_delete_download_button",
            onConfirm = {
                onDeleteDownload(target.fileId, target.fileName)
                offlineDownloadToDelete = null
            },
            onDismiss = { offlineDownloadToDelete = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Storage Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = "Device Storage",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.downloadsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$formattedTotalStorage ${strings.storageUsed} • ${downloadedFiles.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.tabDownloads,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (downloadedFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = strings.noDownloads,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = strings.noDownloadsDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("downloaded_files_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(downloadedFiles, key = { it.fileId }) { download ->
                    val matchingDriveFile = allFiles.find { it.id == download.fileId }
                        ?: DriveFile(
                            id = download.fileId,
                            name = download.fileName,
                            mimeType = download.mimeType,
                            sizeBytes = download.fileSizeBytes,
                            isDownloaded = true,
                            localFilePath = download.localFilePath
                        )

                    DownloadedItemCard(
                        download = download,
                        driveFile = matchingDriveFile,
                        isSuperAdmin = isSuperAdmin,
                        onOpen = { onOpenFile(context, matchingDriveFile) },
                        onPreview = { onPreviewFile?.invoke(matchingDriveFile) ?: onOpenFile(context, matchingDriveFile) },
                        onShare = { onShareFile(context, matchingDriveFile) },
                        onEdit = if (isSuperAdmin) { { fileToEdit = matchingDriveFile } } else null,
                        onDelete = {
                            if (isSuperAdmin && onDeleteMaterialFromPortal != null) {
                                fileToDelete = Pair(download, matchingDriveFile)
                            } else {
                                offlineDownloadToDelete = download
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DownloadedItemCard(
    download: DownloadedFile,
    driveFile: DriveFile,
    isSuperAdmin: Boolean = false,
    onOpen: () -> Unit,
    onPreview: (() -> Unit)? = null,
    onShare: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = com.example.ui.locale.LocalAppStrings.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPreview?.invoke() ?: onOpen() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileTypeIcon(file = driveFile, containerSize = 42, iconSize = 22)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driveFile.name.ifBlank { download.fileName },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${driveFile.formattedSize} • ${driveFile.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(34.dp).testTag("open_download_${download.fileId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = strings.open,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = strings.share,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isSuperAdmin && onEdit != null) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp).testTag("edit_download_${download.fileId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.edit,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).testTag("delete_download_${download.fileId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = if (isSuperAdmin) strings.delete else strings.deleteFromDevice,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialDialog(
    file: DriveFile,
    onDismiss: () -> Unit,
    onSave: (newName: String, newCategory: String, newDescription: String) -> Unit
) {
    val strings = com.example.ui.locale.LocalAppStrings.current
    var name by remember { mutableStateOf(file.name) }
    var selectedCategory by remember { mutableStateOf(file.category.ifBlank { "Python" }) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(file.description) }

    val categories = listOf("Python", "Algorithms", "Past Exams", "Web Development", "C++", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.editMaterialTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.materialName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_material_name_input")
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.materialCategory) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("edit_material_category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.materialDescription) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("edit_material_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedCategory, description.trim())
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_edit_material_button")
            ) {
                Text(strings.saveChanges)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_edit_material_button")
            ) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun SuperAdminDeleteOptionsDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onDeleteOfflineOnly: () -> Unit,
    onDeleteFromPortal: () -> Unit
) {
    val strings = com.example.ui.locale.LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = strings.deleteConfirmationTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.deleteConfirmationDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDeleteFromPortal,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("delete_from_portal_button")
            ) {
                Text(strings.deleteMaterialPortal)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onDeleteOfflineOnly,
                    modifier = Modifier.testTag("delete_offline_only_button")
                ) {
                    Text(strings.deleteOfflineOnly)
                }
                TextButton(onClick = onDismiss) {
                    Text(strings.cancel)
                }
            }
        }
    )
}

private fun rememberFormattedStorage(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    return "%.1f MB".format(mb)
}
