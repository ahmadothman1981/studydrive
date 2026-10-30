package com.example.data.repository

import android.util.Log
import com.example.data.model.AdminMaterial
import com.example.data.model.AdminUser
import com.example.data.model.DownloadedFile
import com.example.data.model.SUPER_ADMIN_EMAIL
import com.example.data.model.StudyAnnouncement
import com.example.data.model.StudyBookmark
import com.example.data.model.UserProfile
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class StudyRepository(private val db: FirebaseFirestore) {

    companion object {
        private const val TAG = "StudyRepository"
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java)
                } else {
                    null
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing user profile", e)
                emit(null)
            }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        require(profile.userId.isNotBlank()) { "User ID cannot be blank" }
        try {
            db.collection("users").document(profile.userId)
                .set(profile.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateUserStatus(userId: String, status: String) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        try {
            db.collection("users").document(userId)
                .update("status", status)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user status: ${e.message}", e)
            throw e
        }
    }

    private fun validateAssignedFolders(folderIds: List<String>) {
        require(folderIds.distinct().size <= 16) { "A student can be assigned up to 16 folders." }
        require(folderIds.all { it.matches(Regex("^[A-Za-z0-9_\\-]{1,64}$")) }) {
            "Folder IDs must contain only letters, numbers, underscores or hyphens and be 1–64 characters long."
        }
    }

    suspend fun updateStudentAllowedFolders(userId: String, allowedFolderIds: List<String>) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        validateAssignedFolders(allowedFolderIds)
        try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "allowedFolderIds" to allowedFolderIds,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating student allowed folders: ${e.message}", e)
            throw e
        }
    }

    suspend fun approveStudentWithFolders(userId: String, allowedFolderIds: List<String>) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        validateAssignedFolders(allowedFolderIds)
        try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "status" to "approved",
                        "allowedFolderIds" to allowedFolderIds,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error approving student with folders: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateActiveFolder(userId: String, folderId: String, folderName: String) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "activeFolderId" to folderId,
                        "activeFolderName" to folderName
                    )
                ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating active folder: ${e.message}", e)
            throw e
        }
    }

    // Super Admin & Admin: Observe all registered students to approve/reject
    fun observeAllStudents(): Flow<List<UserProfile>> {
        return db.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserProfile::class.java)
                    .sortedByDescending { it.createdAt }
            }
            .catch { e ->
                Log.e(TAG, "Error observing all students", e)
                emit(emptyList())
            }
    }

    // Super Admin & Admin: Admins management
    fun observeAdmins(): Flow<List<AdminUser>> {
        return db.collection("admins")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AdminUser::class.java)
                    .sortedByDescending { it.createdAt }
            }
            .catch { e ->
                Log.e(TAG, "Error observing admins", e)
                emit(emptyList())
            }
    }

    suspend fun addAdminByEmail(email: String, superAdminEmail: String) {
        require(email.isNotBlank()) { "Admin email cannot be blank" }
        val cleanEmail = email.trim().lowercase()
        // This email-keyed document is written only by a super admin. Firestore rules
        // use the verified Firebase Auth email to recognize the assignment.
        val docId = cleanEmail
        try {
            val adminUser = AdminUser(
                id = docId,
                email = cleanEmail,
                addedBy = superAdminEmail,
                role = "admin"
            )
            db.collection("admins").document(docId)
                .set(adminUser.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding admin: ${e.message}", e)
            throw e
        }
    }

    suspend fun removeAdmin(adminDocId: String) {
        require(adminDocId.isNotBlank()) { "Admin ID cannot be blank" }
        try {
            db.collection("admins").document(adminDocId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error removing admin: ${e.message}", e)
            throw e
        }
    }

    fun observeDownloads(userId: String): Flow<List<DownloadedFile>> {
        return db.collection("users").document(userId)
            .collection("downloads")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(DownloadedFile::class.java)
                    .sortedByDescending { it.downloadedAt }
            }
            .catch { e ->
                Log.e(TAG, "Error observing downloads", e)
                emit(emptyList())
            }
    }

    suspend fun recordDownload(userId: String, download: DownloadedFile) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(download.fileId.isNotBlank()) { "File ID cannot be blank" }
        try {
            db.collection("users").document(userId)
                .collection("downloads")
                .document(download.fileId)
                .set(download.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error recording download: ${e.message}", e)
            throw e
        }
        incrementMaterialDownloadCount(download.fileId)
    }

    /**
     * Best-effort counter update shown on the admin dashboard. Admins may write
     * `admin_materials`, so students can bump this field via security rules;
     * failures here must never break the local download flow.
     */
    private suspend fun incrementMaterialDownloadCount(fileId: String) {
        try {
            db.collection("admin_materials").document(fileId)
                .update("downloadCount", FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            // Document may not exist in admin_materials (sample/local material) — ignore.
            Log.d(TAG, "Could not increment downloadCount for $fileId: ${e.message}")
        }
    }

    suspend fun removeDownload(userId: String, fileId: String) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        try {
            db.collection("users").document(userId)
                .collection("downloads")
                .document(fileId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting download record: ${e.message}", e)
            throw e
        }
    }

    fun observeBookmarks(userId: String): Flow<List<StudyBookmark>> {
        return db.collection("users").document(userId)
            .collection("bookmarks")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyBookmark::class.java)
                    .sortedByDescending { it.createdAt }
            }
            .catch { e ->
                Log.e(TAG, "Error observing bookmarks", e)
                emit(emptyList())
            }
    }

    suspend fun addBookmark(userId: String, bookmark: StudyBookmark) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(bookmark.fileId.isNotBlank()) { "File ID cannot be blank" }
        try {
            db.collection("users").document(userId)
                .collection("bookmarks")
                .document(bookmark.fileId)
                .set(bookmark.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding bookmark: ${e.message}", e)
            throw e
        }
    }

    suspend fun removeBookmark(userId: String, fileId: String) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        try {
            db.collection("users").document(userId)
                .collection("bookmarks")
                .document(fileId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error removing bookmark: ${e.message}", e)
            throw e
        }
    }

    // Admin Materials Methods
    fun observeAdminMaterials(
        allowedFolderIds: List<String> = emptyList(),
        isAdmin: Boolean = false
    ): Flow<List<AdminMaterial>> {
        return when {
            isAdmin -> {
                db.collection("admin_materials")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(AdminMaterial::class.java)
                            .sortedByDescending { it.createdAt }
                    }
                    .catch { e ->
                        Log.e(TAG, "Error observing admin materials as admin", e)
                        emit(emptyList())
                    }
            }
            allowedFolderIds.isNotEmpty() -> {
                val queryFolders = allowedFolderIds.take(30)
                db.collection("admin_materials")
                    .whereIn("folderId", queryFolders)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(AdminMaterial::class.java)
                            .sortedByDescending { it.createdAt }
                    }
                    .catch { e ->
                        Log.e(TAG, "Error observing admin materials for folders: $queryFolders", e)
                        emit(emptyList())
                    }
            }
            else -> {
                flowOf(emptyList())
            }
        }
    }

    suspend fun addAdminMaterial(material: AdminMaterial) {
        require(material.fileId.isNotBlank()) { "File ID cannot be blank" }
        try {
            db.collection("admin_materials")
                .document(material.fileId)
                .set(material.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving admin material: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateAdminMaterial(
        fileId: String,
        name: String,
        category: String,
        description: String,
        department: String = "Computer Science",
        grade: String = "جميع الصفوف",
        folderId: String = "study_materials_root"
    ) {
        require(fileId.isNotBlank()) { "File ID cannot be blank" }
        try {
            val docRef = db.collection("admin_materials").document(fileId)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "category" to category,
                    "description" to description,
                    "department" to department,
                    "grade" to grade,
                    "folderId" to folderId.ifBlank { "study_materials_root" }
                )
                docRef.update(updates).await()
            } else {
                val newMaterial = AdminMaterial(
                    fileId = fileId,
                    name = name,
                    category = category,
                    description = description,
                    department = department,
                    grade = grade,
                    folderId = folderId.ifBlank { "study_materials_root" },
                    mimeType = if (name.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "application/octet-stream"
                )
                docRef.set(newMaterial.toMap()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating admin material: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAdminMaterial(fileId: String) {
        require(fileId.isNotBlank()) { "File ID cannot be blank" }
        try {
            val docRef = db.collection("admin_materials").document(fileId)
            try {
                val chunks = docRef.collection("chunks").get().await()
                if (!chunks.isEmpty) {
                    val batch = db.batch()
                    chunks.documents.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
            } catch (_: Exception) {}
            docRef.delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting admin material: ${e.message}", e)
            throw e
        }
    }

    suspend fun uploadMaterialFileBytes(
        materialId: String,
        bytes: ByteArray,
        onProgress: (Float) -> Unit = {}
    ) {
        if (materialId.isBlank() || bytes.isEmpty()) return
        val chunkSize = 400 * 1024 // 400 KB per chunk (Firestore limit is 1MB)
        val totalChunks = (bytes.size + chunkSize - 1) / chunkSize
        val chunksCol = db.collection("admin_materials").document(materialId).collection("chunks")

        // First clean up any older chunks
        try {
            val existing = chunksCol.get().await()
            if (!existing.isEmpty) {
                val batch = db.batch()
                existing.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        } catch (_: Exception) {}

        for (i in 0 until totalChunks) {
            val start = i * chunkSize
            val end = minOf(start + chunkSize, bytes.size)
            val chunkBytes = bytes.copyOfRange(start, end)
            val chunkDocId = String.format("%05d", i)

            val chunkMap = mapOf(
                "chunkIndex" to i,
                "totalChunks" to totalChunks,
                "data" to Blob.fromBytes(chunkBytes),
                "size" to chunkBytes.size
            )
            chunksCol.document(chunkDocId).set(chunkMap).await()
            onProgress((i + 1).toFloat() / totalChunks.toFloat())
        }
    }

    suspend fun downloadMaterialFileBytes(
        materialId: String,
        onProgress: (Float) -> Unit = {}
    ): ByteArray? {
        if (materialId.isBlank()) return null
        try {
            val chunksCol = db.collection("admin_materials").document(materialId).collection("chunks")
            val snapshot = chunksCol.orderBy("chunkIndex").get().await()
            if (snapshot.isEmpty) return null

            val docs = snapshot.documents
            val outputStream = ByteArrayOutputStream()
            docs.forEachIndexed { index, doc ->
                val blob = doc.getBlob("data")
                if (blob != null) {
                    outputStream.write(blob.toBytes())
                }
                onProgress((index + 1).toFloat() / docs.size.toFloat())
            }
            val result = outputStream.toByteArray()
            return if (result.isNotEmpty()) result else null
        } catch (e: Exception) {
            Log.w(TAG, "Error downloading chunks for material $materialId: ${e.message}")
            return null
        }
    }

    suspend fun bulkUpdateStudentStatus(studentIds: List<String>, status: String) {
        if (studentIds.isEmpty()) return
        val batch = db.batch()
        for (id in studentIds) {
            val docRef = db.collection("users").document(id)
            batch.update(docRef, "status", status)
        }
        try {
            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error in bulk updating student status: ${e.message}", e)
            throw e
        }
    }

    fun observeAnnouncements(): Flow<List<StudyAnnouncement>> {
        return db.collection("announcements")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(StudyAnnouncement::class.java)
                    .sortedByDescending { it.createdAt }
            }
            .catch { e ->
                Log.e(TAG, "Error observing announcements", e)
                emit(emptyList())
            }
    }

    suspend fun addAnnouncement(announcement: StudyAnnouncement) {
        try {
            val docRef = if (announcement.id.isNotBlank()) {
                db.collection("announcements").document(announcement.id)
            } else {
                db.collection("announcements").document()
            }
            docRef.set(announcement.toMap()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding announcement: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAnnouncement(id: String) {
        try {
            db.collection("announcements").document(id).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting announcement: ${e.message}", e)
            throw e
        }
    }

    // Subject Folders CRUD (Super Admin & Admin)
    fun observeSubjectFolders(): Flow<List<com.example.data.model.SubjectFolder>> {
        return db.collection("subject_folders")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(com.example.data.model.SubjectFolder::class.java)
                    .sortedByDescending { it.createdAt }
            }
            .catch { e ->
                Log.e(TAG, "Error observing subject folders", e)
                emit(emptyList())
            }
    }

    suspend fun saveSubjectFolder(folder: com.example.data.model.SubjectFolder) {
        val docId = folder.folderId.ifBlank { folder.id }.ifBlank { "folder_${System.currentTimeMillis()}" }
        try {
            db.collection("subject_folders")
                .document(docId)
                .set(folder.copy(id = docId, folderId = docId).toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving subject folder: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateSubjectFolder(folder: com.example.data.model.SubjectFolder) {
        val docId = folder.folderId.ifBlank { folder.id }
        require(docId.isNotBlank()) { "Folder ID cannot be blank" }
        try {
            val docRef = db.collection("subject_folders").document(docId)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "name" to folder.name,
                    "subject" to folder.subject,
                    "department" to folder.department,
                    "grade" to folder.grade,
                    "description" to folder.description,
                    "driveFolderUrl" to folder.driveFolderUrl
                )
                docRef.update(updates).await()
            } else {
                docRef.set(folder.copy(id = docId, folderId = docId).toMap()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subject folder: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteSubjectFolder(folderId: String) {
        require(folderId.isNotBlank()) { "Folder ID cannot be blank" }
        try {
            db.collection("subject_folders")
                .document(folderId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subject folder: ${e.message}", e)
            throw e
        }
    }
}



