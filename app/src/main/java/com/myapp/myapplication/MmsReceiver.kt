// MmsReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

import android.net.Uri
import android.util.Log

import java.util.Locale




data class Mms(
    val sender: String?,
    val message: String?
)

object MmsParser {
    fun parseFromPdu(pdu: ByteArray): Mms {
        // Simplified parsing logic assuming text is in the "body" or "text" part
        val sender = "MMS Sender"
        val message = extractTextFromPdu(pdu)

        return Mms(sender, message)
    }

    // Add this function inside the MmsParser object
    private fun extractTextFromPdu(pdu: ByteArray): String {
        // This is a simplified example assuming text is in the "body" part
        val bodyIndex = findMmsPartIndex(pdu, "body")
        if (bodyIndex != -1 && bodyIndex < pdu.size) {
            return String(pdu.copyOfRange(bodyIndex, pdu.size), Charsets.UTF_8)
        }

        // If "body" part is not found, try "text" part
        val textIndex = findMmsPartIndex(pdu, "text")
        if (textIndex != -1 && textIndex < pdu.size) {
            return String(pdu.copyOfRange(textIndex, pdu.size), Charsets.UTF_8)
        }

        // If no relevant part is found, return an empty string
        return ""
    }


    // Helper function to find the index of a specific MMS part
    private fun findMmsPartIndex(pdu: ByteArray, partName: String): Int {
        val lowerCasePartName = partName.toLowerCase(Locale.ROOT)
        val lowerCasePdu = String(pdu, Charsets.UTF_8).toLowerCase(Locale.ROOT)
        val partIndex = lowerCasePdu.indexOf(lowerCasePartName)

        if (partIndex != -1) {
            // Return the index of the start of the part
            return partIndex
        }

        // If the part is not found, return -1
        return -1
    }
}

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Empty onReceive method, as we're not automatically processing incoming messages
    }


    // Using "import android.provider.Telephony" and also "Uri.parse("content://mms/inbox")"
    internal fun getExistingMmsMessages(context: Context?): List<Mms> {
        val messages = mutableListOf<Mms>()

        // Add a null check for the context
        if (context == null) {
            Log.e("MmsReceiver", "Context is null. Unable to retrieve MMS messages.")
            return messages
        }

        val uri: Uri = try {
            Telephony.Mms.Inbox.CONTENT_URI
        } catch (e: Exception) {
            Log.w("MmsReceiver", "Unable to use Telephony.Mms.Inbox.CONTENT_URI. Falling back to content://mms/inbox.")
            Uri.parse("content://mms/inbox")
        }

        val projection = arrayOf("sub")

        // Use the try-with-resources syntax to automatically close the cursor
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val subject = cursor.getString(cursor.getColumnIndexOrThrow("sub"))

                // Add a null check for the subject
                if (subject != null) {
                    // Convert the subject to a byte array
                    val pdu = subject.toByteArray()

                    // Create Mms object and add it to the list
                    val mms = MmsParser.parseFromPdu(pdu)
                    messages.add(mms)
                } else {
                    // Log or handle the case where subject is null
                    Log.e("MmsReceiver", "Subject is null for MMS.")
                }
            }
        }

        return messages
    }


    // Add a method to convert MMS messages to CSV format
    internal fun saveMessagesToCsv(context: Context?, mmsMessages: List<Mms>) {
        val csvStringBuilder = StringBuilder()
        for (mmsMessage in mmsMessages) {
            csvStringBuilder.append("${mmsMessage.sender ?: ""},${mmsMessage.message ?: ""}\n")
        }
        val csvData = csvStringBuilder.toString()
        (context as? MainActivity)?.saveMmsToCsv("MMS_data.csv", csvData)
    }
}



