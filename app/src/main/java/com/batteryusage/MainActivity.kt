package com.batteryusage

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings.canDrawOverlays
import android.os.Build
import android.provider.Settings;
import android.view.WindowManager
import android.widget.Toast




class MainActivity : AppCompatActivity() {

    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start_monitor.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
            } else {
                startService()
            }
        }
    }
    fun startService() {
        startService(Intent(this@MainActivity, BatteryUsageService::class.java))
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == Activity.RESULT_OK) {
                startService()
            } else { //Permission is not available
                Toast.makeText(
                    this,
                    "Draw over other app permission not available. Closing the application",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
