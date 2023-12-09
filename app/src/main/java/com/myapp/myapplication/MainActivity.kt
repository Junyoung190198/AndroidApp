package com.myapp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter


import android.




class MainActivity : AppCompatActivity() {

    private lateinit var smsTextView: TextView
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "SMS_RECEIVED") {
                val sender = intent.getStringExtra("sender")
                val content = intent.getStringExtra("content")
                updateTextView("$sender: $content")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        smsTextView = findViewById(R.id.sms_text_view)

        // Register the BroadcastReceiver
        val intentFilter = IntentFilter("SMS_RECEIVED")
        registerReceiver(smsReceiver, intentFilter)
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
            if (permissions.all { it.value }) {
                Toast.makeText(this@MainActivity, "Permissions accepted", Toast.LENGTH_SHORT).show()
                // Now that permissions are granted, you can handle SMS and MMS
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
        } else {
            // Permissions already granted, show a message
            Toast.makeText(this, "All premissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTextView(message: String) {
        // Update the UI with the received message
        smsTextView.append("\n\n$message")
    }
}
