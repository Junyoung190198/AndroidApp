// SmsReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import android.provider.Telephony


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val smsContent = encodeToUtf8(message.messageBody)

                // Handle nullable originatingAddress
                val sender = message.originatingAddress ?: "Unknown"

                // Save SMS content to CSV and JSON
                saveToCsv("sms_data.csv", sender, smsContent)
                saveToJson("sms_data.json", sender, smsContent)

                // Broadcast the received SMS content
                val broadcastIntent = Intent("SMS_RECEIVED")
                broadcastIntent.putExtra("sms_content", smsContent)
                context?.sendBroadcast(broadcastIntent)

            }
        }
    }

    private fun saveToCsv(fileName: String, sender: String, content: String) {
        try {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val messageDataDirectory = File(directory, "Message_data")
            if (!messageDataDirectory.exists()) {
                messageDataDirectory.mkdirs()
            }

            val csvFile = File(messageDataDirectory, fileName)
            val writer = FileWriter(csvFile, true) // 'true' for append mode
            writer.append("$sender,$content\n")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveToJson(fileName: String, sender: String, content: String) {
        try {
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val messageDataDirectory = File(directory, "Message_data")
            if (!messageDataDirectory.exists()) {
                messageDataDirectory.mkdirs()
            }

            val jsonFile = File(messageDataDirectory, fileName)
            val jsonObject = JSONObject()
            jsonObject.put("timestamp", getCurrentTimestamp())
            jsonObject.put("sender", sender)
            jsonObject.put("content", content)

            val writer = FileWriter(jsonFile, true) // 'true' for append mode
            writer.append(jsonObject.toString() + "\n")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
        return sdf.format(Date())
    }

    private fun encodeToUtf8(text: String): String {
        // Encode the text to UTF-8
        val utf8Bytes = text.toByteArray(Charsets.UTF_8)
        return String(utf8Bytes, Charsets.UTF_8)
    }
}
