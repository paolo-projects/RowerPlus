package it.paoloinfante.rowerplus.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData
import it.paoloinfante.rowerplus.receiver.FloatingWindowReceiver
import it.paoloinfante.rowerplus.receiver.RowerDataBroadcastReceiver


class FloatingWindowService : Service(), FloatingWindowReceiver.MessageListener, RowerDataBroadcastReceiver.DataReceivedListener {
    companion object {
        private const val TAG = "FloatingWindowService"
    }

    private val floatingWindowReceiver = FloatingWindowReceiver(this)
    private val rowerDataBroadcastReceiver = RowerDataBroadcastReceiver(this)

    private lateinit var windowManager: WindowManager
    private lateinit var floatingWindowLayoutParams: WindowManager.LayoutParams

    private lateinit var rootView: ConstraintLayout
    private lateinit var timeText: TextView
    private lateinit var distanceText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var spmText: TextView
    private lateinit var closeButton: ImageButton

    override fun onCreate() {
        super.onCreate()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            floatingWindowReceiver,
            IntentFilter(FloatingWindowReceiver.INTENT_KEY)
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(
            rowerDataBroadcastReceiver,
            IntentFilter(RowerDataBroadcastReceiver.INTENT_KEY)
        )

        displayFloatingWindow()
    }

    private fun displayFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        rootView = layoutInflater.inflate(R.layout.floating_widget, null) as ConstraintLayout
        timeText = rootView.findViewById(R.id.timeText)
        distanceText = rootView.findViewById(R.id.distanceText)
        caloriesText = rootView.findViewById(R.id.caloriesText)
        spmText = rootView.findViewById(R.id.spmText)
        closeButton = rootView.findViewById(R.id.closeImageButton)

        floatingWindowLayoutParams = WindowManager.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.floating_window_width),
            resources.getDimensionPixelSize(R.dimen.floating_window_height),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowTouchListener.floatWindowLayoutUpdateParam = floatingWindowLayoutParams

        floatingWindowLayoutParams.gravity = Gravity.CENTER

        floatingWindowLayoutParams.x = 0
        floatingWindowLayoutParams.y = 0

        windowManager.addView(rootView, floatingWindowLayoutParams)

        rootView.setOnTouchListener(windowTouchListener)
        closeButton.setOnClickListener(closeButtonClickListener)
    }

    private val closeButtonClickListener = { _: View ->
        stopSelf()
    }

    private val windowTouchListener = object : View.OnTouchListener {
        lateinit var floatWindowLayoutUpdateParam: WindowManager.LayoutParams
        var x = 0.0
        var y = 0.0
        var px = 0.0
        var py = 0.0

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = floatWindowLayoutUpdateParam.x.toDouble()
                    y = floatWindowLayoutUpdateParam.y.toDouble()

                    // returns the original raw X
                    // coordinate of this event
                    px = event.rawX.toDouble()

                    // returns the original raw Y
                    // coordinate of this event
                    py = event.rawY.toDouble()
                }
                MotionEvent.ACTION_MOVE -> {
                    floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                    floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()

                    // updated parameter is applied to the WindowManager
                    windowManager.updateViewLayout(rootView, floatWindowLayoutUpdateParam)
                }
            }
            return false
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onRequestServiceKill() {
        stopSelf()
    }

    override fun onDataReceived(data: TimerData) {
        timeText.text = getString(R.string.timer_format, data.timeElapsed / 60, data.timeElapsed % 60)
        distanceText.text = getString(R.string.distance_format, data.distance)
        caloriesText.text = getString(R.string.calories_format, data.calories)
        spmText.text = getString(R.string.current_rpm_format, data.currentRPM)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Destroying Floating Window")

        windowManager.removeView(rootView)
    }
}