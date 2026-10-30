package com.example.ui.studydrive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectFolder

@Composable
fun FolderSelectDialog(
    currentFolderId: String,
    currentFolderName: String,
    subjectFolders: List<SubjectFolder> = emptyList(),
    allowedFolderIds: List<String> = emptyList(),
    isSuperAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onFolderSelected: (folderId: String, folderName: String) -> Unit,
    onCreateFolder: (name: String, subject: String, department: String, grade: String, description: String, driveFolderUrl: String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateFolder: (SubjectFolder) -> Unit = {},
    onDeleteFolder: (folderId: String) -> Unit = {}
) {
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
    var selectedId by remember(currentFolderId) { mutableStateOf(currentFolderId) }
    var selectedName by remember(currentFolderName) { mutableStateOf(currentFolderName) }
    var customFolderInput by remember { mutableStateOf("") }
    var customFolderName by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val isRestricted = !isSuperAdmin

    val visibleFolders = remember(subjectFolders, allowedFolderIds, isRestricted) {
        if (isRestricted) {
            subjectFolders.filter { folder ->
                val fId = folder.folderId.ifBlank { folder.id }
                fId in allowedFolderIds && fId != "study_materials_root"
            }
        } else {
            subjectFolders
        }
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<SubjectFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<SubjectFolder?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isRestricted) Icons.Default.Folder else Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRestricted) {
                            if (isArabic) "المجلدات المصرح بها لحسابك" else "Your Assigned Folders"
                        } else {
                            if (isArabic) "مجلدات المواد الدراسية" else "Subject Study Folders"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isSuperAdmin) {
                    FilledTonalButton(
                        onClick = { showCreateFolderDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("dialog_create_subject_folder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "إنشاء مجلد" else "New Folder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isRestricted) {
                        if (isArabic)
                            "المجلدات المخصصة لحسابك من قبل المشرف:"
                        else
                            "Subject folders assigned to your account by admin:"
                    } else if (isArabic) {
                        "اختر مجلد المادة لعرض مذكراته وملفاته، أو اربط مجلد Google Drive مخصص:"
                    } else {
                        "Choose a subject folder to browse its materials, or connect a custom Drive folder:"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isRestricted && visibleFolders.isEmpty()) {
                    Text(
                        text = if (isArabic)
                            "لم يتم تعيين مجلدات مصرح بها لحسابك بعد. يرجى مراجعة المشرف."
                        else
                            "No folders have been assigned to your account yet. Please contact admin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                visibleFolders.forEach { folder ->
                    val fId = folder.folderId.ifBlank { folder.id }
                    val isSelected = selectedId == fId && !showCustomInput
                    val isRoot = fId == "study_materials_root"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedId = fId
                                selectedName = folder.name
                                showCustomInput = false
                            }
                            .testTag("folder_option_$fId"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedId = fId
                                    selectedName = folder.name
                                    showCustomInput = false
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = folder.subject,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                    if (folder.grade.isNotBlank()) {
                                        Text(
                                            text = "• ${folder.grade}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (folder.description.isNotBlank()) {
                                    Text(
                                        text = folder.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isSuperAdmin && !isRoot) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { folderToEdit = folder },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("edit_folder_$fId")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Subject Folder",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { folderToDelete = folder },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("delete_folder_$fId")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Subject Folder",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Google Drive folder option (only for unrestricted users / admins)
                if (!isRestricted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomInput = true },
                        colors = CardDefaults.cardColors(
                            containerColor = if (showCustomInput) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = showCustomInput,
                                onClick = { showCustomInput = true }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "ربط مجلد Google Drive مخصص (رابط أو ID)" else "Connect Custom Drive Folder (ID or URL)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showCustomInput) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = customFolderInput,
                                onValueChange = { customFolderInput = it },
                                label = { Text(if (isArabic) "معرف المجلد أو الرابط المشترك" else "Folder ID or Shared Link") },
                                placeholder = { Text("e.g., 1B2c3D4e5F6g7H8i9J0k...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_folder_input")
                            )

                            OutlinedTextField(
                                value = customFolderName,
                                onValueChange = { customFolderName = it },
                                label = { Text(if (isArabic) "اسم المجلد المعروض (اختياري)" else "Display Label (optional)") },
                                placeholder = { Text(if (isArabic) "مثال: مذكرات المراجعة النهائية" else "e.g. My Semester Notes") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (showCustomInput) {
                        val input = customFolderInput.trim()
                        val extractedId = when {
                            input.contains("/folders/") -> input.substringAfter("/folders/").substringBefore("?").substringBefore("/")
                            input.contains("id=") -> input.substringAfter("id=").substringBefore("&")
                            else -> input
                        }
                        val folderName = customFolderName.trim().ifBlank {
                            if (isArabic) "مجلد دراسي مخصص" else "Custom Study Folder"
                        }
                        onFolderSelected(extractedId.ifBlank { "study_materials_root" }, folderName)
                    } else {
                        onFolderSelected(selectedId, selectedName)
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("apply_folder_button")
            ) {
                Text(if (isArabic) "فتح المجلد" else "Select Folder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )

    if (showCreateFolderDialog) {
        SubjectFolderEditorDialog(
            initialFolder = null,
            onDismiss = { showCreateFolderDialog = false },
            onSave = { name, subject, department, grade, description, driveUrl ->
                onCreateFolder(name, subject, department, grade, description, driveUrl)
                showCreateFolderDialog = false
            }
        )
    }

    folderToEdit?.let { target ->
        SubjectFolderEditorDialog(
            initialFolder = target,
            onDismiss = { folderToEdit = null },
            onSave = { name, subject, department, grade, description, driveUrl ->
                onUpdateFolder(
                    target.copy(
                        name = name,
                        subject = subject,
                        department = department,
                        grade = grade,
                        description = description,
                        driveFolderUrl = driveUrl
                    )
                )
                folderToEdit = null
            }
        )
    }

    folderToDelete?.let { target ->
        DeleteSubjectFolderConfirmDialog(
            folder = target,
            onDismiss = { folderToDelete = null },
            onConfirmDelete = {
                val fId = target.folderId.ifBlank { target.id }
                onDeleteFolder(fId)
                if (selectedId == fId) {
                    selectedId = "study_materials_root"
                    selectedName = if (isArabic) "جميع المواد والمجلدات (الرئيسي)" else "All Study Materials"
                }
                folderToDelete = null
            }
        )
    }
}

@Composable
fun SubjectFolderEditorDialog(
    initialFolder: SubjectFolder? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, subject: String, department: String, grade: String, description: String, driveFolderUrl: String) -> Unit
) {
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
    val isEditMode = initialFolder != null

    var name by remember(initialFolder) { mutableStateOf(initialFolder?.name.orEmpty()) }
    var subject by remember(initialFolder) { mutableStateOf(initialFolder?.subject?.ifBlank { "Python" } ?: "Python") }
    var department by remember(initialFolder) { mutableStateOf(initialFolder?.department?.ifBlank { "Computer Science" } ?: "Computer Science") }
    var grade by remember(initialFolder) { mutableStateOf(initialFolder?.grade?.ifBlank { "الصف الأول الثانوي" } ?: "الصف الأول الثانوي") }
    var description by remember(initialFolder) { mutableStateOf(initialFolder?.description.orEmpty()) }
    var driveFolderUrl by remember(initialFolder) { mutableStateOf(initialFolder?.driveFolderUrl.orEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subjectOptions = listOf("Python", "Algorithms", "Past Exams", "Web Development", "Database", "C++", "AI", "General")
    val departmentOptions = listOf("Computer Science", "Engineering", "Business", "Medicine", "General Secondary")
    val gradeOptions = listOf("الصف الأول الثانوي", "الصف الثاني الثانوي", "الصف الثالث الثانوي", "جميع الصفوف")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.CreateNewFolder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) {
                        if (isArabic) "تعديل مجلد المادة" else "Edit Subject Folder"
                    } else {
                        if (isArabic) "إنشاء مجلد مادة جديد" else "Create Subject Folder"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text(if (isArabic) "اسم مجلد المادة" else "Subject Folder Name") },
                    placeholder = { Text(if (isArabic) "مثال: مجلد برمجة بايثون المتقدمة" else "e.g. Advanced Python Folder") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_folder_name_input")
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(if (isArabic) "اسم المادة / التخصص" else "Subject / Course") },
                    placeholder = { Text(if (isArabic) "مثال: Python أو فيزياء" else "e.g. Python, Physics") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_folder_subject_input")
                )

                // Quick Subject Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectOptions.forEach { option ->
                        FilterChip(
                            selected = subject.equals(option, ignoreCase = true),
                            onClick = { subject = option },
                            label = { Text(option, fontSize = 11.sp) }
                        )
                    }
                }

                // Department Selector
                Text(
                    text = if (isArabic) "القسم الأكاديمي / الكلية:" else "Department / Major:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departmentOptions.forEach { dept ->
                        FilterChip(
                            selected = department.equals(dept, ignoreCase = true),
                            onClick = { department = dept },
                            label = { Text(dept, fontSize = 11.sp) }
                        )
                    }
                }

                // Grade Selector
                Text(
                    text = if (isArabic) "المرحلة / الصف الدراسي:" else "Academic Grade:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    gradeOptions.forEach { gr ->
                        FilterChip(
                            selected = grade == gr,
                            onClick = { grade = gr },
                            label = { Text(gr, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isArabic) "وصف المجلد ومحتوياته" else "Folder Description") },
                    placeholder = { Text(if (isArabic) "نبذة عن المذكرات والمحاضرات داخل المجلد..." else "Summary of folder contents...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_folder_description_input")
                )

                OutlinedTextField(
                    value = driveFolderUrl,
                    onValueChange = { driveFolderUrl = it },
                    label = { Text(if (isArabic) "رابط أو معرف Google Drive (اختياري)" else "Google Drive Folder URL/ID (Optional)") },
                    placeholder = { Text("https://drive.google.com/drive/folders/...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_folder_drive_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = if (isArabic) "يرجى إدخال اسم مجلد المادة" else "Please enter a folder name"
                        return@Button
                    }
                    onSave(
                        name.trim(),
                        subject.trim().ifBlank { "General" },
                        department.trim().ifBlank { "Computer Science" },
                        grade.trim().ifBlank { "جميع الصفوف" },
                        description.trim(),
                        driveFolderUrl.trim()
                    )
                },
                modifier = Modifier.testTag("save_subject_folder_button")
            ) {
                Text(if (isEditMode) (if (isArabic) "حفظ التعديلات" else "Save Changes") else (if (isArabic) "إنشاء المجلد" else "Create Folder"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
fun DeleteSubjectFolderConfirmDialog(
    folder: SubjectFolder,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
    com.example.ui.components.SweetAlertConfirmDialog(
        type = com.example.ui.components.SweetAlertType.FOLDER,
        title = if (isArabic) "حذف مجلد المادة الدراسية" else "Delete Subject Folder?",
        targetItemName = folder.name,
        targetCategory = "${folder.subject} • ${folder.grade}",
        message = if (isArabic)
            "هل أنت متأكد من حذف هذا المجلد؟ لن يتمكن الطلاب من الوصول إلى ملفات ومذكرات هذا المجلد بعد الحذف."
        else
            "Are you sure you want to delete this folder? Students will no longer be able to access materials in this folder.",
        confirmButtonText = if (isArabic) "نعم، حذف المجلد نهائياً" else "Yes, Delete Folder",
        cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
        confirmTag = "confirm_delete_subject_folder_button",
        onConfirm = onConfirmDelete,
        onDismiss = onDismiss
    )
}
