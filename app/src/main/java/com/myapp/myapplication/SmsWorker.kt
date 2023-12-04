package com.myapp.myapplication


import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.provider.Telephony

class SmsWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val smsReceiver = SmsReceiver()

    override fun doWork(): Result {
        try {
            // Register SMS receiver
            val smsFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            applicationContext.registerReceiver(smsReceiver, smsFilter)

            // Unregister SMS receiver
            applicationContext.unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            Log.e("SmsWorker", "Error in SmsWorker: ${e.message}")
            return Result.failure()
        }

        return Result.success()
    }
}
