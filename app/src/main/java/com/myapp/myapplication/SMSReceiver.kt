package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast


class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action){
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for(sms in messages){
                val messageBody = sms.messageBody
                Toast.makeText(context, "New SMS Received: $messageBody", Toast.LENGTH_SHORT).show()
            }
        }
    }
}