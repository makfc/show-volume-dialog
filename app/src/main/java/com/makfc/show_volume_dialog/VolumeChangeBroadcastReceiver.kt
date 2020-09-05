package com.makfc.show_volume_dialog

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import kotlin.math.ceil

class VolumeChangeBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Thread {
            val stream = intent.extras?.getInt(EXTRA_VOLUME_STREAM_TYPE)
//            println("onReceive: $stream")
            if (stream != AudioManager.STREAM_MUSIC) return@Thread
            val volume = intent.extras!!["android.media.EXTRA_VOLUME_STREAM_VALUE"] as Int
            val percent = ceil(volume / 30f * 100).toInt()

            val svc = Intent(context, MainService::class.java)
            svc.putExtra("volume", "$percent%");
            context.startService(svc)
        }.start()
    }
}