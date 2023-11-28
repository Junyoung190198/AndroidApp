// SmsReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony

data class SmsMessageData(
    val sender: String?,
    val message: String?
)

object SmsParser {
    fun parseFromCursor(cursor: Cursor): SmsMessageData {
        val sender = cursor.getString(cursor.getColumnIndexOrThrow("address"))
        val messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body"))

        return SmsMessageData(sender, messageBody)
    }
}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Logic for handling SMS received broadcast
    }

    internal fun getExistingSmsMessages(context: Context?): List<SmsMessageData> {
        val messages = mutableListOf<SmsMessageData>()

        // Use Telephony.Sms.Inbox.CONTENT_URI for querying the inbox
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val smsData = SmsParser.parseFromCursor(it)
                messages.add(smsData)
            }
        }

        return messages
    }
}