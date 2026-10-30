package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DriveFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StudyDrive", appName)
  }

  @Test
  fun `verify DriveFile helper properties`() {
    val pdfFile = DriveFile(
      id = "test_1",
      name = "CS101_Lecture_Notes.pdf",
      mimeType = "application/pdf",
      sizeBytes = 2_048_000L
    )
    assertTrue(pdfFile.isPdf)
    assertEquals("2.0 MB", pdfFile.formattedSize)
  }

  @Test
  fun `verify Super Admin role and auto approval`() {
    val superAdmin = com.example.data.model.UserProfile(
      userId = "super_admin_123",
      email = com.example.data.model.SUPER_ADMIN_EMAIL,
      displayName = "Eng. Ahmed Othman",
      status = "approved",
      role = "super_admin"
    )
    assertTrue("Owner must be recognized as Super Admin", superAdmin.isSuperAdmin)
    assertTrue("Super Admin must have admin privileges", superAdmin.isAdmin)
    assertTrue("Super Admin must always be approved", superAdmin.isApproved)
    org.junit.Assert.assertFalse("Super Admin must never be pending", superAdmin.isPending)
  }

  @Test
  fun `verify new student initial pending state`() {
    val student = com.example.data.model.UserProfile(
      userId = "student_456",
      email = "student@gmail.com",
      displayName = "Mohamed Ali",
      status = "pending",
      role = "student"
    )
    org.junit.Assert.assertFalse(student.isSuperAdmin)
    org.junit.Assert.assertFalse(student.isAdmin)
    org.junit.Assert.assertFalse("New student must not be approved initially", student.isApproved)
    assertTrue("New student must be pending approval", student.isPending)

    // Approved student
    val approvedStudent = student.copy(status = "approved")
    assertTrue("Approved student must have isApproved=true", approvedStudent.isApproved)
    org.junit.Assert.assertFalse("Approved student is no longer pending", approvedStudent.isPending)
  }

  @Test
  fun `verify AdminUser model serialization`() {
    val admin = com.example.data.model.AdminUser(
      id = "doc_admin_1",
      email = "assistant.teacher@gmail.com",
      addedBy = com.example.data.model.SUPER_ADMIN_EMAIL,
      role = "admin"
    )
    val map = admin.toMap()
    assertEquals("assistant.teacher@gmail.com", map["email"])
    assertEquals(com.example.data.model.SUPER_ADMIN_EMAIL, map["addedBy"])
    assertEquals("admin", map["role"])
  }

  @Test
  fun `verify student request disappears from pending list when accepted`() {
    val student1 = com.example.data.model.UserProfile(
      userId = "student_1",
      email = "student1@gmail.com",
      displayName = "Student One",
      status = "pending",
      role = "student"
    )
    val student2 = com.example.data.model.UserProfile(
      userId = "student_2",
      email = "student2@gmail.com",
      displayName = "Student Two",
      status = "pending",
      role = "student"
    )
    var studentList = listOf(student1, student2)

    val pendingBefore = studentList.filter { it.status == "pending" && !it.isSuperAdmin }
    assertEquals(2, pendingBefore.size)
    assertTrue(pendingBefore.any { it.userId == "student_1" })

    // Accept student 1
    val statusOverrides = mapOf("student_1" to "approved")
    studentList = studentList.map { student ->
      statusOverrides[student.userId]?.let { student.copy(status = it) } ?: student
    }

    val pendingAfter = studentList.filter { it.status == "pending" && !it.isSuperAdmin }
    val approvedAfter = studentList.filter { it.status == "approved" && !it.isSuperAdmin }

    // Student 1 must completely disappear from pending list
    assertEquals(1, pendingAfter.size)
    org.junit.Assert.assertFalse("Accepted student must disappear from dashboard pending requests", pendingAfter.any { it.userId == "student_1" })
    assertTrue("Accepted student must appear in approved list", approvedAfter.any { it.userId == "student_1" })
  }

  @Test
  fun `verify language manager saves and retrieves language correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Default or initial language
    com.example.ui.locale.LanguageManager.saveLanguage(context, com.example.ui.locale.AppLanguage.ARABIC)
    assertEquals(com.example.ui.locale.AppLanguage.ARABIC, com.example.ui.locale.LanguageManager.getSavedLanguage(context))

    // Toggle to English
    com.example.ui.locale.LanguageManager.saveLanguage(context, com.example.ui.locale.AppLanguage.ENGLISH)
    assertEquals(com.example.ui.locale.AppLanguage.ENGLISH, com.example.ui.locale.LanguageManager.getSavedLanguage(context))

    // Toggle back to Arabic
    com.example.ui.locale.LanguageManager.saveLanguage(context, com.example.ui.locale.AppLanguage.ARABIC)
    assertEquals(com.example.ui.locale.AppLanguage.ARABIC, com.example.ui.locale.LanguageManager.getSavedLanguage(context))
  }

  @Test
  fun `verify Super Admin can edit material details and sync to DriveFile`() {
    val originalMaterial = com.example.data.model.AdminMaterial(
      fileId = "mat_101",
      name = "ملخص_بايثون_القديم.pdf",
      category = "Python",
      description = "شرح قديم"
    )

    // Edit material
    val updatedMaterial = originalMaterial.copy(
      name = "ملخص_بايثون_المطور_2026.pdf",
      category = "Algorithms",
      description = "شرح حديث وشامل"
    )

    assertEquals("ملخص_بايثون_المطور_2026.pdf", updatedMaterial.name)
    assertEquals("Algorithms", updatedMaterial.category)
    assertEquals("شرح حديث وشامل", updatedMaterial.description)

    val driveFile = updatedMaterial.toDriveFile()
    assertEquals("ملخص_بايثون_المطور_2026.pdf", driveFile.name)
    assertEquals("Algorithms", driveFile.category)
    assertEquals("شرح حديث وشامل", driveFile.description)
  }

  @Test
  fun `verify bilingual strings for Super Admin edit and delete operations`() {
    val ar = com.example.ui.locale.ArabicStrings
    val en = com.example.ui.locale.EnglishStrings

    assertEquals("تعديل", ar.edit)
    assertEquals("Edit", en.edit)

    assertEquals("تعديل بيانات المذكرة", ar.editMaterialTitle)
    assertEquals("Edit Material Details", en.editMaterialTitle)

    assertEquals("حذف المذكرة نهائياً من المنصة", ar.deleteMaterialPortal)
    assertEquals("Delete Material from Portal", en.deleteMaterialPortal)

    assertEquals("حذف النسخة من الهاتف فقط", ar.deleteOfflineOnly)
    assertEquals("Delete Offline Copy Only", en.deleteOfflineOnly)
  }
}
