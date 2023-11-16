package com.myapp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var smsTextView: TextView? = null

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "SMS permission accepted", Toast.LENGTH_SHORT).show()

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

    private val smsReceiver = SmsReceiver {
        // Handle SMS reception
        // (Note: You may choose to perform additional logic here if needed)
    }

    private val mmsReceiver = MmsReceiver {
        // Handle MMS reception
        // (Note: You may choose to perform additional logic here if needed)
    }

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
        }
    }
    private fun readSmsAndSaveToCsv() {
        // Existing logic for reading SMS and saving to CSV
        // ...

        // Display a message when the process is complete
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                "SMS and MMS data saved to CSV",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private var isButtonPressed = false

    fun onSaveToCsvButtonClick(view: View) {
        isButtonPressed = true
        readSmsAndSaveToCsv()
    }

    override fun onDestroy() {
        // Unregister receivers to avoid memory leaks
        unregisterReceiver(smsReceiver)
        unregisterReceiver(mmsReceiver)

        super.onDestroy()
    }


}
