package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import android.telephony.SmsMessage

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            // Telephony.Sms.Intents.getMessagesFromIntent(intent) is used to retrieve an array of SmsMessage objects from the intent
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            // Iterate over the array of SmsMessage to get the sender's phone number and the message body and the date
            for (message in messages) {
                // Handle the case where sender is null
                val phoneNumber = message.displayOriginatingAddress ?: "unknown"
                val timestamp = message.timestampMillis

                // Use the alternative approach to retrieve the message body
                val messageBody = getMessageBody(message)

                Log.i(
                    "SmsReceiver",
                    "senderNum: $phoneNumber; message: $messageBody; timestamp: $timestamp"
                )

                // Show the SMS in the main activity using a global broadcast
                val smsIntent = Intent("SMS_RECEIVED")
                smsIntent.putExtra("senderNum", phoneNumber)
                smsIntent.putExtra("messageBody", messageBody)
                smsIntent.putExtra("timestamp", timestamp)
                context?.sendBroadcast(smsIntent)
            }
        }
    }

    private fun getMessageBody(message: SmsMessage): String {
        return message.displayMessageBody
    }
}