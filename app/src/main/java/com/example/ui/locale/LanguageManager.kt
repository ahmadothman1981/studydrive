package com.example.ui.locale

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppLanguage(val code: String, val title: String, val toggleLabel: String) {
    ARABIC("ar", "العربية", "English"),
    ENGLISH("en", "English", "العربية")
}

data class AppStrings(
    // App Bar & Main
    val appTitle: String,
    val appSubtitle: String,
    val teacherName: String,
    val connectedFolderPrefix: String,

    // Navigation Tabs
    val tabMaterials: String,
    val tabDownloads: String,
    val tabStarred: String,
    val tabAdmin: String,

    // Categories
    val catAll: String,
    val catPython: String,
    val catAlgorithms: String,
    val catPastExams: String,
    val catWebDev: String,
    val catGeneral: String,

    // Search and Materials
    val searchPlaceholder: String,
    val filterAll: String,
    val download: String,
    val downloading: String,
    val downloaded: String,
    val offlineReady: String,
    val bookmark: String,
    val bookmarked: String,
    val open: String,
    val share: String,
    val delete: String,
    val viewDetails: String,
    val previewInApp: String,
    val editMaterialAction: String,
    val deleteMaterialAction: String,
    val downloadAction: String,
    val bookmarkAction: String,
    val noMaterialsFound: String,
    val noMaterialsDesc: String,
    val filterByType: String,
    val filterBySemester: String,
    val filterByYear: String,
    val filterByCourse: String,
    val filterByProfessor: String,
    val clearFilters: String,
    val semesterFall: String,
    val semesterSpring: String,
    val semesterSummer: String,
    val typePdf: String,
    val typeSlides: String,
    val typePastExams: String,
    val typeSummaries: String,
    val typeDocs: String,
    val activeFiltersLabel: String,

    // Downloads Screen
    val downloadsTitle: String,
    val storageUsed: String,
    val noDownloads: String,
    val noDownloadsDesc: String,
    val deleteFromDevice: String,

    // Bookmarks Screen
    val bookmarksTitle: String,
    val noBookmarks: String,
    val noBookmarksDesc: String,

    // Admin Dashboard
    val adminTitle: String,
    val superAdminBadge: String,
    val adminBadge: String,
    val tabPendingStudents: String,
    val tabAdminMaterials: String,
    val tabManageAdmins: String,
    val statPendingRequests: String,
    val statApprovedStudents: String,
    val statMaterials: String,
    val pendingSectionTitle: String,
    val approvedSectionTitle: String,
    val noPendingRequests: String,
    val noPendingDesc: String,
    val noApprovedStudents: String,
    val approveAndActivate: String,
    val reject: String,
    val revokeStudent: String,
    val newStudent: String,
    val approvedStudent: String,
    val addAdminTitle: String,
    val addAdminDesc: String,
    val addAdminButton: String,
    val adminEmailHint: String,
    val adminsListTitle: String,
    val removeAdmin: String,
    val addMaterialTitle: String,

    // Pending Approval Screen
    val pendingReviewTitle: String,
    val pendingReviewDesc: String,
    val rejectedTitle: String,
    val rejectedDesc: String,
    val whatsAppContact: String,
    val whatsAppDesc: String,
    val checkStatus: String,
    val signOut: String,

    // Profile Dialog
    val studentAccount: String,
    val authenticatedWithGoogle: String,
    val roleSuperAdmin: String,
    val roleAdmin: String,
    val roleStudent: String,
    val statusApproved: String,
    val statusPending: String,
    val languageLabel: String,
    val switchLanguage: String,

    // Super Admin Material Edit & Delete in Downloads
    val edit: String,
    val editMaterialTitle: String,
    val materialName: String,
    val materialCategory: String,
    val materialDescription: String,
    val saveChanges: String,
    val cancel: String,
    val deleteMaterialPortal: String,
    val deleteOfflineOnly: String,
    val deleteConfirmationTitle: String,
    val deleteConfirmationDesc: String,

    // Snackbars
    val snackbarStudentApproved: String,
    val snackbarStudentRejected: String,
    val snackbarStudentRevoked: String,
    val snackbarAdminAdded: String,
    val snackbarAdminRemoved: String,
    val snackbarMaterialDeleted: String,
    val snackbarMaterialAdded: String,
    val snackbarFolderConnected: String,
    val snackbarMaterialUpdated: String,
    val snackbarMaterialDeletedFromPortal: String
)

val ArabicStrings = AppStrings(
    appTitle = "بوابة البرمجة للبكالوريا",
    appSubtitle = "منصة مذكرات ودروس البرمجة لطلاب البكالوريا",
    teacherName = "المهندس أحمد عثمان • بكالوريا تونس",
    connectedFolderPrefix = "المجلد المتصل:",

    tabMaterials = "المذكرات",
    tabDownloads = "المحملة",
    tabStarred = "المفضلة",
    tabAdmin = "لوحة الإدارة",

    catAll = "الكل",
    catPython = "بايثون (Python)",
    catAlgorithms = "الخوارزميات",
    catPastExams = "امتحانات سابقة",
    catWebDev = "تطوير الويب",
    catGeneral = "عام وتوجيهات",

    searchPlaceholder = "البحث في المذكرات والملفات...",
    filterAll = "الكل",
    download = "تحميل",
    downloading = "جاري التحميل...",
    downloaded = "تم التحميل",
    offlineReady = "جاهز للقراءة بدون إنترنت",
    bookmark = "إضافة للمفضلة",
    bookmarked = "في المفضلة",
    open = "فتح الملف",
    share = "مشاركة",
    delete = "حذف",
    viewDetails = "عرض التفاصيل",
    previewInApp = "معاينة داخل التطبيق",
    editMaterialAction = "تعديل المذكرة",
    deleteMaterialAction = "حذف المذكرة",
    downloadAction = "تحميل الملف",
    bookmarkAction = "إضافة للمفضلة",
    noMaterialsFound = "لم يتم العثور على مذكرات",
    noMaterialsDesc = "جرب البحث بكلمات أخرى أو اختر تصنيفاً مختلفاً",
    filterByType = "نوع الملف",
    filterBySemester = "الفصل الدراسي",
    filterByYear = "السنة الدراسية",
    filterByCourse = "المساق الدراسي",
    filterByProfessor = "المدرس",
    clearFilters = "مسح الفلاتر",
    semesterFall = "الترم الأول (Fall)",
    semesterSpring = "الترم الثاني (Spring)",
    semesterSummer = "دورة صيفية (Summer)",
    typePdf = "ملفات PDF",
    typeSlides = "سلايدات وعروض",
    typePastExams = "امتحانات سابقة",
    typeSummaries = "ملخصات سريعة",
    typeDocs = "مستندات وورد",
    activeFiltersLabel = "تصفية نشطة",

    downloadsTitle = "الملفات المحملة على الهاتف",
    storageUsed = "المساحة المستهلكة",
    noDownloads = "لا توجد ملفات محملة بعد",
    noDownloadsDesc = "اضغط على أيقونة التحميل بجانب المذكرات لقراءتها بدون إنترنت",
    deleteFromDevice = "حذف النسخة من الهاتف",

    bookmarksTitle = "المذكرات المفضلة والمهمة",
    noBookmarks = "لا توجد مذكرات مفضلة بعد",
    noBookmarksDesc = "اضغط على علامة النجمة لحفظ المذكرات الهامة للرجوع السريع إليها",

    adminTitle = "لوحة إدارة الطلاب والمذكرات",
    superAdminBadge = "المالك والمسؤول العام (Super Admin)",
    adminBadge = "مشرف معتمد (Admin)",
    tabPendingStudents = "طلبات التفعيل",
    tabAdminMaterials = "المذكرات والدروس",
    tabManageAdmins = "إدارة المشرفين",
    statPendingRequests = "طلبات التفعيل",
    statApprovedStudents = "الطلاب المفعلين",
    statMaterials = "المذكرات",
    pendingSectionTitle = "طلبات انتظار التفعيل",
    approvedSectionTitle = "الطلاب المعتمدين والمفعلين",
    noPendingRequests = "لا توجد طلبات معلقة حالياً",
    noPendingDesc = "جميع الطلاب المسجلين تم اعتمادهم أو لا توجد تسجيلات جديدة.",
    noApprovedStudents = "لا يوجد طلاب مفعلين بعد.",
    approveAndActivate = "قبول وتفعيل",
    reject = "رفض",
    revokeStudent = "إلغاء التفعيل",
    newStudent = "طالب جديد",
    approvedStudent = "طالب معتمد",
    addAdminTitle = "إضافة مشرف جديد",
    addAdminDesc = "إعطاء صلاحيات إدارة المذكرات والطلاب لمشرف جديد",
    addAdminButton = "إضافة مشرف",
    adminEmailHint = "البريد الإلكتروني للمشرف (Gmail)",
    adminsListTitle = "قائمة المشرفين المعتمدين",
    removeAdmin = "إزالة المشرف",
    addMaterialTitle = "إضافة ونشر مذكرة جديدة",

    pendingReviewTitle = "حسابك قيد المراجعة والاعتماد",
    pendingReviewDesc = "مرحباً بك! تم تسجيل حسابك بنجاح، وهو الآن بانتظار موافقة المهندس أحمد عثمان (Super Admin) لمنحك صلاحية الوصول للمذكرات والدروس.",
    rejectedTitle = "تم رفض الحساب",
    rejectedDesc = "تم رفض طلب الوصول الخاص بحسابك. يمكنك التواصل مع المهندس أحمد عثمان للاستفسار والمساعدة.",
    whatsAppContact = "تواصل عبر واتساب للتفعيل السريع",
    whatsAppDesc = "تواصل مباشرة مع المهندس أحمد عثمان لطلب تفعيل فوري لحسابك",
    checkStatus = "تحديث حالة الحساب",
    signOut = "تسجيل الخروج",

    studentAccount = "حساب الطالب",
    authenticatedWithGoogle = "تم الدخول بحساب Google",
    roleSuperAdmin = "المالك والمشرف العام",
    roleAdmin = "مشرف معتمد",
    roleStudent = "طالب",
    statusApproved = "مفعّل ومعتمد",
    statusPending = "قيد الانتظار والمراجعة",
    languageLabel = "لغة التطبيق",
    switchLanguage = "English",

    edit = "تعديل",
    editMaterialTitle = "تعديل بيانات المذكرة",
    materialName = "اسم المذكرة أو الملف",
    materialCategory = "التصنيف",
    materialDescription = "الوصف والملاحظات",
    saveChanges = "حفظ التعديلات",
    cancel = "إلغاء",
    deleteMaterialPortal = "حذف المذكرة نهائياً من المنصة",
    deleteOfflineOnly = "حذف النسخة من الهاتف فقط",
    deleteConfirmationTitle = "خيارات الحذف للمسؤول",
    deleteConfirmationDesc = "بصفتك المسؤول العام، يمكنك حذف النسخة المحفوظة على هذا الهاتف فقط، أو حذف المذكرة نهائياً من المنصة لجميع الطلاب.",

    snackbarStudentApproved = "تم قبول وتفعيل حساب الطالب بنجاح!",
    snackbarStudentRejected = "تم رفض حساب الطالب",
    snackbarStudentRevoked = "تم إلغاء تفعيل حساب الطالب",
    snackbarAdminAdded = "تمت إضافة المشرف بنجاح!",
    snackbarAdminRemoved = "تمت إزالة المشرف بنجاح",
    snackbarMaterialDeleted = "تم حذف المذكرة من لوحة الإدارة",
    snackbarMaterialAdded = "تمت إضافة ونشر المذكرة بنجاح!",
    snackbarFolderConnected = "تم الاتصال بالمجلد بنجاح",
    snackbarMaterialUpdated = "تم تحديث بيانات المذكرة بنجاح!",
    snackbarMaterialDeletedFromPortal = "تم حذف المذكرة نهائياً من المنصة"
)

val EnglishStrings = AppStrings(
    appTitle = "Baccalaureate Study Portal",
    appSubtitle = "Programming Study Notes & Lessons for Baccalaureate",
    teacherName = "Eng. Ahmad Othman • Baccalaureate Tunisia",
    connectedFolderPrefix = "Active Folder:",

    tabMaterials = "Materials",
    tabDownloads = "Downloads",
    tabStarred = "Favorites",
    tabAdmin = "Admin Panel",

    catAll = "All",
    catPython = "Python",
    catAlgorithms = "Algorithms",
    catPastExams = "Past Exams",
    catWebDev = "Web Development",
    catGeneral = "General & Guidelines",

    searchPlaceholder = "Search materials & lessons...",
    filterAll = "All",
    download = "Download",
    downloading = "Downloading...",
    downloaded = "Downloaded",
    offlineReady = "Ready to read offline",
    bookmark = "Bookmark",
    bookmarked = "Bookmarked",
    open = "Open File",
    share = "Share",
    delete = "Delete",
    viewDetails = "View Details",
    previewInApp = "In-App Preview",
    editMaterialAction = "Edit material",
    deleteMaterialAction = "Delete material",
    downloadAction = "Download file",
    bookmarkAction = "Bookmark",
    noMaterialsFound = "No materials found",
    noMaterialsDesc = "Try searching with different keywords or select another category.",
    filterByType = "File Type",
    filterBySemester = "Semester",
    filterByYear = "Academic Year",
    filterByCourse = "Course",
    filterByProfessor = "Professor",
    clearFilters = "Clear Filters",
    semesterFall = "Fall Semester",
    semesterSpring = "Spring Semester",
    semesterSummer = "Summer Term",
    typePdf = "PDF Files",
    typeSlides = "Slides & Decks",
    typePastExams = "Past Exams",
    typeSummaries = "Summaries & Cheatsheets",
    typeDocs = "Word Docs",
    activeFiltersLabel = "Active Filters",

    downloadsTitle = "Offline Downloaded Files",
    storageUsed = "Storage Used",
    noDownloads = "No downloaded files yet",
    noDownloadsDesc = "Tap the download icon beside any material to access it offline.",
    deleteFromDevice = "Delete offline copy",

    bookmarksTitle = "Starred & Bookmarked Materials",
    noBookmarks = "No bookmarks yet",
    noBookmarksDesc = "Tap the star icon on any material to save it for quick reference.",

    adminTitle = "Students & Materials Admin Dashboard",
    superAdminBadge = "Owner & Super Admin",
    adminBadge = "Authorized Admin",
    tabPendingStudents = "Pending Requests",
    tabAdminMaterials = "Study Materials",
    tabManageAdmins = "Manage Admins",
    statPendingRequests = "Pending",
    statApprovedStudents = "Approved",
    statMaterials = "Materials",
    pendingSectionTitle = "Pending Student Requests",
    approvedSectionTitle = "Approved Active Students",
    noPendingRequests = "No pending requests at this time",
    noPendingDesc = "All registered students have been approved or there are no new requests.",
    noApprovedStudents = "No approved students yet.",
    approveAndActivate = "Accept & Activate",
    reject = "Reject",
    revokeStudent = "Revoke Access",
    newStudent = "New Student",
    approvedStudent = "Approved Student",
    addAdminTitle = "Add New Admin",
    addAdminDesc = "Grant lesson and student management privileges to a new admin",
    addAdminButton = "Add Admin",
    adminEmailHint = "Admin Email Address (Gmail)",
    adminsListTitle = "Authorized Admins List",
    removeAdmin = "Remove Admin",
    addMaterialTitle = "Upload & Publish New Material",

    pendingReviewTitle = "Account Under Review & Approval",
    pendingReviewDesc = "Welcome! Your account is registered and waiting for approval from Eng. Ahmad Othman (Super Admin) to access study materials and lessons.",
    rejectedTitle = "Account Request Rejected",
    rejectedDesc = "Your access request has been rejected. You can contact Eng. Ahmad Othman for assistance and questions.",
    whatsAppContact = "Contact on WhatsApp for Quick Activation",
    whatsAppDesc = "Directly message Eng. Ahmad Othman for immediate activation of your account",
    checkStatus = "Refresh Status",
    signOut = "Sign Out",

    studentAccount = "Student Account",
    authenticatedWithGoogle = "Signed in with Google",
    roleSuperAdmin = "Owner & Super Admin",
    roleAdmin = "Authorized Admin",
    roleStudent = "Student",
    statusApproved = "Approved & Active",
    statusPending = "Pending Review",
    languageLabel = "App Language",
    switchLanguage = "العربية",

    edit = "Edit",
    editMaterialTitle = "Edit Material Details",
    materialName = "Material / File Name",
    materialCategory = "Category",
    materialDescription = "Description & Notes",
    saveChanges = "Save Changes",
    cancel = "Cancel",
    deleteMaterialPortal = "Delete Material from Portal",
    deleteOfflineOnly = "Delete Offline Copy Only",
    deleteConfirmationTitle = "Super Admin Delete Options",
    deleteConfirmationDesc = "As Super Admin, you can choose to delete only the offline copy on this device, or permanently delete the material from the portal for all students.",

    snackbarStudentApproved = "Student approved and activated successfully!",
    snackbarStudentRejected = "Student request rejected",
    snackbarStudentRevoked = "Student access revoked",
    snackbarAdminAdded = "Admin added successfully!",
    snackbarAdminRemoved = "Admin removed successfully",
    snackbarMaterialDeleted = "Material deleted from dashboard",
    snackbarMaterialAdded = "Material added and published successfully!",
    snackbarFolderConnected = "Connected to folder successfully",
    snackbarMaterialUpdated = "Material updated successfully!",
    snackbarMaterialDeletedFromPortal = "Material permanently deleted from portal"
)

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ARABIC }
val LocalAppStrings = staticCompositionLocalOf { ArabicStrings }

object LanguageManager {
    private const val PREFS_NAME = "studydrive_language_prefs"
    private const val KEY_LANG = "key_selected_language"

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANG, AppLanguage.ARABIC.code)
        return if (code == AppLanguage.ENGLISH.code) AppLanguage.ENGLISH else AppLanguage.ARABIC
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language.code).apply()
    }
}

@Composable
fun LanguageToggleButton(
    currentLanguage: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = modifier
            .height(34.dp)
            .testTag("language_toggle_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Switch Language",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentLanguage.toggleLabel,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
