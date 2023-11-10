package com.myapp.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException
import android.view.View



class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null


    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            requestContactsPermission()
        } else {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            readSmsAndSaveToCsv()
        } else {
            Toast.makeText(this@MainActivity, "File upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkSmsPermission() {

    }

    // Function to request contacts permission
    private fun requestContactsPermission() {

    }

    // Function to get contact name
    private fun getContactName(phoneNumber: String): String {
        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null
        )

        return cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (columnIndex >= 0) {
                    cursor.getString(columnIndex) ?: "Unknown"
                } else {
                    "Unknown"
                }
            } else {
                "Unknown"
            }
        } ?: "Unknown"
    }


    // Function to read SMS and save to CSV
    private fun readSmsAndSaveToCsv() {
        // Create a list to store TextMessage objects
        val messages = mutableListOf<TextMessage>()

        // Define the URI for the SMS inbox
        val uri = Uri.parse("content://sms/inbox")

        // Query the content resolver to retrieve SMS messages
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use { cursor ->
            // Check if there are any SMS messages
            if (cursor.moveToFirst()) {
                // Define column indices for phone number and message body
                val phoneNumberIndex = cursor.getColumnIndex("address")
                val messageIndex = cursor.getColumnIndex("body")

                // Iterate through the cursor to retrieve messages
                do {
                    // Extract phone number and message body
                    val phoneNumber = cursor.getString(phoneNumberIndex) ?: "Unknown"
                    val message = cursor.getString(messageIndex) ?: ""

                    // Get contact name using the phoneNumber
                    val contactName = getContactName(phoneNumber)

                    // Create a TextMessage object and add it to the list
                    val textMessage = TextMessage(contactName, phoneNumber, message)
                    messages.add(textMessage)
                } while (cursor.moveToNext())

                // CSV saving logic
                try {
                    val fileName = "sms_data.csv"
                    val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                    val writer = FileWriter(filePath, true) // Append to the existing file

                    messages.forEach { textMessage ->
                        // Convert each message to CSV format and write to the file
                        val csvLine = "${textMessage.sender},${textMessage.phoneNumber},${textMessage.message}\n"
                        writer.write(csvLine)
                    }

                    // Close the FileWriter
                    writer.close()

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Messages saved to CSV", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "CSV file save failed", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }
            } else {
                // Handle the case where no SMS messages are found
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "No SMS messages found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Button click handler
    fun onSaveToCsvButtonClick(view:View){
        readSmsAndSaveToCsv()
    }
}
