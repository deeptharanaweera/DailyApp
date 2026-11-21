package com.example.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object LocalFileStorage {

    /**
     * Copies a user-picked image or PDF from an external content URI into app-private
     * offline storage so receipts and medical reports remain permanently accessible offline.
     */
    fun copyUriToInternalStorage(
        context: Context,
        sourceUri: Uri,
        subDir: String,
        prefix: String
    ): String? {
        return runCatching {
            val dir = File(context.filesDir, subDir).apply { if (!exists()) mkdirs() }
            val mimeType = context.contentResolver.getType(sourceUri)
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val destFile = File(dir, "${prefix}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        }.getOrNull()
    }

    /**
     * Creates a FileProvider URI for capturing a fresh receipt or medical report photo with Camera.
     */
    fun createCameraCaptureUri(context: Context, prefix: String): Pair<Uri, File> {
        val dir = File(context.cacheDir, "captures").apply { if (!exists()) mkdirs() }
        val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file
    }

    fun getFileProviderUri(context: Context, filePath: String): Uri? {
        val file = File(filePath)
        if (!file.exists()) return null
        return runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }
}
