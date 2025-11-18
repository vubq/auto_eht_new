package com.example.autovubq

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import kotlin.math.pow
import kotlin.math.sqrt

class FloatingService : Service() {
    companion object {
        private const val TAG = "FloatingService"
        private const val CLICK_THRESHOLD = 10 // pixels
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        try {
            initializeFloatingView()
            setupClickListener()
            setupTouchListener()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khởi tạo floating service: ${e.message}", e)
            stopSelf()
        }
    }

    private fun initializeFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)
    }

    private fun setupClickListener() {
        val btnStop = floatingView.findViewById<ImageView>(R.id.btnStop)

        btnStop.setOnClickListener {
            try {
                AutoInstance.autoADB.stop()
                stopSelf()

                val intent = Intent(this@FloatingService, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }

                Handler(Looper.getMainLooper()).post {
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi stop: ${e.message}", e)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        val btnStop = floatingView.findViewById<ImageView>(R.id.btnStop)

        btnStop.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY

                        // Tính khoảng cách di chuyển
                        val distance = sqrt(dx.pow(2) + dy.pow(2))

                        if (distance > CLICK_THRESHOLD) {
                            isClick = false
                        }

                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()

                        try {
                            windowManager.updateViewLayout(floatingView, params)
                        } catch (e: Exception) {
                            Log.e(TAG, "Lỗi cập nhật layout: ${e.message}", e)
                        }

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            v?.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::floatingView.isInitialized) {
                windowManager.removeView(floatingView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi destroy: ${e.message}", e)
        }
    }
}