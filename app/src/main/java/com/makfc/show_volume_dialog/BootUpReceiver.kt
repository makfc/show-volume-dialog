package com.makfc.show_volume_dialog

import android.annotation.SuppressLint
import android.content.*
import androidx.preference.PreferenceManager

class BootUpReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean("show_volume_percentage", true)) {
            val filter = IntentFilter()
            filter.addAction(SettingsActivity.VOLUME_CHANGED_ACTION)
            context.registerReceiver(SettingsActivity.receiver, filter)
        } else {
            context.unregisterReceiver(SettingsActivity.receiver);
        }
    }
}