package com.skanderjabouzi.backgroundlocation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.skanderjabouzi.backgroundlocation.databinding.ActivityMainBinding

const val LOCATION_UPDATE = "LocationUpdate"
const val LATITUDE = "Latitude"
const val LONGITUDE = "Lonngitude"

class MainActivity : AppCompatActivity() {
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

    private lateinit var binding: ActivityMainBinding
    private val locationReceiver by lazy { LocationReceiver() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.button.setOnClickListener {
            if (foregroundPermissionApproved()) {
                startService()
            } else {
                requestForegroundPermissions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(locationReceiver , IntentFilter(LOCATION_UPDATE))
    }

    override fun onPause() {
        super.onPause()
        // reset foreground service receiver if it's registered
        if (locationReceiver != null) unregisterReceiver(locationReceiver)
    }

    fun startService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
    
    fun updateUi(latitude: String, longitude: String) {
        binding.textView.text = String.format(getString(R.string.current_location, latitude, longitude))
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Log.d("####", "additional rationale user didn't accept")
        } else {
            Log.d("####", "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("####", "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d("####", "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    startService()

                else -> {
                    // Permission denied.
                    Log.e("####", "Permission denied")                   

                    }
                }
            }
    }

    inner class LocationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LOCATION_UPDATE) updateUi(intent.getStringExtra(LATITUDE).toString(), intent.getStringExtra(
                LONGITUDE).toString())
        }
    }
}