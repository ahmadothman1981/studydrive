package com.example.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Security tests for SafeUrl: only HTTPS URLs on trusted Google hosts (or bare
 * Drive ids) may be opened via ACTION_VIEW. javascript:, file:, content:,
 * intent:, cleartext http, foreign hosts, and oversized input must all be rejected.
 */
@RunWith(RobolectricTestRunner::class)
class SafeUrlTest {

    @Test
    fun allowsPlainHttpsGoogleDriveLink() {
        val uri = SafeUrl.toSafeUri("https://drive.google.com/file/d/abc123/view")
        assertEquals("https", uri?.scheme)
        assertEquals("drive.google.com", uri?.host)
    }

    @Test
    fun convertsBareDriveIdToTrustedUrl() {
        val uri = SafeUrl.toSafeUri("1AbcDEF-_9xkLmNoPqRsTuVwXyZ01234")
        assertEquals("https", uri?.scheme)
        assertEquals("drive.google.com", uri?.host)
    }

    @Test
    fun blocksJavascriptScheme() {
        assertNull(SafeUrl.toSafeUri("javascript:alert(document.cookie)"))
        assertNull(SafeUrl.toSafeUri("JavaScript:alert(1)"))
        assertNull(SafeUrl.toSafeUri("  javascript:alert(1)"))
    }

    @Test
    fun blocksFileContentIntentAndAboutSchemes() {
        assertNull(SafeUrl.toSafeUri("file:///data/data/com.example/files/secret.pdf"))
        assertNull(SafeUrl.toSafeUri("content://com.example.provider/files/x"))
        assertNull(SafeUrl.toSafeUri("intent://scan/#Intent;scheme=x;end"))
        assertNull(SafeUrl.toSafeUri("about:blank"))
    }

    @Test
    fun blocksCleartextHttp() {
        assertNull(SafeUrl.toSafeUri("http://drive.google.com/file/d/abc/view"))
    }

    @Test
    fun blocksForeignHostsEvenOverHttps() {
        assertNull(SafeUrl.toSafeUri("https://evil.example.com/phish"))
        assertNull(SafeUrl.toSafeUri("https://drive.google.com.evil.example.com/x"))
        // userinfo trick: real host is evil.example.com
        assertNull(SafeUrl.toSafeUri("https://drive.google.com@evil.example.com/x"))
    }

    @Test
    fun blocksMissingSchemeBlankAndMalformed() {
        assertNull(SafeUrl.toSafeUri(""))
        assertNull(SafeUrl.toSafeUri("   "))
        assertNull(SafeUrl.toSafeUri(null))
        assertNull(SafeUrl.toSafeUri("drive.google.com/file/d/abc"))
    }

    @Test
    fun blocksOversizedUrls() {
        val huge = "https://drive.google.com/f?x=" + "a".repeat(5000)
        assertNull(SafeUrl.toSafeUri(huge))
    }
}
