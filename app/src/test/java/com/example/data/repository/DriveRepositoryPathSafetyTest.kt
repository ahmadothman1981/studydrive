package com.example.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the path-safety helpers used by every local file operation
 * (download, preview, delete, content read). These guard against path
 * traversal and filename collisions in the shared downloads directory.
 */
class DriveRepositoryPathSafetyTest {

    @Test
    fun safeFileName_replacesDirectorySeparators() {
        // Separators are replaced with '_', not stripped — result must stay inside one segment.
        val out = DriveRepository.safeFileName("../../etc/passwd")
        assertFalse(out.contains("/"))
        assertFalse(out.contains("\\"))
        assertTrue(out.isNotEmpty())
    }

    @Test
    fun safeFileName_leadingDotsAreTrimmedSoDotDotCannotSurviveAsName() {
        assertEquals("passwd", DriveRepository.safeFileName("..passwd"))
        // ".." itself: separators replaced, leading dots trimmed -> never equals ".."
        assertNotEqualsDotDot(DriveRepository.safeFileName("../.."))
        assertNotEqualsDotDot(DriveRepository.safeFileName(".."))
        assertNotEqualsDotDot(DriveRepository.safeFileName("."))
    }

    private fun assertNotEqualsDotDot(name: String) {
        assertTrue("name must not be a pure traversal marker but was '$name'", name != ".." && name != ".")
    }

    @Test
    fun safeFileName_removesReservedCharacters() {
        val out = DriveRepository.safeFileName("a*b?c\"d<e>f|g.txt")
        assertFalse(out.contains('*'))
        assertFalse(out.contains('?'))
        assertFalse(out.contains('"'))
        assertFalse(out.contains('<'))
        assertFalse(out.contains('>'))
        assertFalse(out.contains('|'))
        assertTrue(out.endsWith(".txt"))
    }

    @Test
    fun safeFileName_capsLengthKeepingExtension() {
        val long = "x".repeat(500) + ".pdf"
        val out = DriveRepository.safeFileName(long)
        assertTrue(out.length <= 150)
        assertTrue(out.endsWith(".pdf"))
    }

    @Test
    fun safeFileName_preservesNormalAndArabicNames() {
        assertEquals("Lecture-1_Algebra.pdf", DriveRepository.safeFileName("Lecture-1_Algebra.pdf"))
        assertEquals("محاضرة_الجبر.pdf", DriveRepository.safeFileName("محاضرة_الجبر.pdf"))
    }

    @Test
    fun safeFileName_neverReturnsBlank() {
        // Even a name made entirely of reserved characters yields a non-blank,
        // single-segment result with no traversal markers.
        val out = DriveRepository.safeFileName("///???***...")
        assertTrue(out.isNotBlank())
        assertFalse(out.contains("/"))
        assertTrue(out != ".." && out != ".")
    }

    @Test
    fun safeSegment_sanitizesIdsToSingleSegment() {
        assertEquals("abc123", DriveRepository.safeSegment("abc123"))
        val evil = DriveRepository.safeSegment("../evil/id")
        assertFalse(evil.contains("/"))
        assertFalse(evil.contains("."))
        // Only truly empty ids fall back to "unknown"; whitespace and other
        // characters are replaced in place (e.g. "   " -> "___"), which is
        // still a safe single path segment.
        assertEquals("unknown", DriveRepository.safeSegment(""))
        assertTrue(DriveRepository.safeSegment("   ").isNotBlank())
        assertFalse(DriveRepository.safeSegment("   ").contains(" "))
    }

    @Test
    fun safeSegment_capsAt100Chars() {
        assertTrue(DriveRepository.safeSegment("a".repeat(300)).length <= 100)
    }

    @Test
    fun distinctIdsMapToDistinctDirectories() {
        // Two materials with the same display name must not collide because each
        // lives under downloads/<safeSegment(id)>/.
        val dirA = DriveRepository.safeSegment("id-material-one")
        val dirB = DriveRepository.safeSegment("id-material-two")
        assertTrue(dirA != dirB)
    }
}
