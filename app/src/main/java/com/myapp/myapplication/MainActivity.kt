package com.myapp.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.Intent




class MainActivity : AppCompatActivity() {

    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request SMS and Contacts permissions when the app is launched
        requestSmsAndContactsPermissions()

        serviceIntent = Intent(this, MessageBackgroundService::class.java)
        startService(serviceIntent)
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