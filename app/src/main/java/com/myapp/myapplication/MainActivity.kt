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
import android.content.IntentFilter
import android.provider.Telephony



class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null


    private val smsReceiver = SmsReceiver()

    private val mmsReceiver = MmsReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smsTextView = findViewById(R.id.sms_text_view)

        // Request permissions when the app starts
        requestSmsAndContactsPermissions()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
            if (permissions.all { it.value }) {
                Toast.makeText(this, "Permissions accepted", Toast.LENGTH_SHORT).show()
                setupReceivers()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
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
            requestPermissionLauncher.launch(permissionsArray)
        } else {
            // Permissions already granted, set up receivers
            setupReceivers()
        }
    }


    private fun setupSmsReceiver() {
        // Register SMS receiver
        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }

    private fun setupMmsReceiver() {
        // Register MMS receiver
        registerReceiver(mmsReceiver, IntentFilter("android.provider.Telephony.WAP_PUSH_RECEIVED"))
    }
    private fun setupReceivers() {
        setupSmsReceiver()
        setupMmsReceiver()
    }


    // Modify onSaveToCsvButtonClick to pass isMms parameter to saveToCsv
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

            // Retrieve and save SMS messages
            val smsMessages = smsReceiver.getExistingSmsMessages(this)
            smsReceiver.saveMessagesToCsv(this, smsMessages)

            // Retrieve and save MMS messages
            val mmsMessages = mmsReceiver.getExistingMmsMessages(this)
            mmsReceiver.saveMessagesToCsv(this, mmsMessages)

        } else {
            // Permissions are not granted, show a message or request permissions again
            Toast.makeText(
                this,
                "Please grant SMS and Contacts permissions",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun encodeToUtf8(text: String): String {
        // Encode the text to UTF-8
        val utf8Bytes = text.toByteArray(Charsets.UTF_8)
        return String(utf8Bytes, Charsets.UTF_8)
    }

    fun saveSmsToCsv(sender: String?, message: String?) {
        if (sender == null || message == null) {
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileType = "sms"
            val fileName = "${fileType}_data_$timeStamp.csv"

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SmsData"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, fileName)

            val writer = FileWriter(file, true)
            val csvLine = "\"${encodeToUtf8(sender)}\",\"${encodeToUtf8(message)}\"\n"
            writer.append(csvLine)
            writer.flush()
            writer.close()

            runOnUiThread {
                Toast.makeText(this@MainActivity, "SMS data saved to $file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Error saving SMS data to CSV file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun saveMmsToCsv(sender: String?, message: String?) {
        if (sender == null || message == null) {
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileType = "mms"
            val fileName = "${fileType}_data_$timeStamp.csv"

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SmsData"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, fileName)

            val writer = FileWriter(file, true)
            val csvLine = "\"${encodeToUtf8(sender)}\",\"${encodeToUtf8(message)}\"\n"
            writer.append(csvLine)
            writer.flush()
            writer.close()

            runOnUiThread {
                Toast.makeText(this@MainActivity, "MMS data saved to $file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Error saving MMS data to CSV file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    override fun onPause() {
        // Unregister receivers when the activity is no longer in the foreground
        unregisterReceiver(smsReceiver)
        unregisterReceiver(mmsReceiver)

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        // Re-register receivers when the activity comes back to the foreground
        setupSmsReceiver()
        setupMmsReceiver()
    }
}
