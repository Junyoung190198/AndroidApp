// MmsReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.database.Cursor
import android.net.Uri



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
        if (bodyIndex != -1) {
            return String(pdu.copyOfRange(bodyIndex, pdu.size), Charsets.UTF_8)
        }

        // If "body" part is not found, try "text" part
        val textIndex = findMmsPartIndex(pdu, "text")
        if (textIndex != -1) {
            return String(pdu.copyOfRange(textIndex, pdu.size), Charsets.UTF_8)
        }

        // If no relevant part is found, return an empty string
        return ""
    }


    // Helper function to find the index of a specific MMS part
    private fun findMmsPartIndex(pdu: ByteArray, partName: String): Int {
        val partStart = "--$partName".toByteArray(Charsets.UTF_8)
        val partEnd = "\r\n\r\n".toByteArray(Charsets.UTF_8)

        for (i in pdu.indices) {
            if (pdu.copyOfRange(i, i + partStart.size).contentEquals(partStart)) {
                var endIndex = -1
                for (j in i until pdu.size - partEnd.size + 1) {
                    if (pdu.copyOfRange(j, j + partEnd.size).contentEquals(partEnd)) {
                        endIndex = j
                        break
                    }
                }

                if (endIndex != -1) {
                    return i
                }
            }
        }
        return -1
    }
}

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Query existing MMS messages from the inbox
        val mmsMessages = getExistingMmsMessages(context)
        saveMessagesToCsv(context, mmsMessages)
    }

    private fun getExistingMmsMessages(context: Context?): List<Mms> {
        val messages = mutableListOf<Mms>()

        val uri: Uri = Uri.parse("content://mms")
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val mmsIdColumnIndex = it.getColumnIndex("_id")
                if (mmsIdColumnIndex >= 0) {
                    val mmsId = it.getString(mmsIdColumnIndex)
                    val mmsPduColumnIndex = it.getColumnIndex("pdu")
                    if (mmsPduColumnIndex < 0) {
                        // Try alternative column names
                        val alternativeColumnNames = listOf("m_data", "mms_data")
                        for (columnName in alternativeColumnNames) {
                            val alternativeIndex = it.getColumnIndex(columnName)
                            if (alternativeIndex >= 0) {
                                val mmsPdu = it.getBlob(alternativeIndex)
                                val mms = MmsParser.parseFromPdu(mmsPdu)
                                messages.add(mms)
                                break
                            }
                        }
                    } else {
                        val mmsPdu = it.getBlob(mmsPduColumnIndex)
                        val mms = MmsParser.parseFromPdu(mmsPdu)
                        messages.add(mms)
                    }
                }
            }
        }

        return messages
    }

    private fun saveMessagesToCsv(context: Context?, messages: List<Mms>) {
        // Your logic to save MMS messages to CSV
        for (message in messages) {
            (context as? MainActivity)?.saveToCsv(message.sender, message.message)
        }
    }
}

