package com.example.data.model

data class DriveFile(
    val id: String = "",
    val name: String = "",
    val mimeType: String = "",
    val sizeBytes: Long = 0L,
    val modifiedTime: String = "",
    val webViewLink: String = "",
    val webContentLink: String = "",
    val thumbnailLink: String = "",
    val parentFolderId: String = "",
    val category: String = "General",
    val description: String = "",
    val isDownloaded: Boolean = false,
    val isBookmarked: Boolean = false,
    val localFilePath: String? = null,
    val courseName: String = "",
    val professor: String = "المهندس أحمد عثمان",
    val materialType: String = "PDF", // "PDF", "Slides", "Past Exams", "Summaries", "Docs"
    val semester: String = "Fall",    // "Fall", "Spring", "Summer"
    val academicYear: String = "2025-2026",
    val subject: String = "",
    val department: String = "Computer Science", // "Computer Science", "Engineering", "Business", "Medicine", "General Secondary"
    val downloadCount: Int = 0
) {
    val isFolder: Boolean
        get() = mimeType == "application/vnd.google-apps.folder"

    val isPdf: Boolean
        get() = mimeType.contains("pdf", ignoreCase = true) || name.endsWith(".pdf", ignoreCase = true)

    val isDocument: Boolean
        get() = mimeType.contains("document") || mimeType.contains("word") ||
                name.endsWith(".doc", ignoreCase = true) || name.endsWith(".docx", ignoreCase = true) ||
                mimeType.contains("google-apps.document")

    val isSpreadsheet: Boolean
        get() = mimeType.contains("sheet") || mimeType.contains("excel") ||
                name.endsWith(".xls", ignoreCase = true) || name.endsWith(".xlsx", ignoreCase = true) ||
                mimeType.contains("google-apps.spreadsheet")

    val isPresentation: Boolean
        get() = mimeType.contains("presentation") || mimeType.contains("powerpoint") ||
                name.endsWith(".ppt", ignoreCase = true) || name.endsWith(".pptx", ignoreCase = true) ||
                mimeType.contains("google-apps.presentation")

    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val isTextOrCode: Boolean
        get() = mimeType.startsWith("text/") || mimeType.contains("json") ||
                mimeType.contains("javascript") || mimeType.contains("python") ||
                name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".py") ||
                name.endsWith(".kt") || name.endsWith(".java") || name.endsWith(".c")

    val formattedSize: String
        get() {
            if (isFolder) return "Folder"
            if (sizeBytes <= 0) return "--"
            val kb = sizeBytes / 1024.0
            if (kb < 1024) return "%.1f KB".format(kb)
            val mb = kb / 1024.0
            return "%.1f MB".format(mb)
        }
}
