// BootReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your background service here
            val serviceIntent = Intent(context, MessageBackgroundService::class.java)
            context?.startService(serviceIntent)
        }
    }
}
