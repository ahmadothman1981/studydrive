package com.example.ui.studydrive

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.ui.components.SweetAlertConfirmDialog
import com.example.ui.components.SweetAlertType
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.AdminMaterial
import com.example.data.model.AdminUser
import com.example.data.model.DriveFile
import com.example.data.model.SUPER_ADMIN_EMAIL
import com.example.data.model.StudyAnnouncement
import com.example.data.model.SubjectFolder
import com.example.data.model.UserProfile
import com.example.ui.components.CategoryChip
import com.example.ui.components.FileTypeIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminMaterials: List<AdminMaterial>,
    subjectFolders: List<SubjectFolder> = emptyList(),
    allStudents: List<UserProfile>,
    admins: List<AdminUser>,
    currentUserEmail: String,
    allDriveFiles: List<DriveFile>,
    activeFolderName: String,
    announcements: List<StudyAnnouncement> = emptyList(),
    totalStorageUsedBytes: Long = 0L,
    onAddMaterialClick: () -> Unit,
    onDeleteMaterialClick: (fileId: String) -> Unit,
    onEditMaterialClick: ((fileId: String, name: String, category: String, department: String, grade: String, folderId: String, description: String) -> Unit)? = null,
    onPreviewMaterialClick: ((DriveFile) -> Unit)? = null,
    onChangeFolderClick: () -> Unit,
    onCreateSubjectFolder: (name: String, subject: String, department: String, grade: String, description: String, driveFolderUrl: String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateSubjectFolder: (SubjectFolder) -> Unit = {},
    onDeleteSubjectFolder: (folderId: String) -> Unit = {},
    onApproveStudent: (studentId: String) -> Unit,
    onApproveStudentWithFolders: (studentId: String, folderIds: List<String>) -> Unit = { _, _ -> },
    onAssignStudentFolders: (studentId: String, folderIds: List<String>) -> Unit = { _, _ -> },
    onRejectStudent: (studentId: String) -> Unit,
    onBulkApproveStudents: (studentIds: List<String>) -> Unit = {},
    onBulkRejectStudents: (studentIds: List<String>) -> Unit = {},
    onPostAnnouncement: (title: String, content: String, type: String, isPinned: Boolean) -> Unit = { _, _, _, _ -> },
    onDeleteAnnouncement: (id: String) -> Unit = {},
    onAddAdminClick: () -> Unit,
    onRemoveAdminClick: (adminDocId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = com.example.ui.locale.LocalAppStrings.current
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var selectedDepartment by remember { mutableStateOf("All") }
    var showPostAnnouncementDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<SubjectFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<SubjectFolder?>(null) }
    var studentToAssignFolders by remember { mutableStateOf<UserProfile?>(null) }
    var studentToApproveWithFolders by remember { mutableStateOf<UserProfile?>(null) }

    // Bulk selection state for pending students
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }

    // SweetAlert confirmation states
    var materialToDelete by remember { mutableStateOf<AdminMaterial?>(null) }
    var fileToDelete by remember { mutableStateOf<DriveFile?>(null) }
    var fileToEdit by remember { mutableStateOf<DriveFile?>(null) }
    var materialsSearchQuery by remember { mutableStateOf("") }
    var selectedFolderFilter by remember { mutableStateOf("All") }
    var studentToReject by remember { mutableStateOf<UserProfile?>(null) }
    var showBulkRejectAlert by remember { mutableStateOf(false) }
    var announcementToDelete by remember { mutableStateOf<StudyAnnouncement?>(null) }
    var adminToRemove by remember { mutableStateOf<AdminUser?>(null) }

    val isSuperAdmin = currentUserEmail.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
    val pendingStudents = allStudents.filter { it.status == "pending" && !it.isSuperAdmin }
    val approvedStudents = allStudents.filter { it.status == "approved" && !it.isSuperAdmin }
    val totalRegistered = allStudents.filter { !it.isSuperAdmin }.size

    // Unified platform materials: Admin uploaded materials + Drive materials (deduplicated)
    val allPlatformMaterials = remember(adminMaterials, allDriveFiles) {
        val map = linkedMapOf<String, DriveFile>()
        adminMaterials.forEach { map[it.toDriveFile().id] = it.toDriveFile() }
        allDriveFiles.forEach { file ->
            if (!map.containsKey(file.id)) {
                map[file.id] = file
            }
        }
        map.values.toList()
    }

    // Most downloaded materials ranking
    val mostDownloadedMaterials = remember(allPlatformMaterials) {
        allPlatformMaterials.sortedByDescending { it.downloadCount }.take(3)
    }

    // Filtered materials by department, folder, and search query
    val filteredPlatformMaterials = remember(allPlatformMaterials, selectedDepartment, selectedFolderFilter, materialsSearchQuery) {
        val q = materialsSearchQuery.trim()
        allPlatformMaterials.filter { file ->
            val matchesDept = selectedDepartment == "All" || file.department.equals(selectedDepartment, ignoreCase = true)
            val matchesFolder = selectedFolderFilter == "All" || file.parentFolderId == selectedFolderFilter
            val matchesQuery = q.isBlank() ||
                    file.name.contains(q, ignoreCase = true) ||
                    file.category.contains(q, ignoreCase = true) ||
                    file.courseName.contains(q, ignoreCase = true) ||
                    file.description.contains(q, ignoreCase = true) ||
                    file.professor.contains(q, ignoreCase = true)
            matchesDept && matchesFolder && matchesQuery
        }
    }

    val departmentLabels = mapOf(
        "All" to if (isArabic) "جميع الأقسام" else "All Departments",
        "Computer Science" to if (isArabic) "علوم الحاسب" else "Computer Science",
        "Engineering" to if (isArabic) "هندسة" else "Engineering",
        "Business" to if (isArabic) "إدارة أعمال" else "Business",
        "Medicine" to if (isArabic) "طب وعلوم صحية" else "Medicine",
        "General Secondary" to if (isArabic) "ثانوية وبكالوريا" else "Secondary School"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("admin_dashboard_list"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Hero Photo Header Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_banner),
                            contentDescription = strings.appTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xCC001833)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSuperAdmin) Color(0xFFD32F2F) else Color(0xFF00C853)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSuperAdmin) strings.superAdminBadge else strings.adminBadge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${strings.teacherName} • ${strings.adminTitle}",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = strings.statPendingRequests,
                        value = "${pendingStudents.size}",
                        icon = Icons.Default.HourglassTop,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedSubTab = 0 }
                    )
                    StatCard(
                        title = strings.statApprovedStudents,
                        value = "${approvedStudents.size}",
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedSubTab = 0 }
                    )
                    StatCard(
                        title = strings.statMaterials,
                        value = "${allPlatformMaterials.size}",
                        icon = Icons.Default.Folder,
                        modifier = Modifier.weight(1f).testTag("stat_card_materials"),
                        onClick = {
                            selectedSubTab = 1
                            materialsSearchQuery = ""
                            selectedDepartment = "All"
                            selectedFolderFilter = "All"
                        }
                    )
                }
            }

            // Dashboard Analytics & Statistics Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("admin_analytics_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تحليلات وإحصائيات المنصة" else "Platform Analytics & Insights",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${totalRegistered} ${if (isArabic) "طالب مسجل" else "Students"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Student Approval Breakdown Progress
                        val approvalRate = if (totalRegistered > 0) approvedStudents.size.toFloat() / totalRegistered else 0f
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "نسبة اعتماد الطلاب: ${(approvalRate * 100).toInt()}%" else "Approval Rate: ${(approvalRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isArabic) "${pendingStudents.size} بانتظار الموافقة" else "${pendingStudents.size} Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pendingStudents.isNotEmpty()) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { approvalRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF2E7D32),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Storage and Total Files Info Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${allDriveFiles.size + adminMaterials.size}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isArabic) "إجمالي الملفات والمذكرات" else "Total Materials",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatStorageSize(totalStorageUsedBytes),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = if (isArabic) "مساحة التخزين المؤقت" else "Cache Storage",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Most Downloaded Materials Ranking
                        Text(
                            text = if (isArabic) "أكثر المذكرات والمواد تحميلاً:" else "Most Downloaded Study Materials:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        mostDownloadedMaterials.forEachIndexed { index, file ->
                            val rankEmoji = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                else -> "🥉"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = rankEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${file.category} • ${file.department}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${file.downloadCount} ${if (isArabic) "تنزيل" else "downloads"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sub-Navigation Tabs
            item {
                SecondaryTabRow(
                    selectedTabIndex = selectedSubTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(strings.tabPendingStudents)
                                if (pendingStudents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(
                                            text = "${pendingStudents.size}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = { Text(strings.tabAdminMaterials) }
                    )
                    Tab(
                        selected = selectedSubTab == 2,
                        onClick = { selectedSubTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isArabic) "الإعلانات" else "Notices")
                                if (announcements.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "${announcements.size}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                    if (isSuperAdmin) {
                        Tab(
                            selected = selectedSubTab == 3,
                            onClick = { selectedSubTab = 3 },
                            text = { Text(strings.tabManageAdmins) }
                        )
                    }
                }
            }

            // ==========================================
            // TAB 0: Student Approvals & Bulk Actions
            // ==========================================
            if (selectedSubTab == 0) {
                // Bulk Student Selection Bar
                if (pendingStudents.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag("bulk_selection_bar"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedStudentIds.size == pendingStudents.size && pendingStudents.isNotEmpty(),
                                        onCheckedChange = { checked ->
                                            selectedStudentIds = if (checked) pendingStudents.map { it.userId }.toSet() else emptySet()
                                        },
                                        modifier = Modifier.testTag("select_all_pending_checkbox")
                                    )
                                    Text(
                                        text = if (selectedStudentIds.isNotEmpty())
                                            "${selectedStudentIds.size} ${if (isArabic) "محدد" else "selected"}"
                                        else
                                            if (isArabic) "تحديد الكل (${pendingStudents.size})" else "Select All (${pendingStudents.size})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (selectedStudentIds.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                showBulkRejectAlert = true
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp).testTag("bulk_reject_button")
                                        ) {
                                            Text(if (isArabic) "رفض (${selectedStudentIds.size})" else "Reject (${selectedStudentIds.size})", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onBulkApproveStudents(selectedStudentIds.toList())
                                                selectedStudentIds = emptySet()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp).testTag("bulk_approve_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(if (isArabic) "قبول (${selectedStudentIds.size})" else "Approve (${selectedStudentIds.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "${strings.pendingSectionTitle} (${pendingStudents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                if (pendingStudents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.noPendingRequests,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = strings.noPendingDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(pendingStudents, key = { it.userId }) { student ->
                        val isChecked = selectedStudentIds.contains(student.userId)
                        PendingStudentCardWithCheckbox(
                            student = student,
                            isSelected = isChecked,
                            onToggleSelect = {
                                selectedStudentIds = if (isChecked) selectedStudentIds - student.userId else selectedStudentIds + student.userId
                            },
                            onApprove = { onApproveStudent(student.userId) },
                            onApproveWithFolders = { studentToApproveWithFolders = student },
                            onReject = { studentToReject = student }
                        )
                    }
                }

                // Approved Students Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${strings.approvedSectionTitle} (${approvedStudents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (approvedStudents.isEmpty()) {
                    item {
                        Text(
                            text = strings.noApprovedStudents,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(approvedStudents, key = { it.userId }) { student ->
                        ApprovedStudentCard(
                            student = student,
                            subjectFolders = subjectFolders,
                            onAssignFolders = { studentToAssignFolders = student },
                            onRevoke = { studentToReject = student }
                        )
                    }
                }
            }

            // ==========================================
            // TAB 1: Subject Folders & Study Materials Management
            // ==========================================
            if (selectedSubTab == 1) {
                // Subject Folders Management Section (Super Admin Create / Edit / Delete)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("admin_subject_folders_section"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isArabic) "مجلدات المواد الدراسية (${subjectFolders.size})" else "Subject Folders (${subjectFolders.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isArabic) "إنشاء وتعديل وحذف مجلدات المواد" else "Create, edit, and delete subject folders",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSuperAdmin) {
                                    Button(
                                        onClick = { showCreateFolderDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("admin_create_subject_folder_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreateNewFolder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isArabic) "إنشاء مجلد مادة" else "New Folder",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            subjectFolders.forEach { folder ->
                                val fId = folder.folderId.ifBlank { folder.id }
                                val isRoot = fId == "study_materials_root"

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_folder_card_$fId"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CategoryChip(category = folder.subject)
                                                if (folder.department.isNotBlank() && folder.department != "All") {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                    ) {
                                                        Text(
                                                            text = folder.department,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                if (folder.grade.isNotBlank()) {
                                                    Text(
                                                        text = folder.grade,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                            if (folder.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = folder.description,
                                                    style = MaterialTheme.typography.bodySmall,
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
                                                        .size(34.dp)
                                                        .testTag("admin_edit_folder_$fId")
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
                                                        .size(34.dp)
                                                        .testTag("admin_delete_folder_$fId")
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
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "جميع المذكرات والملفات بالمنصة (${filteredPlatformMaterials.size})" else "All Platform Materials (${filteredPlatformMaterials.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "إجمالي المذكرات المتاحة: ${allPlatformMaterials.size} ملف" else "Total materials: ${allPlatformMaterials.size} files",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSuperAdmin) {
                            FilledTonalButton(
                                onClick = onAddMaterialClick,
                                modifier = Modifier.testTag("add_material_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "رفع مذكرة جديدة" else "Upload Material", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Search field for all materials
                item {
                    OutlinedTextField(
                        value = materialsSearchQuery,
                        onValueChange = { materialsSearchQuery = it },
                        placeholder = { Text(if (isArabic) "بحث بالاسم، المساق، الأستاذ، المادة..." else "Search by name, course, prof...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (materialsSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { materialsSearchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("admin_materials_search_input")
                    )
                }

                // Department filter chips row for Admin
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Computer Science", "Engineering", "Business", "Medicine", "General Secondary").forEach { dept ->
                            val selected = selectedDepartment.equals(dept, ignoreCase = true)
                            FilterChip(
                                selected = selected,
                                onClick = { selectedDepartment = dept },
                                label = { Text(departmentLabels[dept] ?: dept, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Subject Folders filter chips row
                if (subjectFolders.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedFolderFilter == "All",
                                onClick = { selectedFolderFilter = "All" },
                                label = { Text(if (isArabic) "كل المجلدات" else "All Folders", fontSize = 11.sp) }
                            )
                            subjectFolders.forEach { folder ->
                                val fId = folder.folderId.ifBlank { folder.id }
                                FilterChip(
                                    selected = selectedFolderFilter == fId,
                                    onClick = { selectedFolderFilter = fId },
                                    label = { Text(folder.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                if (filteredPlatformMaterials.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isArabic) "لا توجد مذكرات مطابقة للبحث أو التصفية" else "No materials found",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic) "يمكنك إعادة ضبط خيارات التصفية أو رفع مذكرات جديدة للمنصة." else "Reset filters or upload new materials.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    materialsSearchQuery = ""
                                    selectedDepartment = "All"
                                    selectedFolderFilter = "All"
                                }) {
                                    Text(if (isArabic) "إعادة ضبط خيارات البحث" else "Reset Search & Filters")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredPlatformMaterials, key = { it.id }) { file ->
                        val matchingFolderName = subjectFolders.find { (it.folderId.ifBlank { it.id }) == file.parentFolderId }?.name ?: ""
                        PlatformMaterialCard(
                            file = file,
                            folderName = matchingFolderName,
                            isSuperAdmin = isSuperAdmin,
                            onPreview = { onPreviewMaterialClick?.invoke(file) },
                            onEdit = { fileToEdit = file },
                            onDelete = { fileToDelete = file }
                        )
                    }
                }
            }

            // ==========================================
            // TAB 2: Announcements Feed / Broadcast Notice Board
            // ==========================================
            if (selectedSubTab == 2) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "لوحة الإعلانات والتنبيهات (${announcements.size})" else "Announcements & Notices (${announcements.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showPostAnnouncementDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("post_announcement_button")
                        ) {
                            Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "نشر إعلان جديد" else "Post Notice", fontSize = 12.sp)
                        }
                    }
                }

                if (announcements.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isArabic) "لا توجد إعلانات منشورة بعد" else "No announcements posted yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isArabic) "انشر مواعيد الامتحانات وجداول الاختبارات والتحديثات الأكاديمية للطلاب." else "Post exam schedules, course updates, and notices for all students.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(announcements, key = { it.id }) { item ->
                        AdminAnnouncementItemCard(
                            announcement = item,
                            isArabic = isArabic,
                            onDelete = { announcementToDelete = item }
                        )
                    }
                }
            }

            // ==========================================
            // TAB 3: Manage Admins (Super Admin Only)
            // ==========================================
            if (selectedSubTab == 3 && isSuperAdmin) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "المالك والمسؤول العام: المهندس أحمد عثمان",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "البريد الإلكتروني: $SUPER_ADMIN_EMAIL",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المشرفين المعتمدين (${admins.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onAddAdminClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("open_add_admin_dialog")
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة مشرف جديد", fontSize = 12.sp)
                        }
                    }
                }

                if (admins.isEmpty()) {
                    item {
                        Text(
                            text = "لم تقم بتعيين مشرفين إضافيين بعد. يمكنك إضافة مساعدين بالبريد الإلكتروني.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(admins, key = { it.id }) { admin ->
                        AdminUserCard(
                            admin = admin,
                            onRemove = { adminToRemove = admin }
                        )
                    }
                }
            }
        }

        // Floating Action Buttons
        if (selectedSubTab == 1 && isSuperAdmin) {
            FloatingActionButton(
                onClick = onAddMaterialClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_material"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Upload Study Material")
            }
        } else if (selectedSubTab == 2) {
            FloatingActionButton(
                onClick = { showPostAnnouncementDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_post_announcement"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Campaign, contentDescription = "Post Announcement")
            }
        } else if (selectedSubTab == 3 && isSuperAdmin) {
            FloatingActionButton(
                onClick = onAddAdminClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_admin"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Admin")
            }
        }
    }

    // Dialog for Admin to Post a New Announcement
    if (showPostAnnouncementDialog) {
        PostAnnouncementDialog(
            isArabic = isArabic,
            onDismiss = { showPostAnnouncementDialog = false },
            onPost = { title, content, type, isPinned ->
                onPostAnnouncement(title, content, type, isPinned)
                showPostAnnouncementDialog = false
            }
        )
    }

    if (showCreateFolderDialog) {
        SubjectFolderEditorDialog(
            initialFolder = null,
            onDismiss = { showCreateFolderDialog = false },
            onSave = { name, subject, department, grade, description, driveUrl ->
                onCreateSubjectFolder(name, subject, department, grade, description, driveUrl)
                showCreateFolderDialog = false
            }
        )
    }

    folderToEdit?.let { target ->
        SubjectFolderEditorDialog(
            initialFolder = target,
            onDismiss = { folderToEdit = null },
            onSave = { name, subject, department, grade, description, driveUrl ->
                onUpdateSubjectFolder(
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
                onDeleteSubjectFolder(fId)
                folderToDelete = null
            }
        )
    }

    fileToEdit?.let { target ->
        EditMaterialDialog(
            file = target,
            subjectFolders = subjectFolders,
            onDismiss = { fileToEdit = null },
            onSave = { fileId, name, category, department, grade, folderId, description ->
                onEditMaterialClick?.invoke(fileId, name, category, department, grade, folderId, description)
                fileToEdit = null
            }
        )
    }

    fileToDelete?.let { target ->
        SweetAlertConfirmDialog(
            type = SweetAlertType.DELETE,
            title = if (isArabic) "حذف المذكرة الدراسية نهائياً" else "Delete Study Material?",
            targetItemName = target.name,
            targetCategory = "${target.category} • ${target.department}",
            message = if (isArabic)
                "هل أنت متأكد من حذف هذه المذكرة نهائياً من المنصة؟ سيتم إزالتها وإلغاء إتاحتها لجميع الطلاب."
            else
                "Are you sure you want to delete this study material permanently? It will be removed from the platform for all students.",
            confirmButtonText = if (isArabic) "نعم، حذف نهائي" else "Yes, Delete Material",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_delete_platform_material_button",
            onConfirm = {
                onDeleteMaterialClick(target.id)
                fileToDelete = null
            },
            onDismiss = { fileToDelete = null }
        )
    }

    materialToDelete?.let { target ->
        SweetAlertConfirmDialog(
            type = SweetAlertType.DELETE,
            title = if (isArabic) "حذف المذكرة الدراسية" else "Delete Study Material?",
            targetItemName = target.name,
            targetCategory = "${target.category} • ${target.grade}",
            message = if (isArabic)
                "هل أنت متأكد من حذف هذه المادة الدراسية؟ سيتم إزالتها نهائياً ولن يتمكن الطلاب من تنزيلها بعد الآن."
            else
                "Are you sure you want to delete this study material? It will be permanently removed and students will no longer be able to download it.",
            confirmButtonText = if (isArabic) "نعم، حذف المادة" else "Yes, Delete Material",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_delete_material_button",
            onConfirm = {
                onDeleteMaterialClick(target.fileId)
                materialToDelete = null
            },
            onDismiss = { materialToDelete = null }
        )
    }

    studentToReject?.let { target ->
        SweetAlertConfirmDialog(
            type = SweetAlertType.STUDENT,
            title = if (isArabic) "تأكيد رفض / إلغاء حساب الطالب" else "Reject / Revoke Student?",
            targetItemName = target.displayName,
            targetCategory = target.email,
            message = if (isArabic)
                "هل أنت متأكد من رفض أو إلغاء تفعيل هذا الطالب؟ سيتم تعليق وصوله لجميع مجلدات ومذكرات المنصة."
            else
                "Are you sure you want to reject or revoke access for this student? Their access to all platform materials will be suspended.",
            confirmButtonText = if (isArabic) "نعم، إلغاء الحساب" else "Yes, Revoke Access",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_reject_student_button",
            onConfirm = {
                onRejectStudent(target.userId)
                studentToReject = null
            },
            onDismiss = { studentToReject = null }
        )
    }

    if (showBulkRejectAlert) {
        SweetAlertConfirmDialog(
            type = SweetAlertType.STUDENT,
            title = if (isArabic) "رفض الطلاب المحددين دفعة واحدة" else "Bulk Reject Students?",
            targetItemName = if (isArabic) "عدد الطلاب المحددين: ${selectedStudentIds.size}" else "Selected: ${selectedStudentIds.size} students",
            message = if (isArabic)
                "هل أنت متأكد من رفض جميع طلبات التسجيل للطلاب المحددين؟"
            else
                "Are you sure you want to reject all selected student registration requests?",
            confirmButtonText = if (isArabic) "نعم، رفض المحددين" else "Yes, Reject Selected",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_bulk_reject_button",
            onConfirm = {
                onBulkRejectStudents(selectedStudentIds.toList())
                selectedStudentIds = emptySet()
                showBulkRejectAlert = false
            },
            onDismiss = { showBulkRejectAlert = false }
        )
    }

    announcementToDelete?.let { target ->
        SweetAlertConfirmDialog(
            type = SweetAlertType.DELETE,
            title = if (isArabic) "حذف الإعلان الأكاديمي" else "Delete Announcement?",
            targetItemName = target.title,
            message = if (isArabic)
                "هل أنت متأكد من حذف هذا الإعلان؟ سيتم حذفه من شاشات جميع الطلاب."
            else
                "Are you sure you want to delete this announcement? It will be removed from all student screens.",
            confirmButtonText = if (isArabic) "نعم، حذف الإعلان" else "Yes, Delete Announcement",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_delete_announcement_button",
            onConfirm = {
                onDeleteAnnouncement(target.id)
                announcementToDelete = null
            },
            onDismiss = { announcementToDelete = null }
        )
    }

    adminToRemove?.let { target ->
        SweetAlertConfirmDialog(
            type = SweetAlertType.WARNING,
            title = if (isArabic) "إلغاء صلاحية المشرف" else "Remove Administrator?",
            targetItemName = target.email,
            message = if (isArabic)
                "هل أنت متأكد من إزالة هذا المشرف؟ سيفقد صلاحيات لوحة التحكم فوراً."
            else
                "Are you sure you want to remove this administrator? They will immediately lose access to the admin dashboard.",
            confirmButtonText = if (isArabic) "نعم، إزالة المشرف" else "Yes, Remove Admin",
            cancelButtonText = if (isArabic) "تراجع وإلغاء" else "Cancel",
            confirmTag = "confirm_remove_admin_button",
            onConfirm = {
                onRemoveAdminClick(target.id)
                adminToRemove = null
            },
            onDismiss = { adminToRemove = null }
        )
    }

    studentToApproveWithFolders?.let { student ->
        AssignStudentFoldersDialog(
            student = student,
            allFolders = subjectFolders,
            isApprovalMode = true,
            onDismiss = { studentToApproveWithFolders = null },
            onConfirm = { folderIds ->
                onApproveStudentWithFolders(student.userId, folderIds)
                studentToApproveWithFolders = null
            }
        )
    }

    studentToAssignFolders?.let { student ->
        AssignStudentFoldersDialog(
            student = student,
            allFolders = subjectFolders,
            isApprovalMode = false,
            onDismiss = { studentToAssignFolders = null },
            onConfirm = { folderIds ->
                onAssignStudentFolders(student.userId, folderIds)
                studentToAssignFolders = null
            }
        )
    }
}

@Composable
fun PendingStudentCardWithCheckbox(
    student: UserProfile,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onApprove: () -> Unit,
    onApproveWithFolders: () -> Unit = {},
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = com.example.ui.locale.LocalAppStrings.current
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onToggleSelect() }
            .testTag("pending_student_${student.userId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                modifier = Modifier.testTag("checkbox_student_${student.userId}")
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.displayName.ifBlank { strings.newStudent },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = student.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(32.dp).testTag("reject_student_${student.userId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onApproveWithFolders,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("approve_student_${student.userId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "قبول وتحديد المجلدات" else "Approve & Select Folders",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAnnouncementItemCard(
    announcement: StudyAnnouncement,
    isArabic: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (announcement.type) {
        "exam" -> Color(0xFFD32F2F)
        "course" -> Color(0xFF0288D1)
        "workshop" -> Color(0xFF7B1FA2)
        else -> MaterialTheme.colorScheme.primary
    }

    val typeLabel = when (announcement.type) {
        "exam" -> if (isArabic) "امتحان" else "Exam"
        "course" -> if (isArabic) "مساق" else "Course"
        "workshop" -> if (isArabic) "ورشة عمل" else "Workshop"
        else -> if (isArabic) "تنبيه عام" else "Notice"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("admin_announcement_${announcement.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = typeLabel,
                            color = badgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (announcement.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_announcement_${announcement.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Announcement",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = announcement.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = announcement.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PostAnnouncementDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onPost: (title: String, content: String, type: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("general") }
    var isPinned by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isArabic) "نشر إعلان جديد للطلاب" else "Broadcast New Announcement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isArabic) "عنوان الإعلان" else "Announcement Title") },
                    placeholder = { Text(if (isArabic) "مثال: جدول اختبارات منتصف الفصل" else "e.g. Midterm Exams Schedule") },
                    modifier = Modifier.fillMaxWidth().testTag("announcement_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (isArabic) "تفاصيل الإعلان" else "Announcement Details") },
                    placeholder = { Text(if (isArabic) "اكتب نص الإعلان والتعليمات للطلاب..." else "Write notice details...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("announcement_content_input"),
                    maxLines = 4
                )

                // Type selector
                Text(
                    text = if (isArabic) "نوع الإعلان:" else "Notice Type:",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("general" to if (isArabic) "عام" else "General",
                           "exam" to if (isArabic) "امتحان" else "Exam",
                           "course" to if (isArabic) "مساق" else "Course",
                           "workshop" to if (isArabic) "ورشة" else "Workshop").forEach { (typeKey, typeLabel) ->
                        val selected = selectedType == typeKey
                        FilterChip(
                            selected = selected,
                            onClick = { selectedType = typeKey },
                            label = { Text(typeLabel, fontSize = 11.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Text(
                        text = if (isArabic) "تثبيت في أعلى الشاشة الرئيسية" else "Pin to top of home screen",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onPost(title.trim(), content.trim(), selectedType, isPinned)
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        modifier = Modifier.testTag("submit_announcement_button")
                    ) {
                        Text(if (isArabic) "نشر الإعلان" else "Publish")
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovedStudentCard(
    student: UserProfile,
    subjectFolders: List<SubjectFolder> = emptyList(),
    onAssignFolders: () -> Unit = {},
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = com.example.ui.locale.LocalAppStrings.current
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("approved_student_card_${student.userId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.displayName.ifBlank { strings.approvedStudent },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onAssignFolders,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("manage_student_folders_${student.userId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = if (isArabic) "تخصيص المجلدات" else "Assign Folders",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                TextButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.revokeStudent, fontSize = 11.sp)
                }
            }

            // Folder Access Badge
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (student.allowedFolderIds.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = if (isArabic) "🌐 وصول عام (كافة المواد)" else "🌐 Full Access (All Subjects)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    val assignedFolderNames = subjectFolders
                        .filter { (it.folderId.ifBlank { it.id }) in student.allowedFolderIds }
                        .map { it.name }
                    val label = if (assignedFolderNames.isNotEmpty()) {
                        assignedFolderNames.take(2).joinToString(", ") + if (assignedFolderNames.size > 2) " (+${assignedFolderNames.size - 2})" else ""
                    } else {
                        "${student.allowedFolderIds.size} ${if (isArabic) "مجلدات مخصصة" else "folders"}"
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                    ) {
                        Text(
                            text = if (isArabic) "🔒 مجلدات مصرح بها: $label" else "🔒 Allowed: $label",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignStudentFoldersDialog(
    student: UserProfile,
    allFolders: List<SubjectFolder>,
    isApprovalMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
    val candidateFolders = remember(allFolders) {
        allFolders.filter { it.folderId != "study_materials_root" }
    }
    var allowAllFolders by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(student.allowedFolderIds.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_assign_folders"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isApprovalMode) {
                                if (isArabic) "الموافقة وتخصيص المجلدات" else "Approve & Assign Folders"
                            } else {
                                if (isArabic) "إدارة المجلدات المسموحة للطالب" else "Manage Allowed Folders"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option: Allow all folders
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            allowAllFolders = !allowAllFolders
                            if (allowAllFolders) {
                                selectedIds = emptySet()
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (allowAllFolders) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowAllFolders,
                            onCheckedChange = {
                                allowAllFolders = it
                                if (it) selectedIds = emptySet()
                            },
                            modifier = Modifier.testTag("toggle_allow_all_folders")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isArabic) "السماح بالوصول لجميع المجلدات (بدون قيود)" else "Allow Access to All Folders (Full Access)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isArabic) "يستطيع الطالب تصفح وتحميل ملفات كافة المواد الدراسية" else "Student can view and download all subject materials",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!allowAllFolders) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "اختر المجلدات المصرح بها للطالب:" else "Select Allowed Folders:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            TextButton(
                                onClick = {
                                    val allIds = candidateFolders.map { it.folderId.ifBlank { it.id } }.toSet()
                                    selectedIds = allIds
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(if (isArabic) "تحديد الكل" else "Select All", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = { selectedIds = emptySet() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(if (isArabic) "إلغاء الكل" else "Clear", fontSize = 11.sp)
                            }
                        }
                    }

                    if (candidateFolders.isEmpty()) {
                        Text(
                            text = if (isArabic) "لا توجد مجلدات مواد دراسية منشأة حالياً" else "No subject folders created yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            candidateFolders.forEach { folder ->
                                val fId = folder.folderId.ifBlank { folder.id }
                                val isChecked = selectedIds.contains(fId)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedIds = if (isChecked) selectedIds - fId else selectedIds + fId
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                selectedIds = if (it) selectedIds + fId else selectedIds - fId
                                            },
                                            modifier = Modifier.testTag("checkbox_folder_$fId")
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (folder.subject.isNotBlank() || folder.department.isNotBlank()) {
                                                Text(
                                                    text = listOf(folder.subject, folder.department).filter { it.isNotBlank() }.joinToString(" • "),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!allowAllFolders && selectedIds.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "تنبيه: لم تختر أي مجلد للطالب بعد" else "Note: No folders selected yet for this student",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalIds = if (allowAllFolders) emptyList() else selectedIds.toList()
                            onConfirm(finalIds)
                        },
                        enabled = allowAllFolders || selectedIds.isNotEmpty(),
                        modifier = Modifier.testTag("confirm_assign_folders_button")
                    ) {
                        Text(
                            text = if (isApprovalMode) {
                                if (isArabic) "موافقة وحفظ المجلدات" else "Approve & Save Folders"
                            } else {
                                if (isArabic) "حفظ التغييرات" else "Save Changes"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    admin: AdminUser,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = admin.email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مشرف معتمد • تمت إضافته بواسطة المالك",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag("remove_admin_${admin.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Admin",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AdminMaterialItemCard(
    material: AdminMaterial,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileTypeIcon(file = material.toDriveFile())
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(category = material.category)
                    if (material.department.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = material.department,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = material.grade,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (material.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = material.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_material_${material.fileId}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Material",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PlatformMaterialCard(
    file: DriveFile,
    folderName: String = "",
    isSuperAdmin: Boolean,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("platform_material_${file.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FileTypeIcon(file = file)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        CategoryChip(category = file.category)
                        if (file.department.isNotBlank() && file.department != "All") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = file.department,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (folderName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = folderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (file.formattedSize != "--") {
                            Text(
                                text = file.formattedSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (file.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = file.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preview button
                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp).testTag("preview_material_${file.id}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "معاينة" else "Preview", fontSize = 11.sp)
                }

                if (isSuperAdmin) {
                    Spacer(modifier = Modifier.width(6.dp))
                    // Edit button
                    FilledTonalButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("edit_material_${file.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "تعديل" else "Edit", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    // Delete button with sweet alert
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("delete_material_${file.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onError)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "حذف" else "Delete", fontSize = 11.sp, color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}

private fun formatStorageSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(java.util.Locale.US, "%.1f MB", mb)
    } else {
        String.format(java.util.Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
    }
}
