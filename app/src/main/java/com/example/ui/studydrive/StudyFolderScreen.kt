package com.example.ui.studydrive

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.data.model.DriveFile
import com.example.data.model.StudyAnnouncement
import com.example.data.model.StudyBookmark
import com.example.data.model.SubjectFolder
import com.example.ui.components.CategoryChip
import com.example.ui.components.FileTypeIcon
import com.example.ui.locale.LocalAppStrings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.text.style.TextAlign

val QUICK_FILE_TYPES = listOf("All", "PDF", "Slides", "Past Exams", "Summaries", "Docs")
val SEMESTERS = listOf("All", "Fall", "Spring", "Summer")
val ACADEMIC_YEARS = listOf("All", "2025-2026", "2024-2025", "2023-2024")
val SUBJECT_CATEGORIES = listOf("All", "Python", "Algorithms", "Past Exams", "Web Development", "General")
val DEPARTMENTS = listOf(
    "All",
    "Computer Science",
    "Engineering",
    "Business",
    "Medicine",
    "General Secondary"
)
val COURSES = listOf(
    "All",
    "مقدمة بايثون",
    "الخوارزميات",
    "امتحانات البكالوريا",
    "تطوير الويب",
    "قواعد البيانات",
    "الذكاء الاصطناعي"
)

@Composable
fun StudyFolderScreen(
    state: DriveScreenState,
    bookmarks: List<StudyBookmark>,
    subjectFolders: List<SubjectFolder> = emptyList(),
    allowedFolderIds: List<String> = emptyList(),
    isSuperAdmin: Boolean = false,
    announcements: List<StudyAnnouncement> = emptyList(),
    showApprovalBanner: Boolean = false,
    onDismissApprovalBanner: () -> Unit = {},
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onFileTypeSelect: (String) -> Unit,
    onSemesterSelect: (String) -> Unit,
    onAcademicYearSelect: (String) -> Unit,
    onCourseSelect: (String) -> Unit,
    onDepartmentSelect: (String) -> Unit = {},
    onClearFilters: () -> Unit,
    onDownloadClick: (DriveFile) -> Unit,
    onToggleBookmark: (DriveFile) -> Unit,
    onFileClick: (DriveFile) -> Unit,
    onPreviewClick: (DriveFile) -> Unit,
    onOpenFolderSelect: () -> Unit,
    onFolderSelected: (folderId: String, folderName: String) -> Unit = { _, _ -> },
    onCreateSubjectFolder: (name: String, subject: String, department: String, grade: String, description: String, driveFolderUrl: String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateSubjectFolder: (SubjectFolder) -> Unit = {},
    onDeleteSubjectFolder: (folderId: String) -> Unit = {},
    onAddMaterialToFolder: ((folderId: String) -> Unit)? = null,
    onEditMaterial: ((fileId: String, name: String, category: String, department: String, grade: String, folderId: String, description: String) -> Unit)? = null,
    onDeleteMaterial: ((fileId: String) -> Unit)? = null,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = com.example.ui.locale.LocalAppStrings.current
    val typeScroll = rememberScrollState()
    val filterScroll = rememberScrollState()
    var showPosterModal by remember { mutableStateOf(false) }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<SubjectFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<SubjectFolder?>(null) }
    var fileToEdit by remember { mutableStateOf<DriveFile?>(null) }
    var fileToDelete by remember { mutableStateOf<DriveFile?>(null) }

    val fileTypeLabels = mapOf(
        "All" to strings.filterAll,
        "PDF" to strings.typePdf,
        "Slides" to strings.typeSlides,
        "Past Exams" to strings.typePastExams,
        "Summaries" to strings.typeSummaries,
        "Docs" to strings.typeDocs
    )

    val semesterLabels = mapOf(
        "All" to strings.filterAll,
        "Fall" to strings.semesterFall,
        "Spring" to strings.semesterSpring,
        "Summer" to strings.semesterSummer
    )

    val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC

    val departmentLabels = mapOf(
        "All" to if (isArabic) "جميع الأقسام" else "All Majors",
        "Computer Science" to if (isArabic) "علوم الحاسب والبرمجة" else "Computer Science",
        "Engineering" to if (isArabic) "الهندسة والتقنية" else "Engineering",
        "Business" to if (isArabic) "إدارة الأعمال" else "Business",
        "Medicine" to if (isArabic) "الطب والعلوم الصحية" else "Medicine",
        "General Secondary" to if (isArabic) "الثانوية والبكالوريا" else "Secondary School"
    )

    val isStudent = !isSuperAdmin

    val effectiveSubjectFolders = remember(subjectFolders, allowedFolderIds, isStudent) {
        if (isStudent) {
            subjectFolders.filter { folder ->
                val fId = folder.folderId.ifBlank { folder.id }
                fId in allowedFolderIds && fId != "study_materials_root"
            }
        } else {
            subjectFolders
        }
    }

    val nonRootFolders = remember(effectiveSubjectFolders) {
        effectiveSubjectFolders.filter { it.folderId != "study_materials_root" }
    }

    val inSubfolder = if (isStudent) {
        state.activeFolderId in allowedFolderIds
    } else {
        state.activeFolderId != "study_materials_root"
    }

    val filteredFiles by remember(
        state.files,
        state.activeFolderId,
        state.searchQuery,
        state.selectedCategory,
        state.selectedFileType,
        state.selectedSemester,
        state.selectedAcademicYear,
        state.selectedCourse,
        state.selectedDepartment,
        allowedFolderIds,
        isStudent,
        inSubfolder
    ) {
        derivedStateOf {
            if (isStudent && allowedFolderIds.isEmpty()) {
                return@derivedStateOf emptyList<DriveFile>()
            }

            state.files.filter { file ->
                val matchesFolder = if (isStudent) {
                    if (inSubfolder) {
                        file.parentFolderId == state.activeFolderId && state.activeFolderId in allowedFolderIds
                    } else {
                        file.parentFolderId in allowedFolderIds
                    }
                } else {
                    if (state.activeFolderId == "study_materials_root") {
                        true
                    } else {
                        file.parentFolderId == state.activeFolderId
                    }
                }

                if (!matchesFolder) return@filter false

                val query = state.searchQuery.trim()
                val matchesSearch = query.isBlank() ||
                        file.name.contains(query, ignoreCase = true) ||
                        file.courseName.contains(query, ignoreCase = true) ||
                        file.professor.contains(query, ignoreCase = true) ||
                        file.category.contains(query, ignoreCase = true) ||
                        file.subject.contains(query, ignoreCase = true) ||
                        file.description.contains(query, ignoreCase = true) ||
                        file.semester.contains(query, ignoreCase = true) ||
                        file.academicYear.contains(query, ignoreCase = true) ||
                        file.department.contains(query, ignoreCase = true)

                val matchesCategory = state.selectedCategory == "All" ||
                        file.category.equals(state.selectedCategory, ignoreCase = true) ||
                        file.subject.equals(state.selectedCategory, ignoreCase = true)

                val matchesType = when (state.selectedFileType) {
                    "PDF" -> file.isPdf
                    "Slides" -> file.isPresentation || file.materialType == "Slides"
                    "Past Exams" -> file.materialType == "Past Exams" || file.category == "Past Exams" || file.name.contains("امتحان")
                    "Summaries" -> file.materialType == "Summaries" || file.name.contains("ملخص") || file.isTextOrCode
                    "Docs" -> file.isDocument
                    else -> true
                }

                val matchesSemester = state.selectedSemester == "All" ||
                        file.semester.equals(state.selectedSemester, ignoreCase = true)

                val matchesYear = state.selectedAcademicYear == "All" ||
                        file.academicYear.equals(state.selectedAcademicYear, ignoreCase = true)

                val matchesCourse = state.selectedCourse == "All" ||
                        file.courseName.contains(state.selectedCourse, ignoreCase = true)

                val matchesDepartment = state.selectedDepartment == "All" ||
                        file.department.equals(state.selectedDepartment, ignoreCase = true)

                matchesSearch && matchesCategory && matchesType && matchesSemester && matchesYear && matchesCourse && matchesDepartment
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Interactive Breadcrumbs Path Navigation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("breadcrumbs_bar"),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Scrollable Breadcrumbs Path Trail
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val inRoot = if (isStudent) !inSubfolder else state.activeFolderId == "study_materials_root"

                    // Crumb 1: Home / Root Folder or My Assigned Folders
                    Surface(
                        onClick = {
                            if (isStudent) {
                                onFolderSelected("my_assigned_folders", if (isArabic) "مجلداتي الدراسية المعتمدة" else "My Assigned Folders")
                            } else {
                                onFolderSelected("study_materials_root", if (isArabic) "جميع المواد والمجلدات (الرئيسي)" else "All Study Materials (Root)")
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (inRoot) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier.testTag("breadcrumb_root")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isStudent) Icons.Default.FolderShared else Icons.Default.Home,
                                contentDescription = if (isStudent) "My Assigned Folders" else "Root Folder",
                                tint = if (inRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isStudent) {
                                    if (isArabic) "مجلداتي المعتمدة" else "My Folders"
                                } else {
                                    if (isArabic) "المجلد الرئيسي" else "Root Folder"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (inRoot) FontWeight.Bold else FontWeight.Medium,
                                color = if (inRoot) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Arrow Separator & Crumb 2 (Active Subfolder)
                    if (inSubfolder) {
                        Icon(
                            imageVector = if (isArabic) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 3.dp).size(16.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("breadcrumb_active_folder")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = state.activeFolderName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Quick Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Files",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = onOpenFolderSelect,
                        modifier = Modifier.size(32.dp).testTag("switch_folder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Change Folder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("study_files_list"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Subfolder Active Indicator Banner (when inside a specific folder)
            if (inSubfolder) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("active_subfolder_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = state.activeFolderName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isArabic) "مجلد دراسي • ${filteredFiles.size} ملفات" else "Folder contents • ${filteredFiles.size} files",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSuperAdmin && onAddMaterialToFolder != null) {
                                    FilledTonalButton(
                                        onClick = { onAddMaterialToFolder(state.activeFolderId) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(30.dp).testTag("folder_banner_add_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isArabic) "+ رفع مذكرة" else "+ Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (isStudent) {
                                            onFolderSelected("my_assigned_folders", if (isArabic) "مجلداتي الدراسية المعتمدة" else "My Assigned Folders")
                                        } else {
                                            onFolderSelected("study_materials_root", if (isArabic) "جميع المواد والمجلدات (الرئيسي)" else "All Study Materials (Root)")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp).testTag("folder_banner_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isStudent) (if (isArabic) "مجلداتي" else "My Folders") else (if (isArabic) "الرئيسي" else "Root"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Assigned Student Folders Notice Banner
            if (isStudent && allowedFolderIds.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("restricted_folders_banner"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic)
                                    "📁 تم تخصيص ${effectiveSubjectFolders.size} مجلدات دراسية لحسابك بواسطة الإدارة"
                                else
                                    "📁 ${effectiveSubjectFolders.size} study folders assigned to your account by admin",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Student Approval Status Notification Banner
            if (showApprovalBanner) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("student_approval_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "تم اعتماد وتفعيل حسابك بنجاح!" else "Account Approved & Active!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = if (isArabic)
                                        "مرحباً بك! اعتمد المهندس أحمد عثمان حسابك الدراسي. يمكنك الآن تصفح وتحميل ومطالعة كافة المذكرات مباشرة."
                                    else
                                        "Welcome! Your student account has been approved and activated. You can now access and download all course materials.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            IconButton(
                                onClick = onDismissApprovalBanner,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Screen Photo Hero Card (Gemini generated banner)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_banner),
                            contentDescription = "بوابة البرمجة للبكالوريا",
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
                                            Color(0xB3001833)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "بوابة البرمجة للبكالوريا",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "المهندس أحمد عثمان • دروس ومذكرات أونلاين",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE0F7FA)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        try {
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=201113736196&text=مرحبا%20أود%20الاستفسار%20عن%20دروس%20بوابة%20البرمجة")
                                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01113736196"))
                                            context.startActivity(dialIntent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("واتساب", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showPosterModal = true },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("البوستر", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // "What's New" / Announcements Feed Notice Board
            if (announcements.isNotEmpty()) {
                item {
                    AnnouncementsNoticeBoard(
                        announcements = announcements,
                        isArabic = isArabic
                    )
                }
            }

            // Global Search Bar with Filter Action Icon
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("بحث بالمقرر، الأستاذ، الامتحانات، بايثون...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_files_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        shape = CircleShape,
                        color = if (state.activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier.size(48.dp).testTag("toggle_filters_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Toggle Filters",
                                tint = if (state.activeFilterCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Department / College Categorization Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DEPARTMENTS.forEach { dept ->
                        val selected = state.selectedDepartment.equals(dept, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { onDepartmentSelect(dept) },
                            label = {
                                Text(
                                    text = departmentLabels[dept] ?: dept,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // Subject Folders Carousel (with Super Admin Create / Edit / Delete controls)
            if (subjectFolders.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "مجلدات المواد الدراسية (${subjectFolders.size})" else "Subject Folders (${subjectFolders.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isSuperAdmin) {
                                FilledTonalButton(
                                    onClick = { showCreateFolderDialog = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("home_create_subject_folder_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreateNewFolder,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "+ مجلد مادة جديد" else "+ New Subject Folder",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            subjectFolders.forEach { folder ->
                                val fId = folder.folderId.ifBlank { folder.id }
                                val isSelected = state.activeFolderId == fId
                                val isRoot = fId == "study_materials_root"

                                Card(
                                    modifier = Modifier
                                        .width(if (isSuperAdmin && !isRoot) 230.dp else 195.dp)
                                        .clickable { onFolderSelected(fId, folder.name) }
                                        .testTag("home_subject_folder_$fId"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        }
                                    ),
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else null
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = folder.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (isSuperAdmin && !isRoot) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(
                                                        onClick = { folderToEdit = folder },
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .testTag("home_edit_folder_$fId")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit Folder",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { folderToDelete = folder },
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .testTag("home_delete_folder_$fId")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete Folder",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = folder.subject,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                            if (folder.grade.isNotBlank()) {
                                                Text(
                                                    text = folder.grade,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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

            // Quick File Type Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(typeScroll)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QUICK_FILE_TYPES.forEach { type ->
                        val selected = state.selectedFileType == type
                        FilterChip(
                            selected = selected,
                            onClick = { onFileTypeSelect(type) },
                            label = { Text(fileTypeLabels[type] ?: type, fontSize = 12.sp) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Advanced Filters Panel (Semester, Year, Subject, Course)
            item {
                AnimatedVisibility(visible = showAdvancedFilters) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تصفية ذكية للمقررات والمواد",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (state.activeFilterCount > 0) {
                                    TextButton(onClick = onClearFilters) {
                                        Text(strings.clearFilters, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Semester Filter
                            Column {
                                Text(strings.filterBySemester, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SEMESTERS.forEach { sem ->
                                        val selected = state.selectedSemester == sem
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onSemesterSelect(sem) },
                                            label = { Text(semesterLabels[sem] ?: sem, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // Academic Year Filter
                            Column {
                                Text(strings.filterByYear, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ACADEMIC_YEARS.forEach { yr ->
                                        val selected = state.selectedAcademicYear == yr
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onAcademicYearSelect(yr) },
                                            label = { Text(yr, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // Subject Filter
                            Column {
                                Text("المادة والمجال البرمجي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SUBJECT_CATEGORIES.forEach { cat ->
                                        val selected = state.selectedCategory == cat
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onCategorySelect(cat) },
                                            label = { Text(cat, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // Course Filter
                            Column {
                                Text(strings.filterByCourse, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    COURSES.forEach { c ->
                                        val selected = state.selectedCourse == c
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onCourseSelect(c) },
                                            label = { Text(c, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Filters summary bar (when filters active)
            if (state.activeFilterCount > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strings.activeFiltersLabel} (${state.activeFilterCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = onClearFilters,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(strings.clearFilters, fontSize = 11.sp)
                        }
                    }
                }
            }

            // If in Overview/Root, display Student Empty-State if no folders assigned
            if (!inSubfolder && isStudent && allowedFolderIds.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("student_no_folders_empty_state"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderShared,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = if (isArabic) "بانتظار تخصيص المواد الدراسية" else "Awaiting Course Assignment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic)
                                    "تم تفعيل حسابك بنجاح ولكن لم يتم تعيين مجلدات دراسية لك بعد من قِبل المشرف العام. ستظهر مجلداتك المخصصة هنا بمجرد ربطها بحسابك."
                                else
                                    "Your account is active, but no study folders have been assigned to your account yet by the Super Admin. They will appear here once assigned.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // If in Overview/Root for students, display assigned Folders list
            if (!inSubfolder && isStudent && nonRootFolders.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isStudent) Icons.Default.FolderShared else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isStudent) {
                                        if (isArabic) "المجلدات المصرح بها لحسابك (${nonRootFolders.size})" else "Your Assigned Folders (${nonRootFolders.size})"
                                    } else {
                                        if (isArabic) "مجلدات المواد والمناهج (${nonRootFolders.size})" else "Course & Subject Folders (${nonRootFolders.size})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isSuperAdmin) {
                                TextButton(
                                    onClick = { showCreateFolderDialog = true },
                                    modifier = Modifier.testTag("create_folder_text_button")
                                ) {
                                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "+ مجلد جديد" else "+ New Folder", fontSize = 11.sp)
                                }
                            }
                        }

                        nonRootFolders.forEach { folder ->
                            val fId = folder.folderId.ifBlank { folder.id }
                            val fileCount = state.files.count { it.parentFolderId == fId }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onFolderSelected(fId, folder.name) }
                                    .testTag("folder_card_item_$fId"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = folder.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = folder.subject,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = if (isArabic) "$fileCount ملفات" else "$fileCount files",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSuperAdmin) {
                                            IconButton(
                                                onClick = { folderToEdit = folder },
                                                modifier = Modifier.size(28.dp).testTag("folder_card_edit_$fId")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Folder",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { folderToDelete = folder },
                                                modifier = Modifier.size(28.dp).testTag("folder_card_delete_$fId")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Folder",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isArabic) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Open Folder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Files Section Header if in root / overview
            if (!inSubfolder && (!isStudent || nonRootFolders.isNotEmpty())) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isStudent) {
                                if (isArabic) "ملفات ومذكرات مجلداتي (${filteredFiles.size})" else "Files in My Folders (${filteredFiles.size})"
                            } else {
                                if (isArabic) "جميع المذكرات والملفات المتاحة (${filteredFiles.size})" else "All Available Study Files (${filteredFiles.size})"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Files Section or Empty State
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري تحميل مذكرات البرمجة من Google Drive...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (filteredFiles.isEmpty()) {
                if (inSubfolder) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .testTag("empty_folder_state"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                                Text(
                                    text = if (isArabic) "هذا المجلد فارغ حالياً" else "This folder is currently empty",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isArabic)
                                        "لم يتم إضافة أو رفع أي مذكرات دراسية داخل مجلد \"${state.activeFolderName}\" حتى الآن."
                                    else
                                        "No study materials have been uploaded into \"${state.activeFolderName}\" yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (isStudent) {
                                                onFolderSelected("my_assigned_folders", if (isArabic) "مجلداتي الدراسية المعتمدة" else "My Assigned Folders")
                                            } else {
                                                onFolderSelected("study_materials_root", if (isArabic) "جميع المواد والمجلدات (الرئيسي)" else "All Study Materials (Root)")
                                            }
                                        },
                                        modifier = Modifier.testTag("empty_folder_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isStudent) (if (isArabic) "العودة لمجلداتي" else "Back to My Folders") else (if (isArabic) "العودة للمجلد الرئيسي" else "Back to Root"))
                                    }

                                    if (isSuperAdmin && onAddMaterialToFolder != null) {
                                        Button(
                                            onClick = {
                                                onAddMaterialToFolder(state.activeFolderId)
                                            },
                                            modifier = Modifier.testTag("empty_folder_add_material_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isArabic) "رفع مذكرة للمجلد" else "Upload Material")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = strings.noMaterialsFound,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = strings.noMaterialsDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = onClearFilters) {
                                    Text(strings.clearFilters)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredFiles, key = { it.id }) { file ->
                    val isBookmarked = bookmarks.any { it.fileId == file.id }
                    val progress = state.downloadProgress[file.id]

                    StudyFileCard(
                        file = file,
                        isBookmarked = isBookmarked,
                        downloadProgress = progress,
                        onClick = { onFileClick(file) },
                        onPreviewClick = { onPreviewClick(file) },
                        onDownloadClick = { onDownloadClick(file) },
                        onToggleBookmark = { onToggleBookmark(file) },
                        isSuperAdmin = isSuperAdmin,
                        onEditClick = { fileToEdit = file },
                        onDeleteClick = { fileToDelete = file },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Modal to view Eng. Ahmed Othman Poster
    if (showPosterModal) {
        Dialog(onDismissRequest = { showPosterModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.teacher_poster),
                        contentDescription = "المهندس أحمد عثمان - بوابة البرمجة للبكالوريا",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showPosterModal = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
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
                onEditMaterial?.invoke(fileId, name, category, department, grade, folderId, description)
                fileToEdit = null
            }
        )
    }

    fileToDelete?.let { target: DriveFile ->
        val isArabic = com.example.ui.locale.LocalAppLanguage.current == com.example.ui.locale.AppLanguage.ARABIC
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            modifier = Modifier.testTag("delete_material_confirm_dialog"),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isArabic) "حذف المذكرة الدراسية نهائياً؟" else "Delete Study Material?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = target.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic)
                            "هل أنت متأكد من حذف هذه المذكرة؟ سيتم حذفها نهائياً من التطبيق وإلغاء إتاحتها للطلاب."
                        else
                            "Are you sure you want to delete this study material permanently? It will be removed from all student screens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMaterial?.invoke(target.id)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_material_button")
                ) {
                    Text(if (isArabic) "نعم، حذف نهائي" else "Yes, Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { fileToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isArabic) "تراجع وإلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun StudyFileCard(
    file: DriveFile,
    isBookmarked: Boolean,
    downloadProgress: Float?,
    onClick: () -> Unit,
    onPreviewClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    isSuperAdmin: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPreviewClick() }
            .testTag("file_item_${file.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FileTypeIcon(file = file, containerSize = 44, iconSize = 24)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryChip(category = file.category)
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (file.courseName.isNotBlank()) {
                            Text(
                                text = "• ${file.courseName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Department & Download Count Info Row
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (file.department.isNotBlank()) {
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
                        if (file.downloadCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${file.downloadCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (file.isDownloaded) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = strings.offlineReady,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSuperAdmin) {
                        if (onEditClick != null) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(34.dp).testTag("edit_file_${file.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = strings.editMaterialAction,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (onDeleteClick != null) {
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(34.dp).testTag("delete_file_${file.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = strings.deleteMaterialAction,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Dedicated In-App Preview Button
                    IconButton(
                        onClick = onPreviewClick,
                        modifier = Modifier.size(34.dp).testTag("preview_button_${file.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = strings.previewInApp,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (downloadProgress != null) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else if (!file.isDownloaded) {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(34.dp).testTag("quick_download_${file.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = strings.downloadAction,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(34.dp).testTag("quick_bookmark_${file.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) strings.bookmarked else strings.bookmarkAction,
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (downloadProgress != null) {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }
        }
    }
}

@Composable
fun AnnouncementsNoticeBoard(
    announcements: List<StudyAnnouncement>,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("announcements_notice_board"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "لوحة الإعلانات والتحديثات" else "Notice Board & Announcements",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${announcements.size} ${if (isArabic) "إعلانات" else "Notices"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                announcements.forEach { item ->
                    AnnouncementCard(announcement = item, isArabic = isArabic)
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: StudyAnnouncement,
    isArabic: Boolean
) {
    val badgeColor = when (announcement.type) {
        "exam" -> Color(0xFFD32F2F)
        "course" -> Color(0xFF0288D1)
        "workshop" -> Color(0xFF7B1FA2)
        else -> MaterialTheme.colorScheme.primary
    }

    val typeLabel = when (announcement.type) {
        "exam" -> if (isArabic) "جدول امتحانات" else "Exam"
        "course" -> if (isArabic) "تحديث مساق" else "Course"
        "workshop" -> if (isArabic) "ورشة عمل" else "Workshop"
        else -> if (isArabic) "تنبيه عام" else "Notice"
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .testTag("announcement_item_${announcement.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isArabic) "مثبت" else "Pinned",
                            fontSize = 10.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
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
