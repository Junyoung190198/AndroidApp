// MainActivity.kt
package com.myapp.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val SMS_PERMISSION_CODE = 123

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle["pdus"] as Array<*>?
                    if (pdus != null) {
                        for (i in pdus.indices) {
                            val format = bundle.getString("format")
                            val smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
                            val senderNum: String = smsMessage.originatingAddress ?: "unknown"
                            val messageBody: String = smsMessage.messageBody ?: ""
                            val timestamp = System.currentTimeMillis()

                            runOnUiThread {
                                updateUI(senderNum, messageBody, timestamp)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate")

        if (checkPermission()) {
            registerSmsReceiver()
        } else {
            requestPermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECEIVE_SMS),
            SMS_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerSmsReceiver()
            } else {
                // Handle permission denied
                Log.e("MainActivity", "SMS permission denied")
            }
        }
    }

    private fun registerSmsReceiver() {
        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, intentFilter)
    }

    private fun updateUI(senderNum: String, messageBody: String, timestamp: Long) {
        Log.d("MainActivity", "Update UI with SMS content: $senderNum, $messageBody, $timestamp")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        val formattedDate = dateFormat.format(date)

        val smsDetails = getString(R.string.sms_details, senderNum, messageBody, formattedDate)

        val smsTextView: TextView = findViewById(R.id.sms_text_view)
        smsTextView.append("\n$smsDetails")
    }
}
