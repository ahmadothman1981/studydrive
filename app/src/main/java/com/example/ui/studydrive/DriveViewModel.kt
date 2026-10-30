package com.example.ui.studydrive

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminMaterial
import com.example.data.model.DownloadedFile
import com.example.data.model.DriveFile
import com.example.data.model.StudyAnnouncement
import com.example.data.model.StudyBookmark
import com.example.data.model.SubjectFolder
import com.example.data.repository.DriveRepository
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

data class DriveScreenState(
    val files: List<DriveFile> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedFileType: String = "All",
    val selectedSemester: String = "All",
    val selectedAcademicYear: String = "All",
    val selectedProfessor: String = "All",
    val selectedCourse: String = "All",
    val selectedDepartment: String = "All",
    val activeFolderId: String = "study_materials_root",
    val activeFolderName: String = "Study Materials",
    val downloadProgress: Map<String, Float> = emptyMap(),
    val totalStorageUsedBytes: Long = 0L,
    val errorMessage: String? = null
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (searchQuery.isNotBlank()) count++
            if (selectedCategory != "All") count++
            if (selectedFileType != "All") count++
            if (selectedSemester != "All") count++
            if (selectedAcademicYear != "All") count++
            if (selectedProfessor != "All") count++
            if (selectedCourse != "All") count++
            if (selectedDepartment != "All") count++
            return count
        }
}



class DriveViewModel(
    private val driveRepository: DriveRepository,
    private val studyRepository: StudyRepository,
    private val currentUserId: String,
    private val currentUserEmail: String = ""
) : ViewModel() {

    companion object {
        private const val TAG = "DriveViewModel"
    }

    val isUserSuperAdmin: Boolean = currentUserEmail.equals(com.example.data.model.SUPER_ADMIN_EMAIL, ignoreCase = true)

    private val _screenState = MutableStateFlow(DriveScreenState(isLoading = true))
    val screenState: StateFlow<DriveScreenState> = _screenState.asStateFlow()

    val downloadedFiles: StateFlow<List<DownloadedFile>> = studyRepository.observeDownloads(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<StudyBookmark>> = studyRepository.observeBookmarks(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminMaterials = MutableStateFlow<List<AdminMaterial>>(emptyList())
    val adminMaterials: StateFlow<List<AdminMaterial>> = _adminMaterials.asStateFlow()

    val userProfile: StateFlow<com.example.data.model.UserProfile?> = studyRepository.observeUserProfile(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _statusOverrides = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _studentFoldersOverrides = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    val allStudents: StateFlow<List<com.example.data.model.UserProfile>> = combine(
        studyRepository.observeAllStudents(),
        _statusOverrides,
        _studentFoldersOverrides
    ) { list, overrides, folderOverrides ->
        list.map { student ->
            var updated = student
            overrides[student.userId]?.let { updated = updated.copy(status = it) }
            folderOverrides[student.userId]?.let { updated = updated.copy(allowedFolderIds = it) }
            updated
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admins: StateFlow<List<com.example.data.model.AdminUser>> = studyRepository.observeAdmins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val defaultAnnouncements = listOf(
        StudyAnnouncement(
            id = "default_1",
            title = "جدول امتحانات منتصف العام 2024 / 2025",
            content = "تم اعتماد وإعلان جدول امتحانات الفصل الدراسي الأول لجميع الفرق والأقسام، تبدأ الامتحانات يوم 10 يناير.",
            author = "شؤون الطلاب والامتحانات",
            type = "exam",
            date = "اليوم",
            isPinned = true
        ),
        StudyAnnouncement(
            id = "default_2",
            title = "تحديث مجلدات ومذكرات البرمجة والذكاء الاصطناعي",
            content = "تمت إضافة بنك أسئلة واختبارات تجريبية تفاعلية لمادة بايثون والخوارزميات بإشراف المهندس أحمد عثمان.",
            author = "المهندس أحمد عثمان",
            type = "course",
            date = "أمس",
            isPinned = true
        ),
        StudyAnnouncement(
            id = "default_3",
            title = "ورشة عمل: أساسيات الحوسبة السحابية وقواعد البيانات",
            content = "تقام يوم السبت القادم ورشة تدريبية مجانية لجميع الطلاب مع شهادات حضور معتمدة.",
            author = "قسم علوم الحاسب",
            type = "workshop",
            date = "منذ يومين",
            isPinned = false
        )
    )

    val announcements: StateFlow<List<StudyAnnouncement>> = studyRepository.observeAnnouncements()
        .map { list ->
            if (list.isEmpty()) defaultAnnouncements else list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultAnnouncements)

    private val defaultSubjectFolders = listOf(
        SubjectFolder(
            id = "study_materials_root",
            folderId = "study_materials_root",
            name = "جميع المواد والمجلدات (الرئيسي)",
            subject = "All",
            department = "All",
            grade = "جميع الصفوف",
            description = "المجلد الرئيسي الشامل لجميع المذكرات والملفات الدراسية"
        ),
        SubjectFolder(
            id = "folder_python_1",
            folderId = "folder_python_1",
            name = "مجلد لغة بايثون (Python)",
            subject = "Python",
            department = "Computer Science",
            grade = "الصف الأول الثانوي",
            description = "شرح ومذكرات لغة بايثون، المتغيرات، الشروط والحلقات التكرارية"
        ),
        SubjectFolder(
            id = "folder_algo_2",
            folderId = "folder_algo_2",
            name = "مجلد الخوارزميات وهياكل البيانات",
            subject = "Algorithms",
            department = "Computer Science",
            grade = "الصف الثاني الثانوي",
            description = "خرائط التدفق Flowcharts وتصميم الخوارزميات والبرمجة الكائنية"
        ),
        SubjectFolder(
            id = "folder_exams_bac",
            folderId = "folder_exams_bac",
            name = "مجلد بنك أسئلة وامتحانات البكالوريا",
            subject = "Past Exams",
            department = "General Secondary",
            grade = "جميع الصفوف",
            description = "نماذج الامتحانات الرسمية السابقة مع الحلول النموذجية"
        ),
        SubjectFolder(
            id = "folder_web_html",
            folderId = "folder_web_html",
            name = "مجلد تطوير الويب HTML5 & CSS3",
            subject = "Web Development",
            department = "Engineering",
            grade = "الصف الأول الثانوي",
            description = "تصميم وتطوير صفحات ومواقع الويب التفاعلية"
        ),
        SubjectFolder(
            id = "folder_db_sql",
            folderId = "folder_db_sql",
            name = "مجلد قواعد البيانات SQL",
            subject = "Database",
            department = "Business",
            grade = "الصف الثاني الثانوي",
            description = "أساسيات قواعد البيانات العلائقية واستعلامات SQL"
        )
    )

    private val _folderUpsertOverrides = MutableStateFlow<Map<String, SubjectFolder>>(emptyMap())
    private val _deletedFolderIds = MutableStateFlow<Set<String>>(emptySet())

    val subjectFolders: StateFlow<List<SubjectFolder>> = combine(
        studyRepository.observeSubjectFolders(),
        _folderUpsertOverrides,
        _deletedFolderIds
    ) { remoteList, upserts, deletedIds ->
        val mergedMap = linkedMapOf<String, SubjectFolder>()
        defaultSubjectFolders.forEach { folder ->
            val key = folder.folderId.ifBlank { folder.id }
            mergedMap[key] = folder
        }
        remoteList.forEach { folder ->
            val key = folder.folderId.ifBlank { folder.id }
            if (key.isNotBlank()) {
                mergedMap[key] = folder.copy(id = key, folderId = key)
            }
        }
        upserts.forEach { (key, folder) ->
            mergedMap[key] = folder
        }
        deletedIds.forEach { key ->
            if (key != "study_materials_root") {
                mergedMap.remove(key)
            }
        }
        mergedMap.values.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultSubjectFolders)

    fun createSubjectFolder(
        name: String,
        subject: String,
        department: String,
        grade: String,
        description: String,
        driveFolderUrl: String = ""
    ) {
        val extractedId = if (driveFolderUrl.isNotBlank()) {
            val input = driveFolderUrl.trim()
            when {
                input.contains("/folders/") -> input.substringAfter("/folders/").substringBefore("?").substringBefore("/")
                input.contains("id=") -> input.substringAfter("id=").substringBefore("&")
                else -> input.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")
            }
        } else {
            "folder_subj_${System.currentTimeMillis()}"
        }
        val cleanId = extractedId.ifBlank { "folder_subj_${System.currentTimeMillis()}" }
        val newFolder = SubjectFolder(
            id = cleanId,
            folderId = cleanId,
            name = name.trim(),
            subject = subject.trim().ifBlank { "Python" },
            department = department.trim().ifBlank { "Computer Science" },
            grade = grade.trim().ifBlank { "جميع الصفوف" },
            description = description.trim(),
            driveFolderUrl = driveFolderUrl.trim()
        )
        _deletedFolderIds.value = _deletedFolderIds.value - cleanId
        _folderUpsertOverrides.value = _folderUpsertOverrides.value + (cleanId to newFolder)

        viewModelScope.launch {
            try {
                studyRepository.saveSubjectFolder(newFolder)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating subject folder in Firestore: ${e.message}", e)
            }
        }
    }

    fun updateSubjectFolder(folder: SubjectFolder) {
        val cleanId = folder.folderId.ifBlank { folder.id }
        if (cleanId.isBlank()) return
        val updatedFolder = folder.copy(id = cleanId, folderId = cleanId)
        _folderUpsertOverrides.value = _folderUpsertOverrides.value + (cleanId to updatedFolder)

        if (_screenState.value.activeFolderId == cleanId) {
            _screenState.value = _screenState.value.copy(activeFolderName = updatedFolder.name)
        }

        viewModelScope.launch {
            try {
                studyRepository.updateSubjectFolder(updatedFolder)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating subject folder in Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteSubjectFolder(folderId: String) {
        val cleanId = folderId.trim()
        if (cleanId.isBlank() || cleanId == "study_materials_root") return

        _deletedFolderIds.value = _deletedFolderIds.value + cleanId
        _folderUpsertOverrides.value = _folderUpsertOverrides.value - cleanId

        if (_screenState.value.activeFolderId == cleanId) {
            changeFolder("study_materials_root", "جميع المواد والمجلدات (الرئيسي)")
        }

        viewModelScope.launch {
            try {
                studyRepository.deleteSubjectFolder(cleanId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting subject folder in Firestore: ${e.message}", e)
            }
        }
    }

    private val _showApprovalBanner = MutableStateFlow(true)
    val showApprovalBanner: StateFlow<Boolean> = _showApprovalBanner.asStateFlow()

    fun dismissApprovalBanner() {
        _showApprovalBanner.value = false
    }

    init {
        driveRepository.remoteBytesFetcher = { fileId ->
            studyRepository.downloadMaterialFileBytes(fileId)
        }
        if (isUserSuperAdmin) {
            viewModelScope.launch {
                studyRepository.observeAdminMaterials(emptyList(), isAdmin = true).collect { materials ->
                    _adminMaterials.value = materials
                    driveRepository.syncAdminMaterials(materials.map { it.toDriveFile() })
                    loadFiles()
                }
            }
        }
        loadFiles()
        observeProfile()
        observeAdminMaterials()
    }

    fun approveStudent(studentId: String) {
        _statusOverrides.value = _statusOverrides.value + (studentId to "approved")
        viewModelScope.launch {
            try {
                studyRepository.updateUserStatus(studentId, "approved")
            } catch (e: Exception) {
                Log.e(TAG, "Error approving student: ${e.message}", e)
                _statusOverrides.value = _statusOverrides.value - studentId
            }
        }
    }

    fun approveStudentWithFolders(studentId: String, allowedFolderIds: List<String>) {
        _statusOverrides.value = _statusOverrides.value + (studentId to "approved")
        _studentFoldersOverrides.value = _studentFoldersOverrides.value + (studentId to allowedFolderIds)
        viewModelScope.launch {
            try {
                studyRepository.approveStudentWithFolders(studentId, allowedFolderIds)
            } catch (e: Exception) {
                Log.e(TAG, "Error approving student with folders: ${e.message}", e)
                _statusOverrides.value = _statusOverrides.value - studentId
            }
        }
    }

    fun assignStudentFolders(studentId: String, allowedFolderIds: List<String>) {
        _studentFoldersOverrides.value = _studentFoldersOverrides.value + (studentId to allowedFolderIds)
        viewModelScope.launch {
            try {
                studyRepository.updateStudentAllowedFolders(studentId, allowedFolderIds)
            } catch (e: Exception) {
                Log.e(TAG, "Error assigning student folders: ${e.message}", e)
            }
        }
    }

    fun bulkApproveStudents(studentIds: List<String>) {
        if (studentIds.isEmpty()) return
        val current = _statusOverrides.value.toMutableMap()
        studentIds.forEach { current[it] = "approved" }
        _statusOverrides.value = current
        viewModelScope.launch {
            try {
                studyRepository.bulkUpdateStudentStatus(studentIds, "approved")
            } catch (e: Exception) {
                Log.e(TAG, "Error bulk approving students: ${e.message}", e)
                val rollback = _statusOverrides.value.toMutableMap()
                studentIds.forEach { rollback.remove(it) }
                _statusOverrides.value = rollback
            }
        }
    }

    fun bulkRejectStudents(studentIds: List<String>) {
        if (studentIds.isEmpty()) return
        val current = _statusOverrides.value.toMutableMap()
        studentIds.forEach { current[it] = "rejected" }
        _statusOverrides.value = current
        viewModelScope.launch {
            try {
                studyRepository.bulkUpdateStudentStatus(studentIds, "rejected")
            } catch (e: Exception) {
                Log.e(TAG, "Error bulk rejecting students: ${e.message}", e)
                val rollback = _statusOverrides.value.toMutableMap()
                studentIds.forEach { rollback.remove(it) }
                _statusOverrides.value = rollback
            }
        }
    }

    fun rejectStudent(studentId: String) {
        _statusOverrides.value = _statusOverrides.value + (studentId to "rejected")
        viewModelScope.launch {
            try {
                studyRepository.updateUserStatus(studentId, "rejected")
            } catch (e: Exception) {
                Log.e(TAG, "Error rejecting student: ${e.message}", e)
                _statusOverrides.value = _statusOverrides.value - studentId
            }
        }
    }

    fun revokeStudent(studentId: String) {
        _statusOverrides.value = _statusOverrides.value + (studentId to "pending")
        viewModelScope.launch {
            try {
                studyRepository.updateUserStatus(studentId, "pending")
            } catch (e: Exception) {
                Log.e(TAG, "Error revoking student: ${e.message}", e)
                _statusOverrides.value = _statusOverrides.value - studentId
            }
        }
    }

    fun addAdmin(email: String, superAdminEmail: String) {
        viewModelScope.launch {
            try {
                studyRepository.addAdminByEmail(email, superAdminEmail)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding admin: ${e.message}", e)
            }
        }
    }

    fun removeAdmin(adminDocId: String) {
        viewModelScope.launch {
            try {
                studyRepository.removeAdmin(adminDocId)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing admin: ${e.message}", e)
            }
        }
    }

    private var adminMaterialsJob: Job? = null

    private fun observeAdminMaterials() {
        viewModelScope.launch {
            userProfile.collect { profile ->
                adminMaterialsJob?.cancel()
                val isSuper = isUserSuperAdmin || profile?.isSuperAdmin == true
                val isAdmin = isSuper || profile?.isAdmin == true
                val allowedFolders = if (isSuper) emptyList() else (profile?.allowedFolderIds ?: emptyList())
                adminMaterialsJob = launch {
                    studyRepository.observeAdminMaterials(allowedFolders, isAdmin).collect { materials ->
                        _adminMaterials.value = materials
                        driveRepository.syncAdminMaterials(materials.map { it.toDriveFile() })
                        loadFiles()
                    }
                }
            }
        }
    }

    fun addAdminMaterial(material: com.example.data.model.AdminMaterial) {
        viewModelScope.launch {
            try {
                studyRepository.addAdminMaterial(material)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save admin material: ${e.message}", e)
            }
        }
    }

    fun uploadMaterialWithFile(
        material: com.example.data.model.AdminMaterial,
        contentUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                var sizeBytes = material.sizeBytes
                var downloadUrl = material.downloadUrl.trim()
                val cleanFileId = material.fileId.ifBlank { material.id }
                var fileBytes: ByteArray? = null

                if (contentUri != null) {
                    try {
                        fileBytes = context.contentResolver.openInputStream(contentUri)?.use { it.readBytes() }
                    } catch (readErr: Exception) {
                        Log.e(TAG, "Failed to read file bytes: ${readErr.message}", readErr)
                    }

                    if (fileBytes != null && fileBytes.isNotEmpty()) {
                        driveRepository.saveMobileUploadedFile(
                            inputStream = fileBytes.inputStream(),
                            fileId = cleanFileId,
                            fileName = material.name
                        )
                        sizeBytes = fileBytes.size.toLong()
                    }

                    // Optional Firebase Storage upload if configured
                    try {
                        val cleanFolder = material.folderId.ifBlank { "study_materials_root" }
                        val cleanName = material.name.ifBlank { "material_${System.currentTimeMillis()}.pdf" }
                        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                            .reference.child("materials/$cleanFolder/$cleanFileId/$cleanName")

                        storageRef.putFile(contentUri).await()
                        val remoteUrl = storageRef.downloadUrl.await().toString()
                        if (remoteUrl.isNotBlank()) {
                            downloadUrl = remoteUrl
                            Log.d(TAG, "Uploaded material to Firebase Storage: $remoteUrl")
                        }
                    } catch (_: Exception) {}
                }

                val hasChunks = fileBytes != null && fileBytes.isNotEmpty()
                val totalChunks = if (hasChunks) (fileBytes!!.size + 400 * 1024 - 1) / (400 * 1024) else 0

                val finalMaterial = material.copy(
                    sizeBytes = if (sizeBytes > 0) sizeBytes else material.sizeBytes,
                    downloadUrl = downloadUrl,
                    hasChunks = hasChunks,
                    totalChunks = totalChunks
                )
                studyRepository.addAdminMaterial(finalMaterial)

                if (hasChunks && fileBytes != null) {
                    studyRepository.uploadMaterialFileBytes(cleanFileId, fileBytes)
                }
                loadFiles()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload admin material: ${e.message}", e)
            }
        }
    }

    fun deleteAnyMaterial(fileId: String) {
        viewModelScope.launch {
            try {
                try {
                    studyRepository.deleteAdminMaterial(fileId)
                } catch (e: Exception) {
                    Log.w(TAG, "Not in admin_materials or already deleted: ${e.message}")
                }
                val file = _screenState.value.files.find { it.id == fileId }
                if (file != null) {
                    driveRepository.deleteDownloadedFile(fileId, file.name)
                }
                studyRepository.removeDownload(currentUserId, fileId)
                studyRepository.removeBookmark(currentUserId, fileId)
                driveRepository.deleteAnyMaterial(fileId)
                loadFiles()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete material: ${e.message}", e)
            }
        }
    }

    fun deleteAdminMaterial(fileId: String) {
        deleteAnyMaterial(fileId)
    }

    fun updateAnyMaterial(
        fileId: String,
        name: String,
        category: String,
        department: String = "Computer Science",
        grade: String = "جميع الصفوف",
        folderId: String = "study_materials_root",
        description: String = ""
    ) {
        viewModelScope.launch {
            try {
                try {
                    studyRepository.updateAdminMaterial(
                        fileId = fileId,
                        name = name,
                        category = category,
                        description = description,
                        department = department,
                        grade = grade,
                        folderId = folderId
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed updating Firestore material: ${e.message}")
                }

                driveRepository.updateAnyMaterial(
                    fileId = fileId,
                    name = name,
                    category = category,
                    department = department,
                    grade = grade,
                    folderId = folderId,
                    description = description
                )
                loadFiles()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update material: ${e.message}", e)
            }
        }
    }

    fun updateAdminMaterial(fileId: String, name: String, category: String, description: String) {
        updateAnyMaterial(
            fileId = fileId,
            name = name,
            category = category,
            description = description
        )
    }

    private fun observeProfile() {
        viewModelScope.launch {
            studyRepository.observeUserProfile(currentUserId).collect { profile ->
                if (profile != null) {
                    val isSuper = profile.isSuperAdmin
                    val newFolderId: String
                    val newFolderName: String

                    if (isSuper) {
                        newFolderId = profile.activeFolderId.ifBlank { "study_materials_root" }
                        newFolderName = profile.activeFolderName.ifBlank { "جميع المواد والمجلدات (الرئيسي)" }
                    } else if (profile.allowedFolderIds.isNotEmpty()) {
                        val currentActive = _screenState.value.activeFolderId
                        if (currentActive in profile.allowedFolderIds) {
                            newFolderId = currentActive
                            newFolderName = _screenState.value.activeFolderName
                        } else {
                            val firstFolderId = profile.allowedFolderIds.first()
                            val matchingFolder = subjectFolders.value.find { (it.folderId.ifBlank { it.id }) == firstFolderId }
                            newFolderId = firstFolderId
                            newFolderName = matchingFolder?.name ?: "المجلد المخصص"
                        }
                    } else {
                        newFolderId = "no_assigned_folders"
                        newFolderName = "بانتظار تخصيص المجلدات"
                    }

                    _screenState.value = _screenState.value.copy(
                        activeFolderId = newFolderId,
                        activeFolderName = newFolderName
                    )
                }
            }
        }
    }

    fun loadFiles(folderId: String? = null) {
        viewModelScope.launch {
            _screenState.value = _screenState.value.copy(isLoading = true, errorMessage = null)
            try {
                val targetFolder = folderId ?: _screenState.value.activeFolderId
                val files = driveRepository.fetchStudyFolderFiles(targetFolder)
                val storage = driveRepository.getTotalStorageUsedBytes()
                _screenState.value = _screenState.value.copy(
                    files = files,
                    isLoading = false,
                    totalStorageUsedBytes = storage
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading study files: ${e.message}", e)
                _screenState.value = _screenState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load study files: ${e.localizedMessage}"
                )
            }
        }
    }

    fun changeFolder(folderId: String, folderName: String) {
        viewModelScope.launch {
            val cleanId = folderId.trim()
            val cleanName = folderName.trim().ifBlank { "Study Materials" }
            _screenState.value = _screenState.value.copy(
                activeFolderId = cleanId,
                activeFolderName = cleanName
            )
            studyRepository.updateActiveFolder(currentUserId, cleanId, cleanName)
            loadFiles(cleanId)
        }
    }

    fun setSearchQuery(query: String) {
        _screenState.value = _screenState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: String) {
        _screenState.value = _screenState.value.copy(selectedCategory = category)
    }

    fun selectFileType(type: String) {
        _screenState.value = _screenState.value.copy(selectedFileType = type)
    }

    fun selectSemester(semester: String) {
        _screenState.value = _screenState.value.copy(selectedSemester = semester)
    }

    fun selectAcademicYear(year: String) {
        _screenState.value = _screenState.value.copy(selectedAcademicYear = year)
    }

    fun selectProfessor(prof: String) {
        _screenState.value = _screenState.value.copy(selectedProfessor = prof)
    }

    fun selectCourse(course: String) {
        _screenState.value = _screenState.value.copy(selectedCourse = course)
    }

    fun selectDepartment(dept: String) {
        _screenState.value = _screenState.value.copy(selectedDepartment = dept)
    }

    fun clearAllFilters() {
        _screenState.value = _screenState.value.copy(
            searchQuery = "",
            selectedCategory = "All",
            selectedFileType = "All",
            selectedSemester = "All",
            selectedAcademicYear = "All",
            selectedProfessor = "All",
            selectedCourse = "All",
            selectedDepartment = "All"
        )
    }

    fun postAnnouncement(title: String, content: String, author: String = "Academic Administration", type: String = "general", isPinned: Boolean = true) {
        viewModelScope.launch {
            try {
                val announcement = StudyAnnouncement(
                    title = title,
                    content = content,
                    author = author,
                    type = type,
                    date = "الآن",
                    isPinned = isPinned
                )
                studyRepository.addAnnouncement(announcement)
            } catch (e: Exception) {
                Log.e(TAG, "Error posting announcement: ${e.message}", e)
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            try {
                studyRepository.deleteAnnouncement(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting announcement: ${e.message}", e)
            }
        }
    }

    fun clearCache(): Long {
        val freedBytes = driveRepository.clearLocalCache()
        viewModelScope.launch {
            loadFiles()
            _screenState.value = _screenState.value.copy(
                totalStorageUsedBytes = 0L
            )
        }
        return freedBytes
    }

    suspend fun getPreviewFile(file: DriveFile): File {
        return driveRepository.getOrGeneratePreviewFile(file)
    }

    fun downloadFile(file: DriveFile) {
        viewModelScope.launch {
            val currentMap = _screenState.value.downloadProgress.toMutableMap()
            currentMap[file.id] = 0.05f
            _screenState.value = _screenState.value.copy(downloadProgress = currentMap)

            try {
                val downloadedLocalFile = driveRepository.downloadFile(file) { progress ->
                    val map = _screenState.value.downloadProgress.toMutableMap()
                    map[file.id] = progress
                    _screenState.value = _screenState.value.copy(downloadProgress = map)
                }

                // Record download in Firestore
                studyRepository.recordDownload(
                    userId = currentUserId,
                    download = DownloadedFile(
                        id = file.id,
                        fileId = file.id,
                        fileName = file.name,
                        mimeType = file.mimeType,
                        fileSizeBytes = downloadedLocalFile.length(),
                        localFilePath = downloadedLocalFile.absolutePath,
                        userId = currentUserId
                    )
                )

                // Refresh files state
                loadFiles()

                val finalMap = _screenState.value.downloadProgress.toMutableMap()
                finalMap.remove(file.id)
                _screenState.value = _screenState.value.copy(
                    downloadProgress = finalMap,
                    totalStorageUsedBytes = driveRepository.getTotalStorageUsedBytes()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Download failed for ${file.name}: ${e.message}", e)
                val finalMap = _screenState.value.downloadProgress.toMutableMap()
                finalMap.remove(file.id)
                _screenState.value = _screenState.value.copy(
                    downloadProgress = finalMap,
                    errorMessage = "Failed to download ${file.name}"
                )
            }
        }
    }

    fun deleteDownloadedFile(fileId: String, fileName: String) {
        viewModelScope.launch {
            try {
                driveRepository.deleteDownloadedFile(fileId, fileName)
                studyRepository.removeDownload(currentUserId, fileId)
                loadFiles()
                _screenState.value = _screenState.value.copy(
                    totalStorageUsedBytes = driveRepository.getTotalStorageUsedBytes()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting local file: ${e.message}", e)
            }
        }
    }

    fun toggleBookmark(file: DriveFile, notes: String = "") {
        viewModelScope.launch {
            val isBookmarked = bookmarks.value.any { it.fileId == file.id }
            try {
                if (isBookmarked) {
                    studyRepository.removeBookmark(currentUserId, file.id)
                } else {
                    studyRepository.addBookmark(
                        currentUserId,
                        StudyBookmark(
                            id = file.id,
                            fileId = file.id,
                            fileName = file.name,
                            mimeType = file.mimeType,
                            notes = notes,
                            category = file.category,
                            userId = currentUserId
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling bookmark: ${e.message}", e)
            }
        }
    }

    fun updateBookmarkNotes(fileId: String, fileName: String, mimeType: String, notes: String) {
        viewModelScope.launch {
            try {
                studyRepository.addBookmark(
                    currentUserId,
                    StudyBookmark(
                        id = fileId,
                        fileId = fileId,
                        fileName = fileName,
                        mimeType = mimeType,
                        notes = notes,
                        userId = currentUserId
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating bookmark notes: ${e.message}", e)
            }
        }
    }

    fun openFileExternal(context: Context, file: DriveFile) {
        try {
            val localPath = file.localFilePath
            if (localPath != null && File(localPath).exists()) {
                val fileObj = File(localPath)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    fileObj
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, file.mimeType.ifBlank { "*/*" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else if (file.webViewLink.isNotBlank()) {
                val safeUri = com.example.security.SafeUrl.toSafeUri(file.webViewLink)
                if (safeUri != null) {
                    val intent = Intent(Intent.ACTION_VIEW, safeUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Invalid or untrusted file link", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Download file first to open locally", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open file: ${e.message}", e)
            Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_LONG).show()
        }
    }

    fun shareFile(context: Context, file: DriveFile) {
        try {
            val localPath = file.localFilePath
            if (localPath != null && File(localPath).exists()) {
                val fileObj = File(localPath)
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    fileObj
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = file.mimeType.ifBlank { "*/*" }
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, file.name)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Study Material"))
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Study Material: ${file.name}\n${file.webViewLink}")
                    putExtra(Intent.EXTRA_SUBJECT, file.name)
                }
                context.startActivity(Intent.createChooser(intent, "Share Study Link"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share file: ${e.message}", e)
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getLocalPreviewText(fileName: String): String? {
        return driveRepository.getLocalFileContent(fileName)
    }
}
