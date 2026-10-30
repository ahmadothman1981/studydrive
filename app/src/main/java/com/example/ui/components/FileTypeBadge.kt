package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriveFile

@Composable
fun FileTypeIcon(
    file: DriveFile,
    modifier: Modifier = Modifier,
    iconSize: Int = 24,
    containerSize: Int = 44
) {
    val (icon: ImageVector, bgColor: Color, iconColor: Color) = when {
        file.isFolder -> Triple(Icons.Default.Folder, Color(0xFFFFF3E0), Color(0xFFE65100))
        file.isPdf -> Triple(Icons.Default.PictureAsPdf, Color(0xFFFFEBEE), Color(0xFFD32F2F))
        file.isDocument -> Triple(Icons.Default.Description, Color(0xFFE3F2FD), Color(0xFF1976D2))
        file.isSpreadsheet -> Triple(Icons.Default.TableChart, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        file.isPresentation -> Triple(Icons.Default.Slideshow, Color(0xFFFFF8E1), Color(0xFFF57F17))
        file.isImage -> Triple(Icons.Default.Image, Color(0xFFEDE7F6), Color(0xFF5E35B1))
        file.isTextOrCode -> Triple(Icons.Default.Code, Color(0xFFECEFF1), Color(0xFF37474F))
        else -> Triple(Icons.Default.Description, Color(0xFFF5F5F5), Color(0xFF616161))
    }

    Box(
        modifier = modifier
            .size(containerSize.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = file.mimeType,
            tint = iconColor,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    val chipColor = when (category) {
        "Computer Science" -> Color(0xFF1565C0)
        "Mathematics" -> Color(0xFF00897B)
        "Physics" -> Color(0xFF6A1B9A)
        "Chemistry" -> Color(0xFFC2185B)
        "Biology" -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = chipColor.copy(alpha = 0.12f)
    ) {
        Text(
            text = category,
            color = chipColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
