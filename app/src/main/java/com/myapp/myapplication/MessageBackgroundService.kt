package com.myapp.myapplication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager



class MessageBackgroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Enqueue the SmsWorker
        val smsWorkRequest = OneTimeWorkRequestBuilder<SmsWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(smsWorkRequest)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}