package com.vigram.sdkgh1.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun openFile(context: Context ,fileName: String) {
    try {
        val file = File("${context.filesDir}/$fileName")
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
        intentShareFile.setType("application/pdf")
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...")
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
        context.startActivity(Intent.createChooser(intentShareFile, "Share File"))
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun createFile(folder: String, name: String, fileExtension: String): File {
    fun Date.getCurrentDateToString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return formatter.format(this)
    }

    val dateString = Date().getCurrentDateToString()
    val fileName = "$dateString.$fileExtension"

    val documentsDir = File(Environment.getExternalStorageDirectory(), "Documents/$folder")
    if (!documentsDir.exists()) {
        documentsDir.mkdirs()
    }

    val log = File(documentsDir, name)
    if (!log.exists()) {
        log.mkdirs()
    }

    val filePath = File(log, fileName)
    if (!filePath.exists()) {
        filePath.createNewFile()
    }

    return filePath
}

fun getFileExtension(uri: Uri?, context: Context): String? {
    if (uri?.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val displayName = cursor.getString(nameIndex)
                return displayName
            }
        }
    } else if (uri?.scheme == "file") {
        return uri.path
    }
    return null
}

