package com.makfc.show_volume_dialog

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.widget.FrameLayout
import android.widget.TextView


class MainService : Service(), View.OnTouchListener {

    private lateinit var windowManager: WindowManager
    private var floatyView: View? = null
    private var volume: String? = ""

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        volume = intent?.getStringExtra("volume")
        Log.d(TAG, "volume: $volume")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        addOverlayView()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addOverlayView() {

        val params: LayoutParams
        val layoutParamsType: Int =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                LayoutParams.TYPE_PHONE
            }

        params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            layoutParamsType,
            FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.END
        params.x = 58
        params.y = -750

        val interceptorLayout = object : FrameLayout(this) {

            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                Log.d(TAG, "event: $event")

                // Only fire on the ACTION_DOWN event, or you'll get two events (one for _DOWN, one for _UP)
                if (event.action == KeyEvent.ACTION_DOWN) {

                    // Check if the HOME button is pressed
                    if (event.keyCode == KeyEvent.KEYCODE_BACK) {

                        Log.v(TAG, "BACK Button Pressed")

                        // As we've taken action, we'll return true to prevent other apps from consuming the event as well
                        return true
                    }
                }

                // Otherwise don't intercept the event
                return super.dispatchKeyEvent(event)
            }
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatyView = inflater.inflate(R.layout.floating_view, interceptorLayout)
        floatyView?.let {
            it.findViewById<TextView>(R.id.tv_volume).apply {
                text = volume
                setOnTouchListener(this@MainService)
                Handler(Looper.getMainLooper()).postDelayed({
                    it.animate().alpha(0F).duration = 500
                    Handler(Looper.getMainLooper()).postDelayed({
                        onDestroy()
                    }, 500)
                }, 2000)
            }
            windowManager.addView(floatyView, params)
        } ?: run {
            Log.e(
                TAG,
                "Layout Inflater Service is null; can't inflate and display R.layout.floating_view"
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatyView?.let {
            windowManager.removeView(it)
            floatyView = null
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()

        Log.v(TAG, "onTouch...")

        // Kill service
        onDestroy()

        return true
    }

    companion object {
        private val TAG = MainService::class.java.simpleName
    }
}
