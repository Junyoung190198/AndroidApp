package com.myapp.myapplication

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.io.OutputStream
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkSmsAndContactsPermission()
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
            Toast.makeText(this@MainActivity, "Contacts permission denied", Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smsTextView = findViewById(R.id.sms_text_view)
        checkSmsAndContactsPermission()
    }

    private fun checkSmsAndContactsPermission() {
        val smsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        )
        val contactsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )

        if (smsPermission == PackageManager.PERMISSION_GRANTED && contactsPermission == PackageManager.PERMISSION_GRANTED) {
            if (isButtonPressed) {
                readSmsAndSaveToCsv()
            }
        } else {
            if (smsPermission != PackageManager.PERMISSION_GRANTED) {
                requestSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            }
            if (contactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

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

    private fun readSmsAndSaveToCsv() {
        val messages = mutableListOf<Triple<String, String, String>>()

        // Define the URI for both SMS and MMS
        val smsUri = Uri.parse("content://sms/inbox")
        val mmsUri = Uri.parse("content://mms/inbox")

        // Query the content resolver to retrieve both SMS and MMS messages
        val smsCursor = contentResolver.query(smsUri, null, null, null, null)
        val mmsCursor = contentResolver.query(mmsUri, null, null, null, null)

        // Function to sanitize text
        fun sanitizeText(text: String): String {
            // Replace problematic characters with alternatives
            return text.replace("(", "[").replace(")", "]").replace(Regex("[\n\r]"), " ")
        }

        // Function to sanitize phone number
        fun sanitizePhoneNumber(phoneNumber: String): String {
            // Remove any non-numeric characters from the phone number
            return phoneNumber.replace(Regex("[^0-9]"), "")
        }

        // Function to handle MMS parts
        fun handleMmsPart(mmsId: String): String {
            val partUri = Uri.parse("content://mms/part")
            val partCursor = contentResolver.query(partUri, null, "mid = ?", arrayOf(mmsId), null)

            val content = StringBuilder()

            partCursor?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val partDataIndex = cursor.getColumnIndex("text")
                    do {
                        val partData = cursor.getString(partDataIndex) ?: ""
                        content.append(partData)
                    } while (cursor.moveToNext())
                }
            }

            return content.toString()
        }

        // Process SMS messages
        smsCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val phoneNumberIndex = cursor.getColumnIndex("address")
                val messageIndex = cursor.getColumnIndex("body")

                do {
                    val phoneNumber = cursor.getString(phoneNumberIndex) ?: "Unknown"
                    val message = cursor.getString(messageIndex) ?: ""
                    val contactName = getContactName(phoneNumber)
                    val sanitizedMessage = sanitizeText(message)
                    val messageData = Triple(contactName, phoneNumber, sanitizedMessage)
                    messages.add(messageData)
                } while (cursor.moveToNext())
            }
        }

        // Process MMS messages
        mmsCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val mmsIdIndex = cursor.getColumnIndex("_id")

                do {
                    val mmsId = cursor.getString(mmsIdIndex) ?: ""
                    val message = handleMmsPart(mmsId)
                    val contactName = "Unknown"
                    val phoneNumber = "Unknown" // MMS messages may not have a sender phone number
                    val sanitizedMessage = sanitizeText(message)
                    val messageData = Triple(contactName, phoneNumber, sanitizedMessage)
                    messages.add(messageData)
                } while (cursor.moveToNext())
            }
        }

        // Continue with the existing code to save messages to CSV
        try {
            val fileName = "sms_data.csv"
            val filePath =
                File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val writer =
                BufferedWriter(OutputStreamWriter(FileOutputStream(filePath, true), Charsets.UTF_8))

            messages.forEach { message ->
                val csvLine = "${message.first},${message.second},${message.third}\n"
                writer.write(csvLine)
            }

            writer.close()

            val downloadResult = saveFileToDownloadsDirectory(filePath, messages)

            if (downloadResult) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "CSV file saved to Downloads directory",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to save CSV file to Downloads directory",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "CSV file save failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
            e.printStackTrace()
        }
    }


    private var isButtonPressed = false

    fun onSaveToCsvButtonClick(view: View) {
        isButtonPressed = true
        checkSmsAndContactsPermission()
    }


    private fun sanitizeText(text: String): String {
        // Replace problematic characters with alternatives
        return text.replace("(", "[").replace(")", "]").replace(Regex("[\n\r]"), " ")
    }

    private fun sanitizePhoneNumber(phoneNumber: String): String {
        // Remove any non-numeric characters from the phone number
        return phoneNumber.replace(Regex("[^0-9]"), "")
    }

    private fun saveFileToDownloadsDirectory(file: File, messages: List<Triple<String, String, String>>): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.SIZE, file.length())
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val resolver = contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                return try {
                    resolver.openOutputStream(uri)?.use { output ->
                        BufferedWriter(OutputStreamWriter(output, Charsets.UTF_8)).use { writer ->
                            writer.write("Name,Phone Number,Message Content\n")

                            messages.forEach { message ->
                                val sanitizedName = sanitizeText(message.first)
                                val sanitizedPhoneNumber = sanitizePhoneNumber(message.second)
                                val sanitizedMessage = sanitizeText(message.third)
                                val csvLine = "$sanitizedName,$sanitizedPhoneNumber,$sanitizedMessage\n"
                                writer.write(csvLine)
                            }
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            } ?: false
        } else {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, file.name)
                BufferedWriter(OutputStreamWriter(FileOutputStream(destFile, true), Charsets.UTF_8)).use { writer ->
                    writer.write("Name,Phone Number,Message Content\n")

                    messages.forEach { message ->
                        val sanitizedName = sanitizeText(message.first)
                        val sanitizedPhoneNumber = sanitizePhoneNumber(message.second)
                        val sanitizedMessage = sanitizeText(message.third)
                        val csvLine = "$sanitizedName,$sanitizedPhoneNumber,$sanitizedMessage\n"
                        writer.write(csvLine)
                    }
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
}
