package com.batteryusage

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.TextView
import android.graphics.PixelFormat
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.view.*
import android.widget.Button
import android.support.v4.os.HandlerCompat.postDelayed




class BatteryUsageService: Service() {

    private var windowManager: WindowManager? = null
    private var batteryLayout: View? = null
    private var text: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    private var lastAction: Int = 0
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0.toFloat()
    private var initialTouchY: Float = 0.toFloat()

    private var bm: BatteryManager? = null
    private var handler: Handler = Handler();

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        //Inflate the chat head layout we created
        batteryLayout = LayoutInflater.from(this).inflate(R.layout.battery_value, null)

        batteryLayout!!.apply {
            text = findViewById<TextView>(R.id.text)
            findViewById<Button>(R.id.close_btn).setOnClickListener {
                stopSelf();
            }
            text?.setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_DOWN -> {

                        initialX = params?.x ?: 0;
                        initialY = params?.y ?: 0;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        true;
                    }
                    MotionEvent.ACTION_MOVE -> {

                        params?.x = initialX + (event.getRawX() - initialTouchX).toInt();
                        params?.y = initialY + (event.getRawY() - initialTouchY).toInt();

                        //Update the layout with new X & Y coordinate
                        windowManager?.updateViewLayout(batteryLayout, params);
                        lastAction = event.getAction();
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        lastAction = event.action
                        true
                    }
                    else -> {
                        false;
                    }
                }

            }
        }
        val LAYOUT_FLAG: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE
        }
        //Add the view to the window.
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the chat head position
        //Initially view will be added to top-left corner
        params?.gravity = Gravity.TOP or Gravity.LEFT
        params?.x = 0
        params?.y = 100

        //Add the view to the window
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager!!.addView(batteryLayout, params)

        updateBattery()
        scheduleUpdateBatteryUsage()
    }

    private val FIVE_SECONDS = 5000L

    fun scheduleUpdateBatteryUsage() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateBattery()          // this method will contain your almost-finished HTTP calls
                handler.postDelayed(this, FIVE_SECONDS)
            }
        }, FIVE_SECONDS)
    }

    fun updateBattery() {
        text?.setText("Battery " + bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW))

    }

    override fun onDestroy() {
        super.onDestroy()
        if (batteryLayout != null) windowManager!!.removeView(batteryLayout)
    }
}