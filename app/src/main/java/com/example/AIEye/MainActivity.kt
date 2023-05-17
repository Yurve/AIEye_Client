package com.example.AIEye

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.AIEye.sign.LoginFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setPermission()

        bluetoothOn()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, LoginFragment()).commit()

    }
    
    //블루투스 켜기
    @Throws(SecurityException::class)
    private fun bluetoothOn() {
        val bluetoothAdapter =
            ((this).getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (!bluetoothAdapter.isEnabled) {
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    //권한 확인
    private fun setPermission() {
        val permissions = ArrayList<String>().also {
            it.add(Manifest.permission.RECORD_AUDIO)
            it.add(Manifest.permission.CAMERA)
            it.add(Manifest.permission.BLUETOOTH)
            it.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
            it.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                    ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
                }
            }
        }
    }
}