package com.example.ui.studydrive

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DriveFile
import com.example.ui.components.CategoryChip
import com.example.ui.components.FileTypeIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DocumentViewerDialog(
    file: DriveFile,
    isBookmarked: Boolean,
    bookmarkNotes: String,
    downloadProgress: Float?,
    resolveLocalFile: suspend (DriveFile) -> File,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onShare: (Context) -> Unit,
    onOpenExternal: (Context) -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.ui.locale.LocalAppStrings.current
    var isFullscreen by remember { mutableStateOf(false) }
    var loadedFile by remember { mutableStateOf<File?>(null) }
    var isLoadingFile by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(file.id) {
        isLoadingFile = true
        try {
            val f = withContext(Dispatchers.IO) {
                resolveLocalFile(file)
            }
            loadedFile = f
        } catch (e: Exception) {
            Log.e("DocumentViewer", "Failed to resolve file: ${e.message}", e)
        } finally {
            isLoadingFile = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("document_viewer_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Toolbar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_viewer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Viewer",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            FileTypeIcon(file = file, iconSize = 20, containerSize = 36)

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${file.courseName.ifBlank { file.category }} • ${file.professor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showInfoSheet = !showInfoSheet },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Document Info",
                                    tint = if (showInfoSheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onToggleBookmark(bookmarkNotes) },
                                modifier = Modifier.size(36.dp).testTag("viewer_bookmark_button")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Info banner overlay when toggled
                AnimatedVisibility(visible = showInfoSheet) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "تفاصيل المستند والمقرر",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المساق الدراسي:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(file.courseName.ifBlank { "بوابة البرمجة" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("أستاذ المادة:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(file.professor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الفصل والسنة:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${file.semester} • ${file.academicYear}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            if (file.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = file.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Main Viewer Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingFile) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري تهيئة المعاينة السريعة للمستند...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (loadedFile != null && file.isPdf) {
                        NativePdfPageViewer(
                            pdfFile = loadedFile!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (file.isTextOrCode || file.name.endsWith(".md", ignoreCase = true)) {
                        MarkdownOrCodePreview(
                            file = file,
                            localFile = loadedFile,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (file.isPresentation) {
                        SlidePresentationViewer(
                            file = file,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // General Document summary & notes viewer
                        GeneralDocumentViewer(
                            file = file,
                            localFile = loadedFile,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Bottom Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (downloadProgress != null) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "جاري التحميل للهاتف...", style = MaterialTheme.typography.labelSmall)
                                    Text(text = "${(downloadProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (file.isDownloaded) {
                                FilledTonalButton(
                                    onClick = { onOpenExternal(context) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("محفوظ بالهاتف", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = onDownloadClick,
                                    enabled = downloadProgress == null,
                                    modifier = Modifier.weight(1f).testTag("viewer_download_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تحميل للقراءة بدون نت", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = { onShare(context) },
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مشاركة", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onOpenExternal(context) },
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Drive", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Native Android PDF Renderer supporting page rendering, zoom, and navigation
 */
@Composable
fun NativePdfPageViewer(
    pdfFile: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("studydrive_reading_progress", Context.MODE_PRIVATE)
    }
    val progressKey = remember(pdfFile.name) { "pdf_page_${pdfFile.name}" }

    var pageCount by remember { mutableIntStateOf(1) }
    var currentPageIndex by remember(progressKey) {
        mutableIntStateOf(prefs.getInt(progressKey, 0).coerceAtLeast(0))
    }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val scope = rememberCoroutineScope()

    // Render page whenever index or file changes
    LaunchedEffect(pdfFile.absolutePath, currentPageIndex) {
        isRendering = true
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            var page: PdfRenderer.Page? = null
            try {
                pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                val total = renderer.pageCount.coerceAtLeast(1)
                val safeIndex = currentPageIndex.coerceIn(0, total - 1)
                prefs.edit().putInt(progressKey, safeIndex).apply()

                page = renderer.openPage(safeIndex)
                // 2x scale for sharp text readability on high-res displays
                val renderWidth = (page.width * 2).coerceIn(600, 2048)
                val renderHeight = (page.height * 2).coerceIn(800, 2800)

                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                withContext(Dispatchers.Main) {
                    pageCount = total
                    if (currentPageIndex != safeIndex) currentPageIndex = safeIndex
                    currentBitmap = bitmap
                    isRendering = false
                }
            } catch (e: Exception) {
                Log.e("PdfViewer", "Failed to render PDF page $currentPageIndex: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isRendering = false
                }
            } finally {
                try { page?.close() } catch (_: Exception) {}
                try { renderer?.close() } catch (_: Exception) {}
                try { pfd?.close() } catch (_: Exception) {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page Navigation & Zoom Control Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2C2C2C),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (currentPageIndex > 0) {
                                currentPageIndex--
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        enabled = currentPageIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (currentPageIndex > 0) Color.White else Color.Gray
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "صفحة ${currentPageIndex + 1} من $pageCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentPageIndex < pageCount - 1) {
                                currentPageIndex++
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        enabled = currentPageIndex < pageCount - 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            tint = if (currentPageIndex < pageCount - 1) Color.White else Color.Gray
                        )
                    }
                }

                // Zoom Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            scale = (scale - 0.25f).coerceAtLeast(1f)
                            if (scale == 1f) offset = Offset.Zero
                        },
                        enabled = scale > 1f
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = if (scale > 1f) Color.White else Color.Gray)
                    }

                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = {
                            scale = (scale + 0.25f).coerceAtMost(3f)
                        },
                        enabled = scale < 3f
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = if (scale < 3f) Color.White else Color.Gray)
                    }
                }
            }
        }

        // Rendered Page Bitmap Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isRendering && currentBitmap == null) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (currentBitmap != null) {
                val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 3f)
                    if (scale > 1f) {
                        offset += offsetChange
                    } else {
                        offset = Offset.Zero
                    }
                }

                Card(
                    shape = RoundedCornerShape(6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .transformable(state = transformableState)
                ) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPageIndex + 1}",
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

/**
 * Text & Markdown Summary Preview with syntax-highlighted code blocks
 */
@Composable
fun MarkdownOrCodePreview(
    file: DriveFile,
    localFile: File?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val content = remember(localFile?.absolutePath) {
        if (localFile != null && localFile.exists()) {
            localFile.readText()
        } else {
            file.description
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFF121820))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Document Header Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color(0xFF00668B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "ملخص دراسي سريع ومكثف",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "${file.semester} • ${file.academicYear}",
                color = Color(0xFF80DEEA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = file.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Split text and render syntax-highlighted cards
        val sections = content.split("```")
        sections.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code Block
                val codeLines = part.lines()
                val lang = codeLines.firstOrNull()?.trim().orEmpty()
                val actualCode = codeLines.drop(1).joinToString("\n").ifBlank { part }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2836)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3E50))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang.isNotBlank()) lang.uppercase() else "PYTHON CODE",
                                color = Color(0xFF4FC3F7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("كود تطبيقي", color = Color(0xFF90A4AE), fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = actualCode,
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Text / Markdown section
                val lines = part.lines()
                for (line in lines) {
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("#") -> {
                            val heading = trimmed.removePrefix("#").trim()
                            Text(
                                text = heading,
                                color = Color(0xFF4DD0E1),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text("• ", color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold)
                                Text(
                                    text = trimmed.removePrefix("-").removePrefix("*").trim(),
                                    color = Color(0xFFCFD8DC),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                        trimmed.isNotBlank() -> {
                            Text(
                                text = trimmed,
                                color = Color(0xFFECEFF1),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide Presentation Deck Viewer with Interactive Slide Cards
 */
@Composable
fun SlidePresentationViewer(
    file: DriveFile,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            SlideItem(
                slideNumber = 1,
                title = file.name.removeSuffix(".pptx").replace("_", " "),
                subtitle = "بوابة البرمجة للبكالوريا • إعداد ${file.professor}",
                points = listOf(
                    "مقدمة الدرس والأهداف التعليمية للعام الدراسي ${file.academicYear}",
                    "التركيز على المفاهيم المعتمدة في الاختبارات النهائية",
                    "أمثلة برمجية تفاعلية خطوة بخطوة"
                )
            ),
            SlideItem(
                slideNumber = 2,
                title = "المفاهيم الأساسية وقواعد التنفيذ",
                subtitle = "الفصل الدراسي: ${file.semester}",
                points = listOf(
                    "تتبع المتغيرات والشروط الحاكمة لتنفيذ الأوامر",
                    "تحليل خرائط التدفق (Flowcharts) وربطها بالأكواد",
                    "تجنب الأخطاء الشائعة (Syntax & Logical Errors)"
                )
            ),
            SlideItem(
                slideNumber = 3,
                title = "التطبيق العملي ونماذج الحلول",
                subtitle = "نصائح أ. أحمد عثمان للامتحان",
                points = listOf(
                    "قراءة المسألة مرتين وتحديد المدخلات والمخرجات",
                    "كتابة الخوارزمية كشبه رمز (Pseudocode) أولاً",
                    "التأكد من التنسيق والمسافات البادئة بدقة"
                )
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentSlideIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Slide Deck Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "شريحة ${currentSlide.slideNumber} من ${slides.size}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { if (currentSlideIndex > 0) currentSlideIndex-- },
                    enabled = currentSlideIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Slide",
                        tint = if (currentSlideIndex > 0) Color.White else Color.Gray
                    )
                }

                IconButton(
                    onClick = { if (currentSlideIndex < slides.size - 1) currentSlideIndex++ },
                    enabled = currentSlideIndex < slides.size - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Slide",
                        tint = if (currentSlideIndex < slides.size - 1) Color.White else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Slide Canvas Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = currentSlide.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    currentSlide.points.forEach { pt ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("🔹 ", color = Color(0xFF38BDF8))
                            Text(
                                text = pt,
                                color = Color(0xFFF1F5F9),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Slide Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بوابة البرمجة للبكالوريا",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "01113736196",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class SlideItem(
    val slideNumber: Int,
    val title: String,
    val subtitle: String,
    val points: List<String>
)

/**
 * Fallback General Document Viewer for Docs, Spreadsheets, and other materials
 */
@Composable
fun GeneralDocumentViewer(
    file: DriveFile,
    localFile: File?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FileTypeIcon(file = file, iconSize = 28, containerSize = 48)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                CategoryChip(category = file.category)

                Text(
                    text = file.description.ifBlank { "مذكرة دراسية مخصصة لطلاب البكالوريا - إعداد وتدريس المهندس أحمد عثمان." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Exam Tips and Study Hints
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "💡 إرشادات دراسية هامة للاختبار",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "1. قم بمراجعة هذا الملف جيداً وحل الأسئلة المرفقة.\n2. احفظ الدوال والمفردات البرمجية الأساسية.\n3. تواصل مع المهندس أحمد عثمان لأي استفسار.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
