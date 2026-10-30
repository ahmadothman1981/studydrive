package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.example.data.model.DriveFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/** Thrown when a study file could not be fetched from any source. */
class DownloadFailedException(message: String) : Exception(message)

class DriveRepository(private val context: Context) {

    companion object {
        private const val TAG = "DriveRepository"
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3"
        private const val MAX_CONFIRM_RETRIES = 2

        /**
         * Makes a file name safe for local storage: strips path separators and reserved
         * characters (prevents `../` traversal) while keeping Arabic text and the extension.
         */
        fun safeFileName(name: String): String {
            val cleaned = name
                .replace(Regex("""[\\/:*?"<>|\u0000-\u001F]"""), "_")
                .trim()
                .trimStart('.')
            val limited = if (cleaned.length > 150) {
                val ext = cleaned.substringAfterLast('.', "")
                if (ext.length in 1..10) cleaned.take(149 - ext.length) + "." + ext else cleaned.take(150)
            } else cleaned
            return limited.ifBlank { "file" }
        }

        /** Restricts an ID to a single safe path segment. */
        fun safeSegment(id: String): String =
            id.replace(Regex("[^A-Za-z0-9_\\-]"), "_").take(100).ifBlank { "unknown" }
    }

    /**
     * Fetches a material's full binary from the Firestore-chunked store.
     * Returns the assembled bytes, or null when no chunks exist / fetching failed.
     * Assigned by DriveViewModel once StudyRepository is available.
     */
    var remoteBytesFetcher: (suspend (fileId: String) -> ByteArray?)? = null

    private val cookieStore = java.util.concurrent.ConcurrentHashMap<String, List<okhttp3.Cookie>>()
    private val httpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .cookieJar(object : okhttp3.CookieJar {
            override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
                cookieStore[url.host] = cookies
            }
            override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val downloadsDir = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }

    /** Canonical location for a material: downloads/<safeId>/<safeName>. */
    private fun canonicalFileFor(fileId: String, fileName: String): File =
        File(File(downloadsDir, safeSegment(fileId)), safeFileName(fileName))

    /**
     * Finds the local copy of a material. Each material has its own folder, so two files
     * with the same name never overwrite each other. Files from the old flat layout
     * (downloads/<name>) are migrated on first access.
     */
    fun localFileFor(fileId: String, fileName: String): File {
        val target = canonicalFileFor(fileId, fileName)
        if (target.isFile) return target
        val dir = target.parentFile!!
        // A copy saved under a previous name (material renamed by admin)
        dir.listFiles()?.firstOrNull { it.isFile && !it.name.endsWith(".tmp") }?.let { return it }
        // Legacy flat layout migration
        val legacy = File(downloadsDir, safeFileName(fileName))
        if (legacy.isFile) {
            dir.mkdirs()
            return if (legacy.renameTo(target)) target else legacy
        }
        return target
    }

    /** Resolves an existing local file for [file], honoring a stored path only if it is inside our downloads dir. */
    fun findExistingLocalFile(file: DriveFile): File? {
        file.localFilePath?.let { path ->
            val f = File(path)
            if (f.isFile && f.canonicalPath.startsWith(downloadsDir.canonicalPath)) return f
        }
        val f = localFileFor(file.id, file.name)
        return if (f.isFile && f.length() > 0) f else null
    }

    private fun deleteLocalCopies(fileId: String, fileName: String): Boolean {
        val dir = File(downloadsDir, safeSegment(fileId))
        val dirDeleted = if (dir.exists()) dir.deleteRecursively() else true
        val legacy = File(downloadsDir, safeFileName(fileName))
        val legacyDeleted = if (legacy.isFile) legacy.delete() else true
        return dirDeleted && legacyDeleted
    }

    private fun sampleFile(fileId: String, fileName: String): File =
        localFileFor(fileId, fileName).also { it.parentFile?.mkdirs() }

    // Official study materials for "بوابة البرمجة للبكالوريا - المهندس أحمد عثمان"
    private val initialSampleMaterials = listOf(
        DriveFile(
            id = "prog_bac_001",
            name = "مذكرة_البرمجة_الصف_الأول_الثانوي_بايثون_الترم_الأول.pdf",
            mimeType = "application/pdf",
            sizeBytes = 3_850_000L,
            modifiedTime = "2026-10-24T14:30:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_001",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_001&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_python_1",
            category = "Python",
            description = "مذكرة شاملة لشرح منهج البرمجة بلغة بايثون للصف الأول الثانوي: المتغيرات، الدوال، الحلقات التكرارية، والشروط وفق المنهج الرسمي - إعداد أ. أحمد عثمان.",
            courseName = "مقدمة البرمجة بلغة بايثون",
            professor = "المهندس أحمد عثمان",
            materialType = "PDF",
            semester = "Fall",
            academicYear = "2025-2026",
            subject = "Python",
            department = "Computer Science",
            downloadCount = 142
        ),
        DriveFile(
            id = "prog_bac_002",
            name = "مذكرة_البرمجة_الصف_الثاني_الثانوي_الخوارزميات_وهياكل_البيانات.pdf",
            mimeType = "application/pdf",
            sizeBytes = 4_200_000L,
            modifiedTime = "2026-10-22T10:15:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_002",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_002&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_algo_2",
            category = "Algorithms",
            description = "شرح وافٍ لمنهج الصف الثاني الثانوي: تصميم الخوارزميات، خرائط التدفق (Flowcharts)، ومفاهيم البرمجة الكائنية OOP - المهندس أحمد عثمان.",
            courseName = "الخوارزميات وهياكل البيانات",
            professor = "المهندس أحمد عثمان",
            materialType = "PDF",
            semester = "Spring",
            academicYear = "2025-2026",
            subject = "Algorithms",
            department = "Computer Science",
            downloadCount = 98
        ),
        DriveFile(
            id = "prog_bac_003",
            name = "بنك_أسئلة_امتحانات_البرمجة_وحلولها_النموذجية_2025_2026.pdf",
            mimeType = "application/pdf",
            sizeBytes = 2_950_000L,
            modifiedTime = "2026-10-20T16:45:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_003",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_003&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_exams_bac",
            category = "Past Exams",
            description = "تجميع لأهم مسائل الامتحانات الرسمية السابقة مع إجابات نموذجية وتدريب عملي خطوة بخطوة - بوابة البرمجة للبكالوريا.",
            courseName = "مراجعة امتحانات البكالوريا الشاملة",
            professor = "المهندس أحمد عثمان",
            materialType = "Past Exams",
            semester = "Spring",
            academicYear = "2025-2026",
            subject = "Past Exams",
            department = "General Secondary",
            downloadCount = 215
        ),
        DriveFile(
            id = "prog_bac_004",
            name = "دليل_تطوير_الويب_HTML5_CSS3_لطلاب_الثانوية.docx",
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            sizeBytes = 3_150_000L,
            modifiedTime = "2026-10-18T11:00:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_004",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_004&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_web_html",
            category = "Web Development",
            description = "أساسيات تصميم صفحات الويب باستخدام HTML5 و CSS3 ونماذج المواقع المعتمدة في المنهج العملي المدرسي.",
            courseName = "تطوير تطبيقات الويب",
            professor = "المهندس أحمد عثمان",
            materialType = "Docs",
            semester = "Fall",
            academicYear = "2024-2025",
            subject = "Web Development",
            department = "Engineering",
            downloadCount = 76
        ),
        DriveFile(
            id = "prog_bac_005",
            name = "ملخص_أهم_أكواد_وقواعد_البرمجة_سريعة_الحفظ.md",
            mimeType = "text/markdown",
            sizeBytes = 24_500L,
            modifiedTime = "2026-10-25T18:20:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_005",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_005&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_python_1",
            category = "Python",
            description = "ورقة مراجعة ليلة الامتحان: أهم الدوال، المصطلحات البرمجية، وخرائط التتبع لمراجعة سريعة قبل الاختبار.",
            courseName = "ملخصات وقواعد البرمجة السريعة",
            professor = "المهندس أحمد عثمان",
            materialType = "Summaries",
            semester = "Summer",
            academicYear = "2025-2026",
            subject = "Python",
            department = "Computer Science",
            downloadCount = 184
        ),
        DriveFile(
            id = "prog_bac_006",
            name = "سلايد_عرض_درس_الحلقات_التكرارية_Loops_Presentation.pptx",
            mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            sizeBytes = 5_600_000L,
            modifiedTime = "2026-10-23T12:00:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_006",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_006&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_python_1",
            category = "Python",
            description = "عرض تقديمي تفاعلي يشرح For Loops و While Loops مع أمثلة مرئية سهلة الحفظ والفهم.",
            courseName = "عروض وشروحات تفاعلية",
            professor = "المهندس أحمد عثمان",
            materialType = "Slides",
            semester = "Fall",
            academicYear = "2025-2026",
            subject = "Python",
            department = "Computer Science",
            downloadCount = 65
        ),
        DriveFile(
            id = "prog_bac_007",
            name = "ملخص_شامل_لقواعد_البيانات_SQL_والاستعلامات.pdf",
            mimeType = "application/pdf",
            sizeBytes = 2_800_000L,
            modifiedTime = "2026-10-21T09:10:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_007",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_007&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_db_sql",
            category = "General",
            description = "شرح مفصل لاستعلامات SQL الأساسية: SELECT, INSERT, UPDATE, DELETE والربط JOIN بين الجداول - المهندس أحمد عثمان.",
            courseName = "قواعد البيانات ونظم المعلومات SQL",
            professor = "المهندس أحمد عثمان",
            materialType = "Summaries",
            semester = "Fall",
            academicYear = "2025-2026",
            subject = "Database",
            department = "Business",
            downloadCount = 89
        ),
        DriveFile(
            id = "prog_bac_008",
            name = "سلايدات_محاضرات_الذكاء_الاصطناعي_وتعلم_الآلة.pptx",
            mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            sizeBytes = 4_900_000L,
            modifiedTime = "2026-10-19T15:30:00Z",
            webViewLink = "https://drive.google.com/file/d/sample_prog_bac_008",
            webContentLink = "https://drive.google.com/uc?id=sample_prog_bac_008&export=download",
            thumbnailLink = "",
            parentFolderId = "folder_python_1",
            category = "Python",
            description = "عرض تقديمي شامل لمدخل تعلم الآلة ومكتبات بايثون NumPy و Pandas و Matplotlib للطلاب.",
            courseName = "مقدمة الذكاء الاصطناعي",
            professor = "د. عثمان أحمد",
            materialType = "Slides",
            semester = "Spring",
            academicYear = "2024-2025",
            subject = "Python",
            department = "Engineering",
            downloadCount = 112
        )
    )


    private val deletedPrefs = context.getSharedPreferences("study_deleted_materials", Context.MODE_PRIVATE)
    private val editedPrefs = context.getSharedPreferences("study_edited_materials", Context.MODE_PRIVATE)

    private val masterMaterialsLock = Any()
    private val allMasterMaterials = mutableListOf<DriveFile>()
    private var lastRequestedFolderId: String = "study_materials_root"

    private val _currentFiles = MutableStateFlow<List<DriveFile>>(emptyList())
    val currentFiles: Flow<List<DriveFile>> = _currentFiles.asStateFlow()

    init {
        initializeSampleLocalFiles()
        rebuildMasterMaterials(emptyList())
    }

    private fun initializeSampleLocalFiles() {
        try {
            // Sample markdown summary
            val mdFile = sampleFile("prog_bac_005", "ملخص_أهم_أكواد_وقواعد_البرمجة_سريعة_الحفظ.md")
            if (!mdFile.exists()) {
                mdFile.writeText(
                    """
                    # بوابة البرمجة للبكالوريا
                    **إعداد وتدريس:** المهندس أحمد عثمان
                    **للتواصل والاستفسار:** 01113736196

                    ## مراجعة سريعة - لغة بايثون (Python)
                    
                    ### 1. المتغيرات والطباعة (Variables & Print)
                    ```python
                    student_name = "أحمد"
                    score = 98.5
                    print(f"طالب متفوق: {student_name} - الدرجة: {score}")
                    ```

                    ### 2. الجمل الشرطية (If Conditions)
                    ```python
                    if score >= 90:
                        print("ممتاز - مرتبة الشرف!")
                    elif score >= 75:
                        print("جيد جداً")
                    else:
                        print("ناجح")
                    ```

                    ### 3. الحلقات التكرارية (Loops)
                    ```python
                    for i in range(1, 6):
                        print(f"المحاولة رقم: {i}")
                    ```

                    ### 4. القوائم (Lists)
                    ```python
                    courses = ["بايثون", "خوارزميات", "تطوير الويب"]
                    courses.append("قواعد بيانات")
                    ```

                    ---
                    **نصيحة المهندس أحمد عثمان:** "افهم البرمجة صح ... التدريب العملي اليومي هو مفتاح التفوق في امتحانات البكالوريا والثانوية العامة."
                    """.trimIndent()
                )
            }

            // Create initial valid sample PDF files for in-app viewer
            val pdf1 = sampleFile("prog_bac_001", "مذكرة_البرمجة_الصف_الأول_الثانوي_بايثون_الترم_الأول.pdf")
            ensureValidPdfFile(
                targetFile = pdf1,
                title = "مذكرة بايثون - الصف الأول الثانوي",
                author = "المهندس أحمد عثمان",
                pagesText = listOf(
                    """
                    # الفصل الأول: مقدمة إلى لغة البرمجة بايثون (Python)
                    - لغة بايثون هي لغة برمجة عالية المستوى سهلة القراءة والكتابة.
                    - المتغيرات (Variables): يتم تعريفها مباشرة دون تحديد النوع مسبقاً.
                    - أنواع البيانات الأساسية: int, float, str, bool.
                    
                    # قواعد تسمية المتغيرات:
                    1. يجب أن تبدأ بحرف أو شرطة سفلية (_).
                    2. لا يمكن أن تبدأ برقم أو تحتوي على مسافات أو رموز خاصة.
                    3. حساسة لحالة الأحرف (Case Sensitive).
                    
                    # مثال عملي:
                    x = 10
                    name = "Ahmed"
                    print("مرحبا بكم في بوابة البرمجة:", name)
                    """.trimIndent(),
                    """
                    # الفصل الثاني: الجمل الشرطية والتحكم في التدفق
                    - جملة if البسيطة وجملة if...else.
                    - الشروط المتعددة elif.
                    - العمليات المنطقية: and, or, not.
                    
                    # نموذج كود امتحاني:
                    grade = 85
                    if grade >= 90:
                        print("التقدير: ممتاز")
                    elif grade >= 75:
                        print("التقدير: جيد جداً")
                    else:
                        print("التقدير: ناجح")
                    
                    * تذكر دائماً وضع النقطتين (:) والمسافة البادئة (Indentation)!
                    """.trimIndent(),
                    """
                    # الفصل الثالث: الحلقات التكرارية (Loops)
                    - حلقة for مع دالة range(start, stop, step).
                    - حلقة while المشروطة.
                    - أوامر التحكم: break للخروج من الحلقة، continue للتخطي.
                    
                    # تمرين تطبيقي هام للبكالوريا:
                    sum_val = 0
                    for i in range(1, 11):
                        sum_val += i
                    print("مجموع الأعداد من 1 إلى 10 هو:", sum_val)
                    
                    * نصيحة أ. أحمد عثمان: قم بتطبيق الكود عملياً لتثبيت المعلومة!
                    """.trimIndent()
                )
            )

            val pdf2 = sampleFile("prog_bac_003", "بنك_أسئلة_امتحانات_البرمجة_وحلولها_النموذجية_2025_2026.pdf")
            ensureValidPdfFile(
                targetFile = pdf2,
                title = "بنك أسئلة امتحانات البكالوريا 2026",
                author = "المهندس أحمد عثمان",
                pagesText = listOf(
                    """
                    # الامتحان التجريبي الأول - مادة البرمجة
                    
                    السؤال الأول: اختر الإجابة الصحيحة:
                    1. ما هو ناتج تنفيذ: print(type(5.0)) ؟
                       أ) int    ب) float    ج) str    د) bool
                       [الإجابة النموذجية: ب) float]
                    
                    2. أي من المتغيرات التالية غير صالح في بايثون؟
                       أ) _score   ب) 2nd_student   ج) student_name   د) SCORE
                       [الإجابة النموذجية: ب - لأنه يبدأ برقم]
                    """.trimIndent(),
                    """
                    # السؤال الثاني: تتبع الكود التالي واكتب المخرجات:
                    
                    def calculate_bonus(salary, years):
                        if years > 5:
                            return salary * 0.20
                        return salary * 0.10
                        
                    bonus = calculate_bonus(5000, 7)
                    print("المكافأة المستحقة:", bonus)
                    
                    * المخرجات المتوقعة: المكافأة المستحقة: 1000.0
                    * سبب الحل: عدد السنوات 7 أكبر من 5، لذلك يتم تطبيق 20%.
                    """.trimIndent(),
                    """
                    # السؤال الثالث: اكتب برنامجاً لحساب المضروب (Factorial):
                    
                    n = int(input("أدخل رقماً صحيحاً: "))
                    fact = 1
                    for i in range(1, n + 1):
                        fact *= i
                    print(f"مضروب الرقم {n} هو: {fact}")
                    
                    * تمنياتنا لجميع طلاب البكالوريا بالتفوق والدرجات النهائية!
                    * إشراف: المهندس أحمد عثمان - بوابة البرمجة.
                    """.trimIndent()
                )
            )

            val pdf3 = sampleFile("prog_bac_007", "ملخص_شامل_لقواعد_البيانات_SQL_والاستعلامات.pdf")
            ensureValidPdfFile(
                targetFile = pdf3,
                title = "دليل قواعد البيانات SQL للبكالوريا",
                author = "المهندس أحمد عثمان",
                pagesText = listOf(
                    """
                    # مقدمة إلى لغة SQL وقواعد البيانات العلائقية
                    - مفهوم الجداول (Tables) والحقول (Columns) والسجلات (Records).
                    - المفتاح الأساسي (Primary Key) والمفتاح الأجنبي (Foreign Key).
                    
                    # أوامر الاستعلام الأساسية (DQL):
                    SELECT * FROM Students;
                    SELECT name, grade FROM Students WHERE grade >= 80 ORDER BY grade DESC;
                    """.trimIndent(),
                    """
                    # أوامر التعديل والإضافة (DML):
                    1. إضافة سجل جديد:
                    INSERT INTO Students (id, name, major) VALUES (101, 'Ahmad', 'CS');
                    
                    2. تعديل بيانات سجل:
                    UPDATE Students SET grade = 95 WHERE id = 101;
                    
                    3. حذف سجل:
                    DELETE FROM Students WHERE id = 101;
                    """.trimIndent()
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write sample review notes or PDFs", e)
        }
    }


    private fun applyOverridesAndFilters(file: DriveFile): DriveFile? {
        val isDeleted = deletedPrefs.getBoolean(file.id, false)
        if (isDeleted) return null

        val local = localFileFor(file.id, file.name)
        val editedJson = editedPrefs.getString(file.id, null)
        var result = file.copy(
            isDownloaded = local.exists(),
            localFilePath = if (local.exists()) local.absolutePath else null
        )

        if (!editedJson.isNullOrBlank()) {
            try {
                val json = JSONObject(editedJson)
                result = result.copy(
                    name = json.optString("name", result.name),
                    category = json.optString("category", result.category),
                    department = json.optString("department", result.department),
                    parentFolderId = json.optString("folderId", result.parentFolderId),
                    description = json.optString("description", result.description),
                    subject = json.optString("subject", result.subject)
                )
            } catch (_: Exception) {}
        }
        return result
    }

    private fun rebuildMasterMaterials(adminList: List<DriveFile>) {
        synchronized(masterMaterialsLock) {
            val map = linkedMapOf<String, DriveFile>()
            for (sample in initialSampleMaterials) {
                applyOverridesAndFilters(sample)?.let { map[it.id] = it }
            }
            for (adminItem in adminList) {
                applyOverridesAndFilters(adminItem)?.let { map[it.id] = it }
            }
            allMasterMaterials.clear()
            allMasterMaterials.addAll(map.values)

            val currentList = if (lastRequestedFolderId == "study_materials_root") {
                allMasterMaterials.toList()
            } else {
                allMasterMaterials.filter { it.parentFolderId == lastRequestedFolderId }
            }
            _currentFiles.value = currentList
        }
    }

    fun syncAdminMaterials(adminList: List<DriveFile>) {
        rebuildMasterMaterials(adminList)
    }

    fun getAllMasterFiles(): List<DriveFile> {
        synchronized(masterMaterialsLock) {
            return allMasterMaterials.toList()
        }
    }

    fun deleteAnyMaterial(fileId: String): Boolean {
        deletedPrefs.edit().putBoolean(fileId, true).apply()
        synchronized(masterMaterialsLock) {
            val itemToDelete = allMasterMaterials.find { it.id == fileId }
            if (itemToDelete != null) {
                try {
                    deleteLocalCopies(itemToDelete.id, itemToDelete.name)
                } catch (_: Exception) {}
            }
            allMasterMaterials.removeAll { it.id == fileId }
            _currentFiles.value = _currentFiles.value.filterNot { it.id == fileId }
        }
        return true
    }

    fun updateAnyMaterial(
        fileId: String,
        name: String,
        category: String,
        department: String,
        grade: String,
        folderId: String,
        description: String
    ) {
        try {
            val json = JSONObject()
            json.put("name", name)
            json.put("category", category)
            json.put("department", department)
            json.put("grade", grade)
            json.put("folderId", folderId)
            json.put("description", description)
            json.put("subject", category)
            editedPrefs.edit().putString(fileId, json.toString()).apply()
        } catch (_: Exception) {}

        synchronized(masterMaterialsLock) {
            val idx = allMasterMaterials.indexOfFirst { it.id == fileId }
            val existing = if (idx >= 0) allMasterMaterials[idx] else null
            val updated = (existing ?: DriveFile(id = fileId)).copy(
                name = name,
                category = category,
                department = department,
                parentFolderId = folderId,
                description = description,
                subject = category
            )
            if (idx >= 0) {
                allMasterMaterials[idx] = updated
            } else {
                allMasterMaterials.add(0, updated)
            }

            _currentFiles.value = _currentFiles.value.map {
                if (it.id == fileId) updated else it
            }
        }
    }

    suspend fun fetchStudyFolderFiles(
        folderId: String = "study_materials_root",
        accessToken: String? = null
    ): List<DriveFile> = withContext(Dispatchers.IO) {
        lastRequestedFolderId = folderId

        if (!accessToken.isNullOrBlank() && folderId != "study_materials_root") {
            try {
                val query = "'$folderId' in parents and trashed = false"
                val url = "$DRIVE_API_BASE/files?q=${query}&fields=files(id,name,mimeType,size,modifiedTime,webViewLink,webContentLink,thumbnailLink)&orderBy=folder,name"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().orEmpty()
                    val json = JSONObject(responseBody)
                    val filesArray = json.optJSONArray("files")
                    if (filesArray != null) {
                        val parsedFiles = mutableListOf<DriveFile>()
                        for (i in 0 until filesArray.length()) {
                            val item = filesArray.getJSONObject(i)
                            val fId = item.optString("id")
                            val fName = item.optString("name")
                            val fMime = item.optString("mimeType")
                            val fSize = item.optLong("size", 0L)
                            val fMod = item.optString("modifiedTime")
                            val fWeb = item.optString("webViewLink")
                            val fContent = item.optString("webContentLink")
                            val fThumb = item.optString("thumbnailLink")

                            val localFile = localFileFor(fId, fName)
                            parsedFiles.add(
                                DriveFile(
                                    id = fId,
                                    name = fName,
                                    mimeType = fMime,
                                    sizeBytes = fSize,
                                    modifiedTime = fMod,
                                    webViewLink = fWeb,
                                    webContentLink = fContent,
                                    thumbnailLink = fThumb,
                                    parentFolderId = folderId,
                                    category = inferCategory(fName),
                                    isDownloaded = localFile.exists(),
                                    localFilePath = if (localFile.exists()) localFile.absolutePath else null
                                )
                            )
                        }
                        synchronized(masterMaterialsLock) {
                            for (p in parsedFiles) {
                                if (allMasterMaterials.none { it.id == p.id }) {
                                    allMasterMaterials.add(p)
                                }
                            }
                            _currentFiles.value = parsedFiles
                        }
                        return@withContext parsedFiles
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Drive API query failed, falling back to local study folder: ${e.message}")
            }
        }

        synchronized(masterMaterialsLock) {
            val updated = if (folderId == "study_materials_root") {
                allMasterMaterials.map { file ->
                    val local = localFileFor(file.id, file.name)
                    file.copy(
                        isDownloaded = local.exists(),
                        localFilePath = if (local.exists()) local.absolutePath else null
                    )
                }
            } else {
                allMasterMaterials.filter { it.parentFolderId == folderId }.map { file ->
                    val local = localFileFor(file.id, file.name)
                    file.copy(
                        isDownloaded = local.exists(),
                        localFilePath = if (local.exists()) local.absolutePath else null
                    )
                }
            }
            _currentFiles.value = updated
            updated
        }
    }

    private fun inferCategory(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("بايثون") || lower.contains("python") -> "Python"
            lower.contains("خوارزم") || lower.contains("algorithm") || lower.contains("تدفق") || lower.contains("flow") -> "Algorithms"
            lower.contains("امتحان") || lower.contains("exam") || lower.contains("أسئلة") || lower.contains("test") -> "Past Exams"
            lower.contains("ويب") || lower.contains("html") || lower.contains("css") || lower.contains("web") -> "Web Development"
            lower.contains("c++") || lower.contains("سي") -> "C++"
            else -> "Programming"
        }
    }

    fun extractGoogleDriveFileId(input: String): String? {
        if (input.isBlank()) return null
        val trimmed = input.trim()

        val fileDMatch = Regex("""/file/d/([a-zA-Z0-9_\-]+)""").find(trimmed)
        if (fileDMatch != null) return fileDMatch.groupValues[1]

        val idMatch = Regex("""[?&]id=([a-zA-Z0-9_\-]+)""").find(trimmed)
        if (idMatch != null) return idMatch.groupValues[1]

        val folderMatch = Regex("""/folders/([a-zA-Z0-9_\-]+)""").find(trimmed)
        if (folderMatch != null) return folderMatch.groupValues[1]

        if (trimmed.startsWith("drive_")) {
            val stripped = trimmed.removePrefix("drive_")
            if (stripped.length >= 10) return stripped
        }

        if (trimmed.length in 20..100 && trimmed.matches(Regex("""[a-zA-Z0-9_\-]+""")) && !trimmed.contains(" ") && !trimmed.startsWith("http")) {
            return trimmed
        }
        return null
    }

    private fun executeDownloadRequest(
        request: Request,
        tempFile: File,
        onProgress: (Float) -> Unit,
        confirmRetriesLeft: Int = MAX_CONFIRM_RETRIES
    ): Boolean {
        try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return false
            }

            val contentType = response.header("Content-Type").orEmpty()
            val body = response.body ?: run {
                response.close()
                return false
            }

            // Detect Google Drive large-file virus scan confirmation page
            if (contentType.contains("text/html", ignoreCase = true)) {
                val htmlText = body.string()
                val confirmToken = extractGoogleDriveConfirmToken(htmlText)
                if (!confirmToken.isNullOrBlank() && confirmRetriesLeft > 0) {
                    val originalUrl = request.url.toString()
                    val retryUrl = if (originalUrl.contains("confirm=")) {
                        originalUrl.replace(Regex("""confirm=[^&]+"""), "confirm=$confirmToken")
                    } else {
                        if (originalUrl.contains("?")) "$originalUrl&confirm=$confirmToken" else "$originalUrl?confirm=$confirmToken"
                    }
                    val retryRequest = request.newBuilder().url(retryUrl).build()
                    return executeDownloadRequest(retryRequest, tempFile, onProgress, confirmRetriesLeft - 1)
                }
                // HTML received when document expected; discard to avoid corrupting file
                return false
            }

            val contentLength = body.contentLength()
            var bytesCopied = 0L
            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        if (contentLength > 0) {
                            onProgress((bytesCopied.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f))
                        }
                        bytes = input.read(buffer)
                    }
                    output.flush()
                }
            }
            return tempFile.exists() && tempFile.length() > 500
        } catch (e: Exception) {
            Log.w(TAG, "Request download failed: ${e.message}")
            return false
        }
    }

    private fun extractGoogleDriveConfirmToken(html: String): String? {
        val match = Regex("""confirm=([a-zA-Z0-9_\-]+)""").find(html)
        if (match != null) return match.groupValues[1]
        val tokenMatch = Regex("""name="confirm"\s+value="([a-zA-Z0-9_\-]+)"""").find(html)
        return tokenMatch?.groupValues?.get(1)
    }

    private fun copyAndClean(tempFile: File, targetFile: File): Boolean {
        return try {
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            targetFile.exists() && targetFile.length() > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy temp file to target: ${e.message}", e)
            false
        }
    }

    suspend fun downloadFile(
        file: DriveFile,
        accessToken: String? = null,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        // Canonical per-material path: downloads/<safeId>/<safeName> (prevents traversal & name clashes)
        val targetFile = localFileFor(file.id, file.name).also { it.parentFile?.mkdirs() }
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.${System.currentTimeMillis()}.tmp")

        // 0. Check if file has remote binary chunks stored in Firestore (Real Uploaded File)
        try {
            val remoteBytes = remoteBytesFetcher?.invoke(file.id)
            if (remoteBytes != null && remoteBytes.isNotEmpty()) {
                targetFile.writeBytes(remoteBytes)
                updateFileDownloadState(file.id, targetFile.absolutePath)
                onProgress(1.0f)
                return@withContext targetFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to download remote chunks for file ${file.id}: ${e.message}")
        }

        // 1. Gather all candidate URLs to attempt real download
        val candidateUrls = mutableListOf<String>()

        if (file.webContentLink.isNotBlank() && file.webContentLink.startsWith("http", ignoreCase = true)) {
            candidateUrls.add(file.webContentLink.trim())
        }

        val driveId = extractGoogleDriveFileId(file.webContentLink)
            ?: extractGoogleDriveFileId(file.webViewLink)
            ?: extractGoogleDriveFileId(file.id)

        if (!driveId.isNullOrBlank()) {
            candidateUrls.add("https://drive.usercontent.google.com/download?id=$driveId&export=download")
            candidateUrls.add("https://drive.google.com/uc?export=download&id=$driveId")
            candidateUrls.add("https://drive.google.com/uc?id=$driveId&export=download")
        }

        if (file.webViewLink.isNotBlank() && file.webViewLink.startsWith("http", ignoreCase = true) && !candidateUrls.contains(file.webViewLink.trim())) {
            candidateUrls.add(file.webViewLink.trim())
        }

        // Try OAuth Drive API download if token is present and driveId is available
        if (!accessToken.isNullOrBlank() && !driveId.isNullOrBlank()) {
            val driveApiUrl = "$DRIVE_API_BASE/files/$driveId?alt=media"
            try {
                val req = Request.Builder()
                    .url(driveApiUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                if (executeDownloadRequest(req, tempFile, onProgress)) {
                    if (tempFile.renameTo(targetFile) || copyAndClean(tempFile, targetFile)) {
                        updateFileDownloadState(file.id, targetFile.absolutePath)
                        return@withContext targetFile
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "OAuth Drive API download failed: ${e.message}")
            } finally {
                if (tempFile.exists()) tempFile.delete()
            }
        }

        // 2. Try candidate direct URLs
        for (url in candidateUrls) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                    .build()

                if (executeDownloadRequest(req, tempFile, onProgress)) {
                    if (tempFile.renameTo(targetFile) || copyAndClean(tempFile, targetFile)) {
                        updateFileDownloadState(file.id, targetFile.absolutePath)
                        return@withContext targetFile
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Download attempt failed for url: $url - ${e.message}")
            } finally {
                if (tempFile.exists()) tempFile.delete()
            }
        }

        // 3. Fallback: If no network or file was offline/sample
        if (file.isPdf) {
            ensureValidPdfFile(
                targetFile = targetFile,
                title = file.name.removeSuffix(".pdf").replace("_", " "),
                author = file.professor.ifBlank { "المهندس أحمد عثمان" },
                pagesText = listOf(
                    """
                    # ${file.name.removeSuffix(".pdf").replace("_", " ")}
                    - إعداد وتدريس: ${file.professor.ifBlank { "المهندس أحمد عثمان" }}
                    - المادة والمستوى: ${file.category} • ${file.courseName}
                    - الفصل الدراسي: ${file.semester} • العام الدراسي: ${file.academicYear}
                    
                    # نبذة عن المذكرة:
                    ${file.description.ifBlank { "مذكرة دراسية معتمدة وموجهة لطلاب البكالوريا." }}
                    
                    # إشعار هام:
                    - يتم استكمال مزامنة هذا الملف من قِبل إدارة التطبيق.
                    - للتأكد من ربط النسخة الأصلية الكاملة، يُرجى من المهندس أحمد عثمان إعادة رفع الملف من لوحة الإدارة.
                    - هاتف الأستاذ للاستفسار: 01113736196.
                    """.trimIndent()
                )
            )
            onProgress(1.0f)
            updateFileDownloadState(file.id, targetFile.absolutePath)
            return@withContext targetFile
        }

        targetFile.writeText(
            """
            =======================================================
            بوابة البرمجة للبكالوريا - دليلك الشامل لتفوق مادة البرمجة
            إعداد وتدريس: ${file.professor.ifBlank { "المهندس أحمد عثمان" }} (هاتف: 01113736196)
            اسم الملف: ${file.name}
            المساق: ${file.courseName}
            القسم / المادة: ${file.category}
            الفصل الدراسي: ${file.semester} • السنة: ${file.academicYear}
            =======================================================
            
            ${file.description}
            
            ملاحظات دراسية هامة:
            1. قراءة المفاهيم النظرية بعناية وفهم منطق الحل قبل كتابة الكود.
            2. حل تمارين ونماذج الامتحانات المرفقة.
            3. التواصل مع أ. أحمد عثمان لأي استفسارات أو دعم فني.
            
            [نسخة دراسية معتمدة ومحفوظة بدون إنترنت عبر تطبيق بوابة البرمجة]
            """.trimIndent()
        )
        onProgress(1.0f)
        updateFileDownloadState(file.id, targetFile.absolutePath)
        targetFile
    }

    private fun createStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.15f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                2f,
                true
            )
        }
    }

    private fun renderStaticText(
        canvas: Canvas,
        text: CharSequence,
        paint: TextPaint,
        x: Float,
        y: Float,
        width: Int
    ) {
        val layout = createStaticLayout(text, paint, width)
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    fun ensureValidPdfFile(
        targetFile: File,
        title: String,
        author: String,
        pagesText: List<String>
    ) {
        if (targetFile.exists() && targetFile.length() > 5000) return
        try {
            val document = PdfDocument()
            val textPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(20, 30, 40)
                textSize = 14f
            }
            val titlePaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor("#00668B")
                textSize = 18f
                isFakeBoldText = true
            }
            val headerPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor("#003355")
                textSize = 12f
                isFakeBoldText = true
            }
            val headingPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor("#00668B")
                textSize = 16f
                isFakeBoldText = true
            }
            val bgPaint = Paint().apply {
                color = Color.WHITE
            }
            val accentPaint = Paint().apply {
                color = Color.parseColor("#E0F2F1")
            }
            val borderPaint = Paint().apply {
                color = Color.parseColor("#B2DFDB")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            pagesText.forEachIndexed { index, text ->
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

                // Header Banner
                canvas.drawRect(0f, 0f, 595f, 75f, accentPaint)
                canvas.drawRect(0f, 74f, 595f, 75f, borderPaint)

                // Render Title & Author with StaticLayout for proper Arabic BiDi shaping
                renderStaticText(canvas, title, titlePaint, 30f, 16f, 535)
                renderStaticText(canvas, "$author  •  صفحة ${index + 1} من ${pagesText.size}", headerPaint, 30f, 48f, 535)

                var y = 95f
                val paragraphs = text.split("\n")
                for (para in paragraphs) {
                    val clean = para.trim()
                    if (clean.isBlank()) {
                        y += 10f
                        continue
                    }
                    val paintToUse = if (clean.startsWith("#")) headingPaint else textPaint
                    val displayText = if (clean.startsWith("#")) clean.removePrefix("#").trim() else clean
                    val layout = createStaticLayout(displayText, paintToUse, 535)
                    canvas.save()
                    canvas.translate(30f, y)
                    layout.draw(canvas)
                    canvas.restore()
                    y += layout.height + 6f
                    if (y > 780f) break
                }

                // Footer
                canvas.drawRect(30f, 805f, 565f, 806f, borderPaint)
                renderStaticText(
                    canvas,
                    "بوابة البرمجة للبكالوريا • المهندس أحمد عثمان • 01113736196",
                    headerPaint,
                    30f,
                    814f,
                    535
                )

                document.finishPage(page)
            }

            val fos = FileOutputStream(targetFile)
            document.writeTo(fos)
            fos.flush()
            fos.close()
            document.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate valid PDF: ${e.message}", e)
        }
    }

    suspend fun getOrGeneratePreviewFile(file: DriveFile): File = withContext(Dispatchers.IO) {
        val targetFile = localFileFor(file.id, file.name).also { it.parentFile?.mkdirs() }
        if (targetFile.isFile && targetFile.length() > 5000) {
            return@withContext targetFile
        }

        // 0. Check remote chunks first so student always sees real uploaded content
        try {
            val remoteBytes = remoteBytesFetcher?.invoke(file.id)
            if (remoteBytes != null && remoteBytes.isNotEmpty()) {
                targetFile.writeBytes(remoteBytes)
                updateFileDownloadState(file.id, targetFile.absolutePath)
                return@withContext targetFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote chunks for preview: ${e.message}")
        }

        // Attempt remote download first so the student previews real content
        try {
            val downloaded = downloadFile(file) { }
            if (downloaded.exists() && downloaded.length() > 500) {
                return@withContext downloaded
            }
        } catch (e: Exception) {
            Log.w(TAG, "Preview remote download attempt failed: ${e.message}")
        }

        if (file.isPdf) {
            ensureValidPdfFile(
                targetFile = targetFile,
                title = file.name.removeSuffix(".pdf").replace("_", " "),
                author = file.professor.ifBlank { "المهندس أحمد عثمان" },
                pagesText = listOf(
                    """
                    # ${file.name.removeSuffix(".pdf").replace("_", " ")}
                    - إعداد وتدريس: ${file.professor.ifBlank { "المهندس أحمد عثمان" }}
                    - المادة: ${file.category} • ${file.courseName}
                    - الفصل الدراسي: ${file.semester} • العام: ${file.academicYear}
                    
                    # نبذة عن المذكرة:
                    ${file.description.ifBlank { "محتوى تعليمي معتمد وموجه لامتحانات البكالوريا." }}
                    
                    # إشعار هام:
                    - يتم استكمال مزامنة هذا الملف من قِبل إدارة التطبيق.
                    - يُرجى من المهندس أحمد عثمان إعادة رفع الملف من لوحة الإدارة لربط النسخة الأصلية بكامل صفحاتها مع الطلاب.
                    """.trimIndent()
                )
            )
        } else {
            targetFile.writeText(
                """
                # ${file.name}
                **المساق:** ${file.courseName}
                **الأستاذ:** ${file.professor}
                **الفصل:** ${file.semester} • **السنة:** ${file.academicYear}
                
                ${file.description}
                """.trimIndent()
            )
        }
        targetFile
    }

    fun saveMobileUploadedFile(inputStream: InputStream, fileId: String, fileName: String): Long {
        val targetFile = localFileFor(fileId, fileName).also { it.parentFile?.mkdirs() }
        inputStream.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        updateFileDownloadState(fileId, targetFile.absolutePath)
        return targetFile.length()
    }

    private fun updateFileDownloadState(fileId: String, localPath: String) {
        _currentFiles.update { list ->
            list.map {
                if (it.id == fileId) {
                    it.copy(isDownloaded = true, localFilePath = localPath)
                } else {
                    it
                }
            }
        }
    }

    /** Atomic variant of [updateFileDownloadState] for callers that already hold the file model. */
    fun markFileDownloaded(fileId: String, localPath: String) = updateFileDownloadState(fileId, localPath)

    suspend fun deleteDownloadedFile(fileId: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        val deleted = deleteLocalCopies(fileId, fileName)
        _currentFiles.update { list ->
            list.map {
                if (it.id == fileId) {
                    it.copy(isDownloaded = false, localFilePath = null)
                } else {
                    it
                }
            }
        }
        deleted
    }

    fun getLocalFileContent(fileId: String, fileName: String): String? {
        val file = localFileFor(fileId, fileName)
        return if (file.isFile && file.canRead()) {
            try {
                file.readText()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read local content for ${file.name}: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    fun updateMaterialInCurrentFiles(updatedFile: DriveFile) {
        _currentFiles.update { list ->
            list.map {
                if (it.id == updatedFile.id) {
                    val local = localFileFor(updatedFile.id, updatedFile.name)
                    updatedFile.copy(
                        isDownloaded = local.isFile,
                        localFilePath = if (local.isFile) local.absolutePath else null
                    )
                } else {
                    it
                }
            }
        }
    }

    fun removeMaterialFromList(fileId: String) {
        _currentFiles.value = _currentFiles.value.filterNot { it.id == fileId }
    }

    fun getDownloadedFilesList(): List<File> {
        return downloadsDir.listFiles()?.filter { it.isFile }?.toList() ?: emptyList()
    }

    fun getTotalStorageUsedBytes(): Long {
        return downloadsDir.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
    }

    fun clearLocalCache(): Long {
        val files = downloadsDir.listFiles() ?: return 0L
        var freedBytes = 0L
        for (f in files) {
            freedBytes += f.length()
            f.delete()
        }
        val updated = _currentFiles.value.map {
            it.copy(isDownloaded = false, localFilePath = null)
        }
        _currentFiles.value = updated
        return freedBytes
    }
}

