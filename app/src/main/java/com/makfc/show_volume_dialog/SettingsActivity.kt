package com.makfc.show_volume_dialog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference


class SettingsActivity : AppCompatActivity() {
    companion object {
        const val TAG = BuildConfig.APPLICATION_ID
        const val CHANNEL_ID = TAG
        private const val REQUEST_CODE = 10101
        var switchPreference: SwitchPreference? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        channelAndNotify()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("SettingsActivity onDestroy")
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
                    val enabled: Boolean =
                        sharedPreferences.getBoolean("show_volume_percentage", false)
                    val flag =
                        if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    val component =
                        ComponentName(
                            (activity as SettingsActivity).baseContext,
                            VolumeChangeBroadcastReceiver::class.java
                        )
//                    context?.packageManager
//                        ?.setComponentEnabledSetting(
//                            component, flag,
//                            PackageManager.DONT_KILL_APP
//                        )
                    if (enabled && !Settings.canDrawOverlays(context)) {
                        settingsActivity.checkDrawOverlayPermission()
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
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(
                    this,
                    "Sorry. Can't draw STREAM_MUSIC volume percentage overlays without permission...",
                    Toast.LENGTH_LONG
                ).show()
                switchPreference?.isChecked = false
            }
        }
    }

    private fun channelAndNotify() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(applicationContext, SettingsActivity::class.java)
        notificationIntent.action =
            "android.intent.action.MAIN" // A string containing the action name
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentPendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setSound(null)
        with(NotificationManagerCompat.from(this)) {
            val build = builder.build()
            build.flags =
                build.flags or Notification.FLAG_NO_CLEAR
            notify(0, build)
        }
    }
}