package com.makfc.show_volume_dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val TAG = BuildConfig.APPLICATION_ID
        private const val REQUEST_CODE = 10101
        const val ACTION_BROADCAST = "Broadcast"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (Settings.canDrawOverlays(this)) {

            // Launch service right away - the user has already previously granted permission
            launchMainService()
        } else {

            // Check that the user has granted permission, and prompt them if not
            checkDrawOverlayPermission()
        }
    }

    private fun launchMainService() {
        val svc = Intent(this, MainService::class.java)
        startService(svc)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            return when (preference.key) {
                "show" -> {
                    val intent = Intent(context, MyBroadcastReceiver::class.java)
                    context?.sendBroadcast(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun checkDrawOverlayPermission() {

        // Checks if app already has permission to draw overlays
        if (!Settings.canDrawOverlays(this)) {

            // If not, form up an Intent to launch the permission request
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))

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

                // Launch the service
                launchMainService()
            } else {

                Toast.makeText(this, "Sorry. Can't draw STREAM_MUSIC volume percentage overlays without permission...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}