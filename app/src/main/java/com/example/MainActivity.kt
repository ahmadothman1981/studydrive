package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.DriveFile
import com.example.data.repository.DriveRepository
import com.example.data.repository.StudyRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.authStateFlow
import com.example.ui.studydrive.AdminAddMaterialDialog
import com.example.ui.studydrive.AdminDashboardScreen
import com.example.ui.studydrive.BookmarksScreen
import com.example.ui.studydrive.DownloadsScreen
import com.example.ui.studydrive.DriveViewModel
import com.example.ui.studydrive.FileDetailDialog
import com.example.ui.studydrive.FolderSelectDialog
import com.example.ui.studydrive.StudyFolderScreen
import com.example.ui.studydrive.UserProfileDialog
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import com.example.data.model.SUPER_ADMIN_EMAIL
import com.example.ui.auth.PendingApprovalScreen
import com.example.ui.studydrive.AddAdminDialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var currentLanguage by remember { mutableStateOf(com.example.ui.locale.LanguageManager.getSavedLanguage(context)) }
            var currentThemeMode by remember { mutableStateOf(com.example.ui.theme.ThemeManager.getSavedThemeMode(context)) }
            val strings = if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) com.example.ui.locale.ArabicStrings else com.example.ui.locale.EnglishStrings
            val layoutDirection = if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr

            androidx.compose.runtime.CompositionLocalProvider(
                com.example.ui.locale.LocalAppLanguage provides currentLanguage,
                com.example.ui.locale.LocalAppStrings provides strings,
                androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection
            ) {
                MyApplicationTheme(themeMode = currentThemeMode) {
                    AppNavigation(
                        currentLanguage = currentLanguage,
                        currentThemeMode = currentThemeMode,
                        onThemeChanged = { mode ->
                            com.example.ui.theme.ThemeManager.saveThemeMode(context, mode)
                            currentThemeMode = mode
                        },
                        onToggleLanguage = {
                            val nextLang = if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) com.example.ui.locale.AppLanguage.ENGLISH else com.example.ui.locale.AppLanguage.ARABIC
                            com.example.ui.locale.LanguageManager.saveLanguage(context, nextLang)
                            currentLanguage = nextLang
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    currentLanguage: com.example.ui.locale.AppLanguage,
    currentThemeMode: com.example.ui.theme.AppThemeMode = com.example.ui.theme.AppThemeMode.SYSTEM,
    onThemeChanged: (com.example.ui.theme.AppThemeMode) -> Unit = {},
    onToggleLanguage: () -> Unit,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val context = LocalContext.current
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    if (currentUser == null) {
        val authViewModel: AuthViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    val app = (this[APPLICATION_KEY] as? Application) ?: (context.applicationContext as Application)
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    val repo = StudyRepository(db)
                    AuthViewModel(repo)
                }
            }
        )
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()

        AuthScreen(
            uiState = authState,
            onSignInClick = { authViewModel.signInWithGoogle(context) }
        )
    } else {
        val user = currentUser!!
        val isSuperAdmin = user.email.orEmpty().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
        val app = (context.applicationContext as Application)
        val databaseId = app.getString(R.string.firestore_database_id)
        val studyRepo = remember(databaseId) {
            val db = FirebaseFirestore.getInstance(databaseId)
            StudyRepository(db)
        }
        val userProfile by studyRepo.observeUserProfile(user.uid)
            .collectAsStateWithLifecycle(initialValue = null)

        when {
            isSuperAdmin || userProfile?.isApproved == true -> {
                MainAppContent(
                    currentUserId = user.uid,
                    currentUser = user,
                    currentUserProfile = userProfile,
                    currentLanguage = currentLanguage,
                    currentThemeMode = currentThemeMode,
                    onThemeChanged = onThemeChanged,
                    onToggleLanguage = onToggleLanguage,
                    onSignOut = { auth.signOut() }
                )
            }
            userProfile == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                PendingApprovalScreen(
                    user = user,
                    isRejected = userProfile?.isRejected == true,
                    onRefreshStatus = { /* Real-time snapshot updates automatically */ },
                    onSignOut = { auth.signOut() },
                    onToggleLanguage = onToggleLanguage
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    currentUserId: String,
    currentUser: FirebaseUser,
    currentUserProfile: com.example.data.model.UserProfile? = null,
    currentLanguage: com.example.ui.locale.AppLanguage,
    currentThemeMode: com.example.ui.theme.AppThemeMode = com.example.ui.theme.AppThemeMode.SYSTEM,
    onThemeChanged: (com.example.ui.theme.AppThemeMode) -> Unit = {},
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.ui.locale.LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Keyed by currentUserId so switching accounts creates a fresh ViewModel instance
    val driveViewModel: DriveViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as? Application) ?: (context.applicationContext as Application)
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val studyRepo = StudyRepository(db)
                val driveRepo = DriveRepository(app)
                DriveViewModel(driveRepo, studyRepo, currentUserId, currentUser.email.orEmpty())
            }
        }
    )

    val state by driveViewModel.screenState.collectAsStateWithLifecycle()
    val downloadedFiles by driveViewModel.downloadedFiles.collectAsStateWithLifecycle()
    val bookmarks by driveViewModel.bookmarks.collectAsStateWithLifecycle()
    val adminMaterials by driveViewModel.adminMaterials.collectAsStateWithLifecycle()
    val subjectFolders by driveViewModel.subjectFolders.collectAsStateWithLifecycle()
    val allStudents by driveViewModel.allStudents.collectAsStateWithLifecycle()
    val admins by driveViewModel.admins.collectAsStateWithLifecycle()
    val announcements by driveViewModel.announcements.collectAsStateWithLifecycle()
    val showApprovalBanner by driveViewModel.showApprovalBanner.collectAsStateWithLifecycle()

    val isSuperAdmin = currentUser.email.orEmpty().equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
    val allowedFolderIds = if (isSuperAdmin) emptyList() else (currentUserProfile?.allowedFolderIds ?: emptyList())

    val canAccessAdmin = isSuperAdmin || currentUserProfile?.isAdmin == true

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedFileForDetail by remember { mutableStateOf<DriveFile?>(null) }
    var selectedFileForViewer by remember { mutableStateOf<DriveFile?>(null) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var addMaterialInitialFolderId by remember { mutableStateOf("") }
    var showAddAdminDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showFolderDialog = true },
                        modifier = Modifier.testTag("top_folder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Active Study Folder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Language Switch Button
                    com.example.ui.locale.LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onToggle = onToggleLanguage
                    )

                    if (canAccessAdmin) {
                        IconButton(
                            onClick = { selectedTab = 3 },
                            modifier = Modifier.testTag("top_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = strings.tabAdmin,
                                tint = if (selectedTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier.testTag("top_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Student Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = strings.tabMaterials
                        )
                    },
                    label = { Text(strings.tabMaterials) },
                    modifier = Modifier.testTag("nav_tab_materials")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = strings.tabDownloads
                        )
                    },
                    label = { Text(strings.tabDownloads) },
                    modifier = Modifier.testTag("nav_tab_downloads")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = strings.tabStarred
                        )
                    },
                    label = { Text(strings.tabStarred) },
                    modifier = Modifier.testTag("nav_tab_starred")
                )

                if (canAccessAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = strings.tabAdmin
                            )
                        },
                        label = { Text(strings.tabAdmin) },
                        modifier = Modifier.testTag("nav_tab_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    StudyFolderScreen(
                        state = state,
                        bookmarks = bookmarks,
                        subjectFolders = subjectFolders,
                        allowedFolderIds = allowedFolderIds,
                        isSuperAdmin = isSuperAdmin,
                        announcements = announcements,
                        showApprovalBanner = showApprovalBanner,
                        onDismissApprovalBanner = { driveViewModel.dismissApprovalBanner() },
                        onSearchChange = { driveViewModel.setSearchQuery(it) },
                        onCategorySelect = { driveViewModel.selectCategory(it) },
                        onFileTypeSelect = { driveViewModel.selectFileType(it) },
                        onSemesterSelect = { driveViewModel.selectSemester(it) },
                        onAcademicYearSelect = { driveViewModel.selectAcademicYear(it) },
                        onCourseSelect = { driveViewModel.selectCourse(it) },
                        onDepartmentSelect = { driveViewModel.selectDepartment(it) },
                        onClearFilters = { driveViewModel.clearAllFilters() },
                        onDownloadClick = { file ->
                            driveViewModel.downloadFile(file)
                            scope.launch {
                                snackbarHostState.showSnackbar("جاري تحميل ${file.name}...")
                            }
                        },
                        onToggleBookmark = { file ->
                            driveViewModel.toggleBookmark(file)
                        },
                        onFileClick = { file ->
                            selectedFileForDetail = file
                        },
                        onPreviewClick = { file ->
                            selectedFileForViewer = file
                        },
                        onOpenFolderSelect = { showFolderDialog = true },
                        onFolderSelected = { folderId, folderName ->
                            driveViewModel.changeFolder(folderId, folderName)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.snackbarFolderConnected)
                            }
                        },
                        onCreateSubjectFolder = { name, subject, department, grade, description, driveUrl ->
                            driveViewModel.createSubjectFolder(name, subject, department, grade, description, driveUrl)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم إنشاء مجلد المادة بنجاح" else "Subject folder created"
                                )
                            }
                        },
                        onUpdateSubjectFolder = { updated ->
                            driveViewModel.updateSubjectFolder(updated)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم تعديل مجلد المادة بنجاح" else "Subject folder updated"
                                )
                            }
                        },
                        onDeleteSubjectFolder = { folderId ->
                            driveViewModel.deleteSubjectFolder(folderId)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حذف مجلد المادة" else "Subject folder deleted"
                                )
                            }
                        },
                        onAddMaterialToFolder = { folderId ->
                            addMaterialInitialFolderId = folderId
                            showAddMaterialDialog = true
                        },
                        onEditMaterial = { fileId, name, category, department, grade, folderId, description ->
                            driveViewModel.updateAnyMaterial(fileId, name, category, department, grade, folderId, description)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حفظ تعديلات المذكرة بنجاح" else "Material updated successfully"
                                )
                            }
                        },
                        onDeleteMaterial = { fileId ->
                            driveViewModel.deleteAnyMaterial(fileId)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حذف المذكرة نهائياً من المنصة" else "Material deleted permanently"
                                )
                            }
                        },
                        onRefresh = { driveViewModel.loadFiles() }
                    )
                }

                1 -> {
                    DownloadsScreen(
                        downloadedFiles = downloadedFiles,
                        totalStorageBytes = state.totalStorageUsedBytes,
                        allFiles = state.files,
                        isSuperAdmin = isSuperAdmin,
                        onOpenFile = { ctx, file -> driveViewModel.openFileExternal(ctx, file) },
                        onShareFile = { ctx, file -> driveViewModel.shareFile(ctx, file) },
                        onPreviewFile = { file -> selectedFileForViewer = file },
                        onDeleteDownload = { fileId, fileName ->
                            driveViewModel.deleteDownloadedFile(fileId, fileName)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.deleteOfflineOnly)
                            }
                        },
                        onEditMaterial = { file, newTitle, newCategory, newDescription ->
                            driveViewModel.updateAnyMaterial(file.id, newTitle, newCategory, description = newDescription)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.snackbarMaterialUpdated)
                            }
                        },
                        onDeleteMaterialFromPortal = { fileId ->
                            driveViewModel.deleteAnyMaterial(fileId)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.snackbarMaterialDeletedFromPortal)
                            }
                        }
                    )
                }


                2 -> {
                    BookmarksScreen(
                        bookmarks = bookmarks,
                        allFiles = state.files,
                        onFileClick = { file -> selectedFileForDetail = file },
                        onRemoveBookmark = { fileId ->
                            val file = state.files.find { it.id == fileId }
                            if (file != null) {
                                driveViewModel.toggleBookmark(file)
                            }
                        }
                    )
                }

                3 -> {
                    if (canAccessAdmin) {
                        AdminDashboardScreen(
                        adminMaterials = adminMaterials,
                        subjectFolders = subjectFolders,
                        allStudents = allStudents,
                        admins = admins,
                        currentUserEmail = currentUser.email.orEmpty(),
                        allDriveFiles = state.files,
                        activeFolderName = state.activeFolderName,
                        announcements = announcements,
                        totalStorageUsedBytes = state.totalStorageUsedBytes,
                        onAddMaterialClick = {
                            addMaterialInitialFolderId = ""
                            showAddMaterialDialog = true
                        },
                        onDeleteMaterialClick = { fileId ->
                            driveViewModel.deleteAnyMaterial(fileId)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حذف المذكرة نهائياً من المنصة" else "Material deleted permanently"
                                )
                            }
                        },
                        onEditMaterialClick = { fileId, name, category, department, grade, folderId, description ->
                            driveViewModel.updateAnyMaterial(fileId, name, category, department, grade, folderId, description)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حفظ تعديلات المذكرة بنجاح" else "Material updated successfully"
                                )
                            }
                        },
                        onPreviewMaterialClick = { file ->
                            selectedFileForViewer = file
                        },
                        onChangeFolderClick = { showFolderDialog = true },
                        onCreateSubjectFolder = { name, subject, department, grade, description, driveUrl ->
                            driveViewModel.createSubjectFolder(name, subject, department, grade, description, driveUrl)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم إنشاء مجلد المادة بنجاح" else "Subject folder created"
                                )
                            }
                        },
                        onUpdateSubjectFolder = { updated ->
                            driveViewModel.updateSubjectFolder(updated)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم تعديل مجلد المادة بنجاح" else "Subject folder updated"
                                )
                            }
                        },
                        onDeleteSubjectFolder = { folderId ->
                            driveViewModel.deleteSubjectFolder(folderId)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حذف مجلد المادة" else "Subject folder deleted"
                                )
                            }
                        },
                        onApproveStudent = { studentId ->
                            driveViewModel.approveStudent(studentId)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.snackbarStudentApproved)
                            }
                        },
                        onApproveStudentWithFolders = { studentId, folderIds ->
                            driveViewModel.approveStudentWithFolders(studentId, folderIds)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC)
                                        "تم قبول الطالب وتخصيص ${folderIds.size} مجلدات له"
                                    else
                                        "Student approved with ${folderIds.size} folders assigned"
                                )
                            }
                        },
                        onAssignStudentFolders = { studentId, folderIds ->
                            driveViewModel.assignStudentFolders(studentId, folderIds)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC)
                                        "تم تحديث المجلدات المصرح بها للطالب (${folderIds.size} مجلد)"
                                    else
                                        "Updated allowed folders for student (${folderIds.size} folders)"
                                )
                            }
                        },
                        onRejectStudent = { studentId ->
                            driveViewModel.rejectStudent(studentId)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.snackbarStudentRejected)
                            }
                        },
                        onBulkApproveStudents = { studentIds ->
                            driveViewModel.bulkApproveStudents(studentIds)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم قبول وتفعيل ${studentIds.size} طالب بنجاح" else "Selected ${studentIds.size} students approved")
                            }
                        },
                        onBulkRejectStudents = { studentIds ->
                            driveViewModel.bulkRejectStudents(studentIds)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم رفض ${studentIds.size} طلب بنجاح" else "Selected ${studentIds.size} requests rejected")
                            }
                        },
                        onPostAnnouncement = { title, content, type, isPinned ->
                            driveViewModel.postAnnouncement(title, content, type = type, isPinned = isPinned)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم نشر الإعلان بنجاح للطلاب" else "Announcement posted successfully")
                            }
                        },
                        onDeleteAnnouncement = { id ->
                            driveViewModel.deleteAnnouncement(id)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حذف الإعلان" else "Announcement deleted")
                            }
                        },
                        onAddAdminClick = { showAddAdminDialog = true },
                        onRemoveAdminClick = { adminDocId ->
                            driveViewModel.removeAdmin(adminDocId)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.snackbarAdminRemoved)
                            }
                        }
                    )
                }
            }
        }
    }
    }

    // File Detail Dialog
    selectedFileForDetail?.let { file ->
        val isBookmarked = bookmarks.any { it.fileId == file.id }
        val bookmarkNotes = bookmarks.find { it.fileId == file.id }?.notes.orEmpty()
        val downloadProgress = state.downloadProgress[file.id]
        val preview = driveViewModel.getLocalPreviewText(file.id, file.name)

        FileDetailDialog(
            file = file,
            isBookmarked = isBookmarked,
            bookmarkNotes = bookmarkNotes,
            downloadProgress = downloadProgress,
            previewContent = preview,
            onDismiss = { selectedFileForDetail = null },
            onDownloadClick = {
                driveViewModel.downloadFile(file)
                scope.launch {
                    snackbarHostState.showSnackbar(strings.downloading)
                }
            },
            onToggleBookmark = { notes ->
                driveViewModel.toggleBookmark(file, notes)
            },
            onOpenExternal = { ctx -> driveViewModel.openFileExternal(ctx, file) },
            onShare = { ctx -> driveViewModel.shareFile(ctx, file) },
            onPreviewInApp = {
                val f = file
                selectedFileForDetail = null
                selectedFileForViewer = f
            }
        )
    }

    // In-App PDF & Document Viewer
    selectedFileForViewer?.let { file ->
        val isBookmarked = bookmarks.any { it.fileId == file.id }
        val bookmarkNotes = bookmarks.find { it.fileId == file.id }?.notes.orEmpty()
        val downloadProgress = state.downloadProgress[file.id]

        com.example.ui.studydrive.DocumentViewerDialog(
            file = file,
            isBookmarked = isBookmarked,
            bookmarkNotes = bookmarkNotes,
            downloadProgress = downloadProgress,
            resolveLocalFile = { f -> driveViewModel.getPreviewFile(f) },
            onDismiss = { selectedFileForViewer = null },
            onDownloadClick = {
                driveViewModel.downloadFile(file)
                scope.launch {
                    snackbarHostState.showSnackbar(strings.downloading)
                }
            },
            onToggleBookmark = { notes ->
                driveViewModel.toggleBookmark(file, notes)
            },
            onShare = { ctx -> driveViewModel.shareFile(ctx, file) },
            onOpenExternal = { ctx -> driveViewModel.openFileExternal(ctx, file) }
        )
    }


    // Folder Switcher Dialog
    if (showFolderDialog) {
        FolderSelectDialog(
            currentFolderId = state.activeFolderId,
            currentFolderName = state.activeFolderName,
            subjectFolders = subjectFolders,
            allowedFolderIds = allowedFolderIds,
            isSuperAdmin = canAccessAdmin,
            onDismiss = { showFolderDialog = false },
            onFolderSelected = { folderId, folderName ->
                driveViewModel.changeFolder(folderId, folderName)
                scope.launch {
                    snackbarHostState.showSnackbar(strings.snackbarFolderConnected)
                }
            },
            onCreateFolder = { name, subject, department, grade, description, driveUrl ->
                driveViewModel.createSubjectFolder(name, subject, department, grade, description, driveUrl)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم إنشاء مجلد المادة بنجاح" else "Subject folder created"
                    )
                }
            },
            onUpdateFolder = { updated ->
                driveViewModel.updateSubjectFolder(updated)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم تعديل مجلد المادة بنجاح" else "Subject folder updated"
                    )
                }
            },
            onDeleteFolder = { folderId ->
                driveViewModel.deleteSubjectFolder(folderId)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم حذف مجلد المادة" else "Subject folder deleted"
                    )
                }
            }
        )
    }

    // Admin Add Material Dialog (Super Admin only can upload from mobile + drive link)
    if (showAddMaterialDialog && isSuperAdmin) {
        AdminAddMaterialDialog(
            subjectFolders = subjectFolders,
            initialFolderId = addMaterialInitialFolderId,
            onDismiss = { showAddMaterialDialog = false },
            onAddMaterial = { material, localFileUri ->
                driveViewModel.uploadMaterialWithFile(material, localFileUri, context)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC)
                            "تم رفع وحفظ المذكرة بنجاح من جهاز المشرف"
                        else
                            "Study material successfully uploaded and published"
                    )
                }
            }
        )
    }

    // Super Admin Add Admin Dialog
    if (showAddAdminDialog) {
        AddAdminDialog(
            onDismiss = { showAddAdminDialog = false },
            onAddAdmin = { email ->
                driveViewModel.addAdmin(email, currentUser.email.orEmpty())
                showAddAdminDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(strings.snackbarAdminAdded)
                }
            }
        )
    }

    // User Profile Dialog
    if (showProfileDialog) {
        UserProfileDialog(
            user = currentUser,
            activeFolderName = state.activeFolderName,
            totalStorageUsedBytes = state.totalStorageUsedBytes,
            currentThemeMode = currentThemeMode,
            onThemeChanged = onThemeChanged,
            onClearCache = {
                val freed = driveViewModel.clearCache()
                scope.launch {
                    snackbarHostState.showSnackbar(if (currentLanguage == com.example.ui.locale.AppLanguage.ARABIC) "تم مسح وتفريغ الذاكرة المؤقتة بنجاح" else "Cache cleared successfully")
                }
            },
            onDismiss = { showProfileDialog = false },
            onSignOut = onSignOut,
            onToggleLanguage = onToggleLanguage
        )
    }
}
