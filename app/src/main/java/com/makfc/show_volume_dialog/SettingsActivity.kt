package com.makfc.show_volume_dialog

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference


class SettingsActivity : AppCompatActivity() {
    companion object {
        const val TAG = BuildConfig.APPLICATION_ID
        private const val REQUEST_CODE = 10101
        const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
        val receiver: BroadcastReceiver = VolumeChangeBroadcastReceiver()
        var switchPreference : SwitchPreference? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    private fun registerReceiver() {
        try {
            val filter = IntentFilter()
            filter.addAction(VOLUME_CHANGED_ACTION)
            registerReceiver(receiver, filter)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun unregisterReceiver() {
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            switchPreference = preferenceManager.findPreference("show_volume_percentage")
            val sharedPreferences = preferenceScreen.sharedPreferences
            onSharedPreferenceChanged(sharedPreferences, "show_volume_percentage")
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return when (preference.key) {
                "show_volume_dialog" -> {
                    val intent = Intent(context, MyBroadcastReceiver::class.java)
                    context?.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences,
            key: String
        ) {
            val settingsActivity = activity as SettingsActivity
            when (key) {
                "show_volume_percentage" -> {
                    if (sharedPreferences.getBoolean("show_volume_percentage", false)) {
                        if (Settings.canDrawOverlays(context)) {
                            settingsActivity.registerReceiver()
                        } else {
                            settingsActivity.checkDrawOverlayPermission()
                        }
                    } else {
                        settingsActivity.unregisterReceiver()
                    }
                }
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            preferenceManager.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }
    }

    private fun checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (!Settings.canDrawOverlays(this)) {

            // If not, form up an Intent to launch the permission request
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))

            // Launch Intent, with the supplied request code
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if a request code is received that matches that which we provided for the overlay draw request
        if (requestCode == REQUEST_CODE) {

            // Double-check that the user granted it, and didn't just dismiss the request
            if (Settings.canDrawOverlays(this)) {
                registerReceiver()
            } else {
                Toast.makeText(
                    this,
                    "Sorry. Can't draw STREAM_MUSIC volume percentage overlays without permission...",
                    Toast.LENGTH_LONG
                ).show()
                switchPreference?.isChecked = false
            }
        }
    }
}