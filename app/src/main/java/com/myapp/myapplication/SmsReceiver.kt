package com.myapp.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action) {
            val pdus = intent.getSerializableExtra("pdus") as Array<*>?
            if (pdus != null) {
                for (pdu in pdus) {
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                    handleSms(context, sms)
                }
            }
        }
    }

    private fun handleSms(context: Context, sms: SmsMessage) {
        val sender = sms.originatingAddress
        val messageBody = sms.messageBody
        // Handle the SMS message here
    }

}