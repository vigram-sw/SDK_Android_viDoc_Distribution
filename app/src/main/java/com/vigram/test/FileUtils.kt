package com.vigram.test

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object FileUtils {

    fun createFileFromUri(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName: String = getFileName(context, uri) ?: return null
        val file = createFile(context, fileName)
        copyInputStreamToFile(inputStream, file)
        return file
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"))
                }
            } finally {
                cursor?.close()
            }
        } else if (uri.scheme == "file") {
            result = File(uri.path).name
        }
        return result
    }

    private fun createFile(context: Context, fileName: String): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(storageDir, fileName)
    }

    private fun copyInputStreamToFile(inputStream: InputStream?, outputFile: File) {
        if (inputStream != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(4 * 1024) // 4 KB
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    outputStream?.close()
                    inputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}