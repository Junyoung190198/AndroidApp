package com.myapp.myapplication


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.Telephony
class MmsReceiver {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent?.action) {
                val data = intent.data
                if (data != null && data.scheme == "mms") {
                    val pdus = intent.getByteArrayExtra("data")
                    if (pdus != null) {
                        val mms = Mms.from(context, pdus)
                        if (mms != null) {
                            handleMms(context, mms)
                        }
                    }
                }
            }
        }
    }

    private fun handleMms(context: Context, mms: Mms) {
        val sender = mms.transactionId
        val parts = mms.parts
        for (part in parts) {
            val mimeType = part.mimeType
            val data = part.data
            // Handle each part of the MMS
        }
    }
}