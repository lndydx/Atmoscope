package com.example.atmoscope.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("atmoscope_prefs", Context.MODE_PRIVATE)
            val notifEnabled = prefs.getBoolean("notif_enabled", true)
            if (notifEnabled) {
                NotificationScheduler.schedule(context)
            }
        }
    }
}