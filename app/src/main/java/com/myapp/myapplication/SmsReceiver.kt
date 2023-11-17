package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val pdus = intent.getSerializableExtra("pdus") as Array<*>?
            if (pdus != null) {
                for (pdu in pdus) {
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                    handleSms(context, sms)
                }
            }
        }
    }

    private fun handleSms(context: Context?, sms: SmsMessage) {
        // Handle the SMS message here
        val sender = sms.originatingAddress
        val messageBody = sms.messageBody

        // Now, you can decide how to save or process the SMS data
        (context as? MainActivity)?.saveToCsv(sender, messageBody)
    }
}
