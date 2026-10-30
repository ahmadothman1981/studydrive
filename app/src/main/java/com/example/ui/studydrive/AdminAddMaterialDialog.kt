package com.example.ui.studydrive

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminMaterial
import com.example.data.model.SubjectFolder

val ADMIN_CATEGORIES = listOf("Python", "Algorithms", "Web Development", "C++", "Past Exams", "General")
val ADMIN_GRADES = listOf("الصف الأول الثانوي", "الصف الثاني الثانوي", "لجميع الصفوف")
val ADMIN_MIME_TYPES = listOf(
    "application/pdf" to "PDF Document (.pdf)",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "Word Document (.docx)",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation" to "PowerPoint (.pptx)",
    "text/markdown" to "Markdown / Text (.md)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddMaterialDialog(
    subjectFolders: List<SubjectFolder> = emptyList(),
    initialFolderId: String = "",
    onDismiss: () -> Unit,
    onAddMaterial: (AdminMaterial, Uri?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableLongStateOf(0L) }

    val availableFolders = remember(subjectFolders) {
        subjectFolders.filter { it.folderId != "study_materials_root" }
    }
    val matchedFolder = remember(availableFolders, initialFolderId) {
        availableFolders.find { (it.folderId.ifBlank { it.id }) == initialFolderId }
    }
    var selectedCategory by remember(matchedFolder) {
        mutableStateOf(matchedFolder?.subject?.takeIf { it.isNotBlank() && it != "All" } ?: ADMIN_CATEGORIES.first())
    }
    var categoryExpanded by remember { mutableStateOf(false) }

    var selectedFolderId by remember(availableFolders, initialFolderId) {
        mutableStateOf(matchedFolder?.let { it.folderId.ifBlank { it.id } } ?: availableFolders.firstOrNull()?.let { it.folderId.ifBlank { it.id } } ?: "study_materials_root")
    }
    var folderExpanded by remember { mutableStateOf(false) }

    var selectedGrade by remember { mutableStateOf(ADMIN_GRADES.first()) }
    var gradeExpanded by remember { mutableStateOf(false) }

    var selectedMime by remember { mutableStateOf(ADMIN_MIME_TYPES.first().first) }
    var mimeExpanded by remember { mutableStateOf(false) }

    var driveLinkOrId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Activity launcher to pick any document from mobile phone storage
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            var displayName = ""
            var size = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: ""
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }
            } catch (_: Exception) {}

            if (displayName.isBlank()) {
                displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "study_file_${System.currentTimeMillis()}"
            }
            selectedFileName = displayName
            selectedFileSize = size

            // Auto-fill title if empty
            if (title.isBlank()) {
                val cleanTitle = displayName.substringBeforeLast('.')
                title = cleanTitle.replace("_", " ")
            }

            // Auto-detect MIME type & Category
            val lowerName = displayName.lowercase()
            selectedMime = when {
                lowerName.endsWith(".pdf") -> "application/pdf"
                lowerName.endsWith(".docx") || lowerName.endsWith(".doc") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                lowerName.endsWith(".pptx") || lowerName.endsWith(".ppt") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                lowerName.endsWith(".md") || lowerName.endsWith(".txt") -> "text/markdown"
                else -> context.contentResolver.getType(uri) ?: "application/pdf"
            }

            when {
                lowerName.contains("python") || lowerName.contains("بايثون") -> selectedCategory = "Python"
                lowerName.contains("algo") || lowerName.contains("خوارزم") -> selectedCategory = "Algorithms"
                lowerName.contains("web") || lowerName.contains("ويب") || lowerName.contains("html") -> selectedCategory = "Web Development"
                lowerName.contains("exam") || lowerName.contains("امتحان") || lowerName.contains("بنك") -> selectedCategory = "Past Exams"
                lowerName.contains("c++") || lowerName.contains("cpp") -> selectedCategory = "C++"
            }
            errorMessage = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "رفع مذكرة دراسية جديدة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "خاص بالمشرف العام • هاتف + Google Drive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Mobile File Picker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFileUri != null)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedFileUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ملف من ذاكرة الهاتف",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (selectedFileUri != null) {
                                Surface(
                                    color = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "✓ تم الاختيار",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (selectedFileUri == null) {
                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pick_mobile_file_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "اختيار ملف من الهاتف (PDF، Word، إلخ)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = selectedFileName ?: "ملف محدد",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = formatFileSize(selectedFileSize),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row {
                                    TextButton(
                                        onClick = { filePickerLauncher.launch("*/*") }
                                    ) {
                                        Text("تغيير", fontSize = 11.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedFileUri = null
                                            selectedFileName = null
                                            selectedFileSize = 0L
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "إزالة",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Google Drive Link
                OutlinedTextField(
                    value = driveLinkOrId,
                    onValueChange = {
                        driveLinkOrId = it
                        errorMessage = null
                    },
                    label = { Text("رابط Google Drive للمذكرة (أو ارفع ملفاً من الهاتف أعلاه)") },
                    placeholder = { Text("https://drive.google.com/file/d/...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "يمكنك إضافة المذكرة عبر رابط Google Drive فقط دون رفع ملف من الهاتف. تأكد أن إذن المشاركة في Drive مضبوط على: 'أي شخص لديه الرابط' (Anyone with the link).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("material_drive_id_input")
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("عنوان المذكرة أو الدرس *") },
                    placeholder = { Text("مثال: ملخص بايثون - الترم الأول") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("material_title_input")
                )

                // Subject Folder Dropdown
                if (availableFolders.isNotEmpty()) {
                    val selectedFolderObj = availableFolders.find {
                        val fid = it.folderId.ifBlank { it.id }
                        fid == selectedFolderId
                    }
                    ExposedDropdownMenuBox(
                        expanded = folderExpanded,
                        onExpandedChange = { folderExpanded = !folderExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedFolderObj?.name ?: "المجلد الرئيسي (عام)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مجلد المادة الدراسية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = folderExpanded,
                            onDismissRequest = { folderExpanded = false }
                        ) {
                            availableFolders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text("${folder.name} (${folder.subject})") },
                                    onClick = {
                                        selectedFolderId = folder.folderId.ifBlank { folder.id }
                                        if (folder.subject.isNotBlank() && folder.subject != "All") {
                                            selectedCategory = folder.subject
                                        }
                                        folderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("القسم / المادة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ADMIN_CATEGORIES.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Grade Dropdown
                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = !gradeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المرحلة الدراسية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false }
                    ) {
                        ADMIN_GRADES.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    selectedGrade = grade
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Mime Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = mimeExpanded,
                    onExpandedChange = { mimeExpanded = !mimeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val displayMime = ADMIN_MIME_TYPES.find { it.first == selectedMime }?.second ?: selectedMime
                    OutlinedTextField(
                        value = displayMime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الملف") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mimeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = mimeExpanded,
                        onDismissRequest = { mimeExpanded = false }
                    ) {
                        ADMIN_MIME_TYPES.forEach { (mime, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedMime = mime
                                    mimeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("نبذة مختصرة أو تعليمات للطلاب") },
                    placeholder = { Text("أهم التمارين والأفكار الأساسية...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "يرجى كتابة عنوان المذكرة"
                        return@Button
                    }
                    if (selectedFileUri == null && driveLinkOrId.isBlank()) {
                        errorMessage = "يرجى اختيار ملف من الهاتف أو إدخال رابط Google Drive للمذكرة"
                        return@Button
                    }

                    val cleanId = if (driveLinkOrId.isNotBlank()) {
                        val input = driveLinkOrId.trim()
                        when {
                            input.contains("/file/d/") -> input.substringAfter("/file/d/").substringBefore("/").substringBefore("?")
                            input.contains("id=") -> input.substringAfter("id=").substringBefore("&")
                            else -> input
                        }
                    } else {
                        "admin_mat_" + System.currentTimeMillis()
                    }

                    val extension = when (selectedMime) {
                        "application/pdf" -> ".pdf"
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx"
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> ".pptx"
                        else -> ".md"
                    }
                    val fileName = if (title.contains(".")) title else "$title$extension"
                    val finalSizeBytes = if (selectedFileSize > 0L) selectedFileSize else 2_500_000L

                    val newMaterial = AdminMaterial(
                        id = cleanId,
                        fileId = cleanId,
                        name = fileName,
                        category = selectedCategory,
                        grade = selectedGrade,
                        mimeType = selectedMime,
                        sizeBytes = finalSizeBytes,
                        description = description.ifBlank { "مذكرة دراسية معتمدة - إعداد وتقديم المهندس أحمد عثمان." },
                        downloadUrl = driveLinkOrId.trim(),
                        folderId = selectedFolderId
                    )
                    onAddMaterial(newMaterial, selectedFileUri)
                    onDismiss()
                },
                modifier = Modifier.testTag("submit_material_button")
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ ونشر المذكرة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f MB", mb)
    } else {
        String.format(java.util.Locale.US, "%.0f KB", kb)
    }
}
