package com.littlemissadjective.hangitup

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity

class PermissionsActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 1
    private var permissionsAccepted = false
    private val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, this.permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> permissionsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionsAccepted) finish()
    }

}