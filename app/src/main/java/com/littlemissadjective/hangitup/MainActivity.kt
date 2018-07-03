package com.littlemissadjective.hangitup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import littlemissadjective.com.hangitup.R

abstract class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(PermissionsActivity, permissions, REQUEST_PERMISSION_CODE)

    }
}
