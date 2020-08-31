package com.makfc.show_volume_dialog

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.makfc.show_volume_dialog.SettingsActivity.Companion.ACTION_BROADCAST
import kotlin.math.ceil

class VolumeChangeBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Thread {
            val stream = intent.extras?.getInt(EXTRA_VOLUME_STREAM_TYPE)
            if (stream != AudioManager.STREAM_MUSIC) return@Thread
            val volume = intent.extras!!["android.media.EXTRA_VOLUME_STREAM_VALUE"] as Int
            val percent = ceil(volume / 30f * 100).toInt()

            val intent1 = Intent(ACTION_BROADCAST)
            intent1.putExtra("volume", "$percent%")
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent1)
        }.start()
    }
}