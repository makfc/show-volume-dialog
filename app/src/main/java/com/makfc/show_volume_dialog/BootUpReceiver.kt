package com.makfc.show_volume_dialog

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager

class BootUpReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val enabled: Boolean =
            prefs.getBoolean("show_volume_percentage", false)
        val flag =
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val component =
            ComponentName(
                context,
                VolumeChangeBroadcastReceiver::class.java
            )
        context.packageManager
            ?.setComponentEnabledSetting(
                component, flag,
                PackageManager.DONT_KILL_APP
            )
    }
}