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



class MainActivity : AppCompatActivity() {

    private lateinit var smsReceiver: SmsReceiver
    private lateinit var mmsReceiver: MmsReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request permissions when the app starts
        requestSmsAndContactsPermissions()

        // Register SMS receiver
        smsReceiver = SmsReceiver()
        val smsFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, smsFilter)

        // Register MMS receiver
        mmsReceiver = MmsReceiver()
        val mmsFilter = IntentFilter("android.provider.Telephony.WAP_PUSH_RECEIVED")
        mmsFilter.addDataScheme("sms")
        mmsFilter.addDataAuthority("*", "*")
        registerReceiver(mmsReceiver, mmsFilter)
    }

    override fun onDestroy() {
        // Unregister receivers when the app is destroyed
        unregisterReceiver(smsReceiver)
        unregisterReceiver(mmsReceiver)
        super.onDestroy()
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
            Toast.makeText(this, "Press the button to retrieve and save SMS and MMS data", Toast.LENGTH_SHORT).show()
        }
    }
}