// MessageBackgroundService.kt
package com.myapp.myapplication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.content.IntentFilter

// MessageBackgroundService.kt
class MessageBackgroundService : Service() {

    companion object {
        const val ACTION_NEW_SMS = "com.myapp.myapplication.ACTION_NEW_SMS"
    }

    private val smsReceiver = SmsReceiver()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // Register the SmsReceiver to listen for incoming SMS messages
            val smsFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            registerReceiver(smsReceiver, smsFilter)


            return START_STICKY
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }
}

