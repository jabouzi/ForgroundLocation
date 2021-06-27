package com.skanderjabouzi.backgroundlocation

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


class LocationService() : Service(), CoroutineScope {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val helper by lazy { NotificationHelper(this) }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(NotificationHelper.NOTIFICATION_ID, helper.getNotification())
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: called.")
        location
        return START_STICKY
    }

    private fun stopService() {
        helper.updateNotification(getString(R.string.get_back))
        stopForeground(true)
        stopSelf()
    }

    // ---------------------------------- LocationRequest ------------------------------------
    // Create the location request to start receiving updates
    private val location:


    // new Google API SDK v11 uses getFusedLocationProviderClient(this)
    // Looper.myLooper tells this to repeat forever until thread is destroyed
            Unit
        private get() {

            // ---------------------------------- LocationRequest ------------------------------------
            // Create the location request to start receiving updates
            val mLocationRequestHighAccuracy = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = UPDATE_INTERVAL
                fastestInterval = FASTEST_INTERVAL
            }

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "getLocation: stopping the location service.")
                stopSelf()
                return
            }
            Log.d(TAG, "getLocation: getting location information.")
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequestHighAccuracy, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        Log.d(TAG, "onLocationResult: got location result.")
                        val location = locationResult.lastLocation
                        if (location != null) {
                            saveUserLocation(location)
                        }
                    }
                },
                Looper.myLooper()
            ) // Looper.myLooper tells this to repeat forever until thread is destroyed
        }

    private fun saveUserLocation(location: Location) {
        sendBroadcast(
            Intent(LOCATION_UPDATE)
                .putExtra(LATITUDE, location.latitude.toString())
                .putExtra(LONGITUDE, location.longitude.toString())
        )

        helper.updateNotification(
            String.format(getString(R.string.current_location, location.latitude.toString(), location.longitude.toString()))
        )
        Log.e("######", "Latitude: ${location.latitude} - Longitude: ${location.longitude}")
    }

    companion object {
        private const val TAG = "LocationService"
        private const val UPDATE_INTERVAL = (4 * 1000 /* 4 secs */).toLong()
        private const val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    }
}