package com.example.core.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object MultipartUploadHelper {

    fun createPartFromString(string: String): RequestBody {
        return string.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun prepareFilePart(partName: String, fileUri: Uri, context: Context): MultipartBody.Part? {
        val file = getFileFromUri(context, fileUri) ?: return null
        val requestFile = file.asRequestBody(context.contentResolver.getType(fileUri)?.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    fun prepareFilePart(partName: String, file: File, mimeType: String = "application/octet-stream"): MultipartBody.Part {
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = if (nameIndex != null && nameIndex >= 0) cursor.getString(nameIndex) else "temp_upload_file"
            cursor?.close()

            val tempFile = File(context.cacheDir, fileName)
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
