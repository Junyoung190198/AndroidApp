package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Query existing SMS messages from the inbox
        val smsMessages = getExistingSmsMessages(context)
        saveMessagesToCsv(context, smsMessages)
    }

    internal fun getExistingSmsMessages(context: Context?): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        val uri: Uri = Uri.parse("content://sms/inbox")
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val sender = it.getString(it.getColumnIndexOrThrow("address"))
                val messageBody = it.getString(it.getColumnIndexOrThrow("body"))

                // Convert the message body to a byte array
                val pdu = messageBody?.toByteArray()

                // Create SmsMessage from the byte array
                pdu?.let { smsData ->
                    val sms = SmsMessage.createFromPdu(smsData)
                    messages.add(sms)
                }
            }
        }

        return messages
    }

    internal fun saveMessagesToCsv(context: Context?, messages: List<SmsMessage>) {
        // Your logic to save SMS messages to CSV
        for (message in messages) {
            val sender = message.originatingAddress ?: "Unknown Sender"
            val messageBody = message.messageBody ?: ""

            (context as? MainActivity)?.saveToCsv(sender, messageBody)
        }
    }
}
