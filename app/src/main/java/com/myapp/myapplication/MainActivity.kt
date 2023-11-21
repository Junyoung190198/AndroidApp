package com.myapp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import android.telephony.Telephony
import android.telephony.SmsMessage
import android.content.BroadcastReceiver



class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "SMS permission accepted", Toast.LENGTH_SHORT).show()
            setupSmsReceiver()
            setupMmsReceiver()
        } else {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this@MainActivity, "Contacts permission accepted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this@MainActivity, "Contacts permission denied", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val smsReceiver = SmsReceiver { sms -> handleSms(sms) }

    private val mmsReceiver = MmsReceiver { mms: Mms -> handleMms(mms) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smsTextView = findViewById(R.id.sms_text_view)

        // Request permissions
        requestSmsAndContactsPermissions()
    }

    private fun requestSmsAndContactsPermissions() {
        val smsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        )
        val contactsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )

        if (smsPermission != PackageManager.PERMISSION_GRANTED || contactsPermission != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            requestSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            // Permissions already granted, set up receivers
            setupSmsReceiver()
            setupMmsReceiver()
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

    private fun handleSms(sms: SmsMessage) {
        val sender = sms.originatingAddress
        val messageBody = sms.messageBody
        // Save the SMS data to a CSV file
        saveToCsv(sender, messageBody)
    }

    fun handleMmsMessage(context: Context?, mms: Mms?) {
        // Handle the MMS message here
        if (mms != null) {
            val sender = mms.sender
            val message = mms.message

            // Now, you can decide how to save or process the MMS data
            saveToCsv(sender, message)
        }
    }

    fun saveToCsv(sender: String?, message: String?) {
        if (sender == null || message == null) {
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "sms_data_$timeStamp.csv"

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "SmsData"
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, fileName)

            val writer = FileWriter(file, true)
            writer.append("\"$sender\",\"$message\"\n")
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
                    "Error saving data to CSV file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun onSaveToCsvButtonClick(view: View) {
        // Trigger the process of reading SMS and MMS and saving to CSV
        handleSms(smsObject)
        handleMms(mmsObject)
    }

    override fun onDestroy() {
        // Unregister receivers to avoid memory leaks
        unregisterReceiver(smsReceiver)
        unregisterReceiver(mmsReceiver)

        super.onDestroy()
    }
}
