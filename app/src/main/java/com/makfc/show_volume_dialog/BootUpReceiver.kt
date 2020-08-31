package com.makfc.show_volume_dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val intent = Intent(context, MainService::class.java)
        context.startService(intent)
    }
}