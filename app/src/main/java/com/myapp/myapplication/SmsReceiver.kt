package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.provider.Telephony
import android.provider.ContactsContract

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Empty onReceive method, as we're not automatically processing incoming messages

    }

    // using "import android.provider.Telephony" for SMS, and not " val uri: Uri = Uri.parse("content://sms/inbox")"

    internal fun getExistingSmsMessages(context: Context?): List<Triple<String, String, String?>> {
        val messages = mutableListOf<Triple<String, String, String?>>()

        // Add a null check for the context
        if (context == null) {
            Log.e("SmsReceiver", "Context is null. Unable to retrieve SMS messages.")
            return messages
        }

        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf("body", "address")

        try {
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

            // Add a null check for the cursor
            if (cursor != null) {
                cursor.use {
                    // Log column names
                    val columnNames = it.columnNames
                    Log.d("SmsReceiver", "Column names: ${columnNames.joinToString()}")

                    while (it.moveToNext()) {
                        // Use the correct column names to retrieve the message body and sender's address
                        val messageBodyIndex = it.getColumnIndexOrThrow("body")
                        val senderAddressIndex = it.getColumnIndexOrThrow("address")

                        if (messageBodyIndex != -1 && senderAddressIndex != -1) {
                            val messageBody = it.getString(messageBodyIndex)
                            val senderAddress = it.getString(senderAddressIndex)
                            val contactName = getContactNameFromPhoneNumber(context, senderAddress)

                            // Add a null check for messageBody and senderAddress
                            if (messageBody != null) {
                                messages.add(Triple(contactName ?: senderAddress, senderAddress, messageBody))
                            } else {
                                // Log or handle the case where messageBody or senderAddress is null
                                Log.e("SmsReceiver", "Message body or sender address is null for SMS. Cursor position: ${it.position}")
                            }
                        } else {
                            // Log or handle the case where the body or address column is not found
                            Log.e("SmsReceiver", "Column 'body' or 'address' not found in the cursor.")
                        }
                    }
                }
            } else {
                Log.e("SmsReceiver", "Cursor is null. Unable to retrieve SMS messages.")
            }
        } catch (e: Exception) {
            // Log or handle the exception when querying the content resolver
            Log.e("SmsReceiver", "Error querying content resolver", e)
        }

        return messages
    }

    private fun getContactNameFromPhoneNumber(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName: String? = null

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            // Check if the "DISPLAY_NAME" column exists
            val displayNameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
            if (displayNameIndex == -1) {
                // Log or handle the case where the column is not found
                Log.e("SmsReceiver", "Column 'DISPLAY_NAME' not found in the cursor.")
                return null
            }

            if (cursor.moveToFirst()) {
                // Retrieve the contact name using the correct column index
                contactName = cursor.getString(displayNameIndex)
            }
        }

        return contactName
    }




    // Modify the method to accumulate SMS messages
    internal fun saveMessagesToCsv(context: Context?, smsMessages: List<Triple<String, String, String?>>) {
        val csvStringBuilder = StringBuilder()
        for (smsMessage in smsMessages) {
            csvStringBuilder.append("${smsMessage.first ?: ""},${smsMessage.third ?: ""}\n")
        }
        val csvData = csvStringBuilder.toString()
        (context as? MainActivity)?.saveSmsToCsv("SMS_data.csv", csvData)
    }
}