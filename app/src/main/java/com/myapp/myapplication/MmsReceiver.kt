// MmsReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony



data class Mms(
    val sender: String?,
    val message: String?
)

object MmsParser {
    fun parseFromPdu(pdu: ByteArray): Mms? {
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
                val endIndex = pdu.indexOf(partEnd[0].toInt(), i)


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
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent?.action) {
            val data = intent.data
            if (data != null && data.scheme == "mms") {
                val pdus = intent.getByteArrayExtra("data")
                if (pdus != null) {
                    handleMms(context, mms)
                }
            }
        }
    }

    private fun handleMms(context: Context?, mms: Mms?) {
        // Call the function in MainActivity to save the MMS data
        (context as? MainActivity)?.saveToCsv(mms?.sender, mms?.message)
    }
}
