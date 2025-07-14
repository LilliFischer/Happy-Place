package com.example.happyplaces

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

fun saveUriToFile(context: Context, uri: Uri, desiredFileName: String): String? {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e("FileUtils", "Failed to get input stream from URI: $uri")
            return null
        }

        // Create a file in the app's internal files directory
        // This directory is private to your app
        val outputDir = context.filesDir // Or context.getExternalFilesDir(null) for app-specific external
        val outputFile = File(outputDir, desiredFileName)

        outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(4 * 1024) // 4K buffer
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()

        Log.d("FileUtils", "File saved successfully: ${outputFile.absolutePath}")
        return outputFile.absolutePath // Return the absolute path of the saved file
    } catch (e: IOException) {
        Log.e("FileUtils", "Error saving URI to file", e)
        return null // Return null if there was an error
    } finally {
        try {
            inputStream?.close()
            outputStream?.close()
        } catch (e: IOException) {
            Log.e("FileUtils", "Error closing streams", e)
        }
    }
}