package com.myapp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.SmsMessage



data class MessageDetails(
    val type: MessageType,
    val sender: String?,
    val message: String?
)

enum class MessageType {
    SMS,
    MMS
}

class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smsTextView = findViewById(R.id.sms_text_view)

        // Request permissions when the app starts
        requestSmsAndContactsPermissions()
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
            if (permissions.all { it.value }) {
                Toast.makeText(this@MainActivity, "Permissions accepted", Toast.LENGTH_SHORT).show()
                handleSmsAndMms()
            } else {
                Toast.makeText(this@MainActivity, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestSmsAndContactsPermissions() {
        val smsPermission = Manifest.permission.READ_SMS
        val contactsPermission = Manifest.permission.READ_CONTACTS

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, smsPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(smsPermission)
        }

        if (ContextCompat.checkSelfPermission(this, contactsPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(contactsPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            val permissionsArray = permissionsToRequest.toTypedArray()
            requestPermissionsLauncher.launch(permissionsArray)
        }else {
            // Permissions already granted, show a message
            Toast.makeText(this, "Press the button to retrieve and save SMS and MMS data", Toast.LENGTH_SHORT).show()
        }

    }


    private val SmsReceiver = SmsReceiver()
    private val MmsReceiver = MmsReceiver()


    private fun saveToCsv(fileName: String, messages: List<Any>) {
        if (messages.isEmpty()) {
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val csvFileName = "${fileName}_$timeStamp.csv"

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "MessageData"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, csvFileName)

            val writer = FileWriter(file, true)
            for (message in messages) {
                val messageText = when (message) {
                    is SmsMessage -> {
                        val sender = message.originatingAddress ?: "Unknown Sender"
                        val messageBody = message.messageBody ?: ""
                        "$sender: $messageBody"
                    }
                    is Mms -> {
                        val sender = message.sender ?: "MMS Sender"
                        val messageBody = message.message ?: ""
                        "$sender: $messageBody"
                    }
                    else -> {
                        // Handle other types if necessary
                        ""
                    }
                }

                val csvLine = "\"${encodeToUtf8(messageText)}\"\n"
                writer.append(csvLine)
            }
            writer.flush()
            writer.close()

            runOnUiThread {
                Toast.makeText(this@MainActivity, "Data saved to $file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,

                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun encodeToUtf8(text: String): String {
        // Encode the text to UTF-8
        val utf8Bytes = text.toByteArray(Charsets.UTF_8)
        return String(utf8Bytes, Charsets.UTF_8)
    }


    fun onSaveToCsvButtonClick(view: View) {
        // Check if SMS and Contacts permissions are granted
        val smsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        )
        val contactsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )

        if (smsPermission == PackageManager.PERMISSION_GRANTED && contactsPermission == PackageManager.PERMISSION_GRANTED) {
            // Permissions are granted, proceed to retrieve SMS and MMS data
            handleSmsAndMms()
        } else {
            // Permissions are not granted, show a message or request permissions again
            Toast.makeText(this, "Please grant SMS and Contacts permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSmsAndMms() {
        // Retrieve and save SMS messages
        val smsDetails = SmsReceiver.getExistingSmsMessages(this).map { smsMessage ->
            MessageDetails(
                type = MessageType.SMS,
                sender = smsMessage.sender,
                message = smsMessage.message
            )
        }
        saveToCsv("SmsData", smsDetails)

        // Retrieve and save MMS messages
        val mmsDetails = MmsReceiver.getExistingMmsMessages(this).map { mmsMessage ->
            MessageDetails(
                type = MessageType.MMS,
                sender = mmsMessage.sender,
                message = mmsMessage.message
            )
        }
        saveToCsv("MmsData", mmsDetails)
    }
}