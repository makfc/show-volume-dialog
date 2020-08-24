package com.makfc.show_volume_dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.makfc.show_volume_dialog.SettingsActivity.Companion.TAG
import kotlin.math.ceil

class VolumeChangeBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
        private var toast: Toast? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val stream = intent?.extras?.getInt(EXTRA_VOLUME_STREAM_TYPE)
        if (stream != AudioManager.STREAM_MUSIC) return
        val volume = intent.extras!!["android.media.EXTRA_VOLUME_STREAM_VALUE"] as Int
        val percent = ceil(volume / 30f * 100).toInt()

        var svc = Intent(context, MainService::class.java)
        svc.putExtra("volume", "$percent%");
        context?.stopService(svc)
        context?.startService(svc)
//        Log.i(
//            TAG,
//            "Action : ${intent.action.toString()} / volume : $volume , $percent%"
//        )
//        toast?.cancel()
//        toast = Toast.makeText(context, "$percent%", Toast.LENGTH_SHORT)
////        toast?.setGravity(Gravity.END, 250, 0)
//        toast?.setGravity(Gravity.END, 50, -740)
//        toast?.show()
    }
}