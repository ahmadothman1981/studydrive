package com.example.ui.studydrive

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriveFile
import com.example.data.model.SubjectFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialDialog(
    file: DriveFile,
    subjectFolders: List<SubjectFolder> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        fileId: String,
        name: String,
        category: String,
        department: String,
        grade: String,
        folderId: String,
        description: String
    ) -> Unit
) {
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC

    var title by remember { mutableStateOf(file.name) }
    var selectedCategory by remember { mutableStateOf(file.category.ifBlank { "Python" }) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var selectedDepartment by remember { mutableStateOf(file.department.ifBlank { "Computer Science" }) }
    var departmentExpanded by remember { mutableStateOf(false) }

    var selectedGrade by remember { mutableStateOf("جميع الصفوف") }
    var gradeExpanded by remember { mutableStateOf(false) }

    var selectedFolderId by remember { mutableStateOf(file.parentFolderId.ifBlank { "study_materials_root" }) }
    var folderExpanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf(file.description) }

    val categories = listOf("Python", "Algorithms", "Web Development", "C++", "Past Exams", "Database", "General")
    val departments = listOf("Computer Science", "Engineering", "Business", "Medicine", "General Secondary")
    val grades = listOf("الصف الأول الثانوي", "الصف الثاني الثانوي", "جميع الصفوف")

    val selectableFolders = remember(subjectFolders) {
        if (subjectFolders.any { it.folderId == "study_materials_root" }) {
            subjectFolders
        } else {
            listOf(
                SubjectFolder(
                    id = "study_materials_root",
                    folderId = "study_materials_root",
                    name = if (isArabic) "المجلد الرئيسي الشامل" else "Root Directory",
                    subject = "All",
                    department = "All"
                )
            ) + subjectFolders
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("edit_material_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "تعديل بيانات المذكرة" else "Edit Material",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name / Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isArabic) "عنوان أو اسم المذكرة" else "Material Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_material_name_input")
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isArabic) "المادة / التصنيف" else "Subject / Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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

                // Department Dropdown
                ExposedDropdownMenuBox(
                    expanded = departmentExpanded,
                    onExpandedChange = { departmentExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedDepartment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isArabic) "القسم الدراسي / المرحلة" else "Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = departmentExpanded,
                        onDismissRequest = { departmentExpanded = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    selectedDepartment = dept
                                    departmentExpanded = false
                                }
                            )
                        }
                    }
                }

                // Target Subject Folder Dropdown
                val currentFolder = selectableFolders.find { (it.folderId.ifBlank { it.id }) == selectedFolderId }
                ExposedDropdownMenuBox(
                    expanded = folderExpanded,
                    onExpandedChange = { folderExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentFolder?.name ?: selectedFolderId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isArabic) "المجلد المستهدف للمذكرة" else "Target Folder") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = folderExpanded,
                        onDismissRequest = { folderExpanded = false }
                    ) {
                        selectableFolders.forEach { folder ->
                            val fId = folder.folderId.ifBlank { folder.id }
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    selectedFolderId = fId
                                    folderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Grade Dropdown
                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isArabic) "الصف الدراسي" else "Grade Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false }
                    ) {
                        grades.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    selectedGrade = g
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isArabic) "نبذة وتفاصيل المذكرة" else "Description") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("edit_material_description_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            file.id,
                            title.trim(),
                            selectedCategory,
                            selectedDepartment,
                            selectedGrade,
                            selectedFolderId,
                            description.trim()
                        )
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_edit_material_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isArabic) "حفظ التعديلات" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
