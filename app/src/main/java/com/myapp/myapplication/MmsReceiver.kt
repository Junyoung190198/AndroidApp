// MmsReceiver.kt
package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.net.Uri
import android.util.Log



data class Mms(
    val sender: String?,
    val message: String?
)

object MmsParser {
    fun parseFromPdu(pdu: ByteArray): MessageDetails {
        // Simplified parsing logic assuming text is in the "body" or "text" part
        val sender = "MMS Sender"
        val message = extractTextFromPdu(pdu)

        return MessageDetails(MessageType.MMS, sender, message)
    }

    // Add this function inside the MmsParser object
    private fun extractTextFromPdu(pdu: ByteArray): String {
        // This is a simplified example assuming text is in the "body" part
        val bodyIndex = findMmsPartIndex(pdu, "body")
        if (bodyIndex != -1) {
            return String(pdu.copyOfRange(bodyIndex, pdu.size), Charsets.UTF_8)
        }

        // If "body" part is not found, try "text" part
        val textIndex = findMmsPartIndex(pdu, "tex")
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
            if (i + partStart.size > pdu.size) {
                // Avoid index out of bounds
                return -1
            }

            if (pdu.copyOfRange(i, i + partStart.size).contentEquals(partStart)) {
                var endIndex = -1
                for (j in i until pdu.size - partEnd.size + 1) {
                    if (j + partEnd.size > pdu.size) {
                        // Avoid index out of bounds
                        return -1
                    }

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
        // Logic for handling MMS received broadcast
    }

    // Using "import android.provider.Telephony" and also "Uri.parse("content://mms/inbox")"
    internal fun getExistingMmsMessages(context: Context?): List<MessageDetails> {
        val messages = mutableListOf<MessageDetails>()

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
                val pdu = subject?.toByteArray()
                pdu?.let {
                    val mms = MmsParser.parseFromPdu(it)
                    messages.add(mms)
                }
            }
        }

        return messages
    }
}