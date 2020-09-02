package com.makfc.show_volume_dialog

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Handler.Callback
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
import android.widget.FrameLayout
import android.widget.TextView

class MainService : Service(), View.OnTouchListener {
    private lateinit var params: LayoutParams
    private lateinit var windowManager: WindowManager
    private var floatyView: View? = null
    private val timeoutHandler =
        Handler(Callback(fun(msg: Message): Boolean {
            when (msg.what) {
                1 -> (msg.obj as Runnable).run()
            }
            return false
        }))

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        addOverlayView()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        timeoutHandler.removeMessages(1)
//                    Log.d(TAG, "onReceive: $ACTION_BROADCAST")
        val volume = intent.getStringExtra("volume")
//                    Log.d(TAG, "volume: $volume")
        floatyView?.let {
            it.findViewById<TextView>(R.id.tv_volume).apply {
                text = volume
                it.alpha = 1F
                val msg = Message.obtain()
                msg.what = 1
                msg.obj = Runnable { dismiss() }
                timeoutHandler.sendMessageDelayed(msg, 2000)
            }
            if (it.windowToken == null)
                windowManager.addView(floatyView, params)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun addOverlayView() {

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
            FLAG_NOT_TOUCH_MODAL
                    or FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.END
        params.x = 58
        params.y = -750

        val interceptorLayout = FrameLayout(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatyView = inflater.inflate(R.layout.floating_view, interceptorLayout)
        floatyView?.let {
            it.setOnTouchListener(this)
//            windowManager.addView(floatyView, params)
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

    private fun dismiss() {
        floatyView?.let {
            val mills: Long = 500
            it.animate().alpha(0F).duration = mills
            Handler(Looper.getMainLooper()).postDelayed({
                if (it.windowToken != null)
                    windowManager.removeView(floatyView)
            }, mills)
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()

        Log.v(TAG, "onTouch: motionEvent: $motionEvent")

        // Kill service
//        onDestroy()
        if (motionEvent.action == MotionEvent.ACTION_OUTSIDE) {
            timeoutHandler.removeMessages(1)
            dismiss()
        }
        return true
    }

    companion object {
        private val TAG = MainService::class.java.simpleName
    }
}
