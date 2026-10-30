package com.example.security

import android.net.Uri

/**
 * Validates external links so only HTTPS URLs pointing to trusted Google Drive / Docs hosts
 * can be opened via Intent.ACTION_VIEW. Rejects cleartext HTTP, javascript:, file:, content:,
 * intent:, and arbitrary third-party domains.
 */
object SafeUrl {
    private val allowedHosts = setOf(
        "drive.google.com",
        "docs.google.com",
        "drive.usercontent.google.com"
    )

    private val bareDriveId = Regex("^[A-Za-z0-9_\\-]{10,128}$")

    fun toSafeUri(raw: String?): Uri? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty() || value.length > 500) return null
        if (bareDriveId.matches(value)) {
            return Uri.parse("https://drive.google.com/file/d/$value/view")
        }
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        val host = uri.host?.lowercase() ?: return null
        return if (host in allowedHosts) uri else null
    }
}
