package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val SUPER_ADMIN_EMAIL = "ahmadothmanshoap1981@gmail.com"

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val role: String = "student",   // "student", "admin", "super_admin"
    val activeFolderId: String = "study_materials_root",
    val activeFolderName: String = "بوابة البرمجة للبكالوريا",
    val allowedFolderIds: List<String> = emptyList(), // Specific folders assigned to this student
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isSuperAdmin: Boolean
        get() = email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) || role == "super_admin"

    val isAdmin: Boolean
        get() = isSuperAdmin || role == "admin"

    val isApproved: Boolean
        get() = isSuperAdmin || status == "approved"

    val isPending: Boolean
        get() = !isSuperAdmin && status == "pending"

    val isRejected: Boolean
        get() = !isSuperAdmin && status == "rejected"

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["userId"] = userId
        map["displayName"] = displayName
        map["email"] = email
        map["status"] = status
        map["role"] = role
        map["activeFolderId"] = activeFolderId
        map["activeFolderName"] = activeFolderName
        map["allowedFolderIds"] = allowedFolderIds
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        updatedAt?.let { map["updatedAt"] = it } ?: run { map["updatedAt"] = FieldValue.serverTimestamp() }
        return map
    }
}

@IgnoreExtraProperties
data class AdminUser(
    @DocumentId val id: String = "",
    val email: String = "",
    val addedBy: String = "",
    val role: String = "admin",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["email"] = email
        map["addedBy"] = addedBy
        map["role"] = role
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        return map
    }
}

@IgnoreExtraProperties
data class DownloadedFile(
    @DocumentId val id: String = "",
    val fileId: String = "",
    val fileName: String = "",
    val mimeType: String = "",
    val fileSizeBytes: Long = 0L,
    val localFilePath: String = "",
    val userId: String = "",
    @ServerTimestamp val downloadedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["fileId"] = fileId
        map["fileName"] = fileName
        map["mimeType"] = mimeType
        map["fileSizeBytes"] = fileSizeBytes
        map["localFilePath"] = localFilePath
        map["userId"] = userId
        map["downloadedAt"] = downloadedAt ?: FieldValue.serverTimestamp()
        return map
    }
}

@IgnoreExtraProperties
data class StudyBookmark(
    @DocumentId val id: String = "",
    val fileId: String = "",
    val fileName: String = "",
    val mimeType: String = "",
    val notes: String = "",
    val category: String = "General",
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["fileId"] = fileId
        map["fileName"] = fileName
        if (mimeType.isNotBlank()) map["mimeType"] = mimeType
        if (notes.isNotBlank()) map["notes"] = notes
        if (category.isNotBlank()) map["category"] = category
        map["userId"] = userId
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        return map
    }
}

@IgnoreExtraProperties
data class AdminMaterial(
    @DocumentId val id: String = "",
    val fileId: String = "",
    val name: String = "",
    val category: String = "Python",
    val grade: String = "الصف الأول الثانوي",
    val mimeType: String = "application/pdf",
    val description: String = "",
    val downloadUrl: String = "",
    val sizeBytes: Long = 2_500_000L,
    val courseName: String = "",
    val professor: String = "المهندس أحمد عثمان",
    val materialType: String = "PDF",
    val semester: String = "Fall",
    val academicYear: String = "2025-2026",
    val subject: String = "",
    val department: String = "Computer Science",
    val folderId: String = "study_materials_root",
    val downloadCount: Int = 0,
    val hasChunks: Boolean = false,
    val totalChunks: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["fileId"] = fileId
        map["name"] = name
        map["category"] = category
        map["grade"] = grade
        map["mimeType"] = mimeType
        if (description.isNotBlank()) map["description"] = description
        if (downloadUrl.isNotBlank()) map["downloadUrl"] = downloadUrl
        map["sizeBytes"] = sizeBytes
        if (courseName.isNotBlank()) map["courseName"] = courseName
        if (professor.isNotBlank()) map["professor"] = professor
        if (materialType.isNotBlank()) map["materialType"] = materialType
        if (semester.isNotBlank()) map["semester"] = semester
        if (academicYear.isNotBlank()) map["academicYear"] = academicYear
        if (subject.isNotBlank()) map["subject"] = subject
        map["department"] = department
        map["folderId"] = folderId.ifBlank { "study_materials_root" }
        map["downloadCount"] = downloadCount
        if (hasChunks) map["hasChunks"] = true
        if (totalChunks > 0) map["totalChunks"] = totalChunks
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        return map
    }

    fun toDriveFile(): DriveFile {
        return DriveFile(
            id = fileId.ifBlank { id },
            name = name,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            parentFolderId = folderId.ifBlank { "study_materials_root" },
            category = category,
            description = description,
            webViewLink = downloadUrl.ifBlank { "https://drive.google.com/file/d/$fileId" },
            webContentLink = downloadUrl,
            courseName = courseName,
            professor = professor.ifBlank { "المهندس أحمد عثمان" },
            materialType = materialType.ifBlank { if (name.endsWith(".pdf", ignoreCase = true)) "PDF" else "Docs" },
            semester = semester.ifBlank { "Fall" },
            academicYear = academicYear.ifBlank { "2025-2026" },
            subject = subject.ifBlank { category },
            department = department,
            downloadCount = downloadCount
        )
    }
}

@IgnoreExtraProperties
data class SubjectFolder(
    @DocumentId val id: String = "",
    val folderId: String = "",
    val name: String = "",
    val subject: String = "Python",
    val department: String = "Computer Science",
    val grade: String = "الصف الأول الثانوي",
    val description: String = "",
    val driveFolderUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["folderId"] = folderId.ifBlank { id }
        map["name"] = name
        map["subject"] = subject
        map["department"] = department
        map["grade"] = grade
        map["description"] = description
        if (driveFolderUrl.isNotBlank()) map["driveFolderUrl"] = driveFolderUrl
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        return map
    }
}

@IgnoreExtraProperties
data class StudyAnnouncement(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "المهندس أحمد عثمان",
    val type: String = "exam", // "exam", "course", "urgent", "general"
    val date: String = "",
    val isPinned: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["title"] = title
        map["content"] = content
        map["author"] = author
        map["type"] = type
        map["date"] = date
        map["isPinned"] = isPinned
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        return map
    }
}



