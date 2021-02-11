package com.example.rachadores

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val locationPerm = 124
    private var speedUpStartTime = 0L
    private var speedUpEndTime = 0L
    private var speedDownStartTime = 0L
    private var speedDownEndTime = 0L

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isDone :Boolean by Delegates.observable(false) {property, oldValue, newValue ->
        if(newValue == true)
        {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        askForLocationPermission()
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (!isDone) {
                    Log.d("Debug:", locationResult.lastLocation.speed.toString())
                    val speedToInt = locationResult.lastLocation.speed.toInt()
                    calcSpeed(speedToInt)
                    currentSpeedId.text = speedToInt.toString()
                }
            }
        }
    }

    private fun calcSpeed(speed: Int) {
        if(speed >= 0){
            speedUpStartTime= System.currentTimeMillis()
            speedDownEndTime= System.currentTimeMillis()

            if(speedDownStartTime != 0L) {
                val speedDownTime= speedDownEndTime - speedDownStartTime
                zeroToOneHundredId.text = (speedDownTime/1000).toString()
                speedDownStartTime= 0L
            }
        }
        else if (speed >= 100)
        {
            if(speedUpStartTime != 0L){
                speedUpEndTime = System.currentTimeMillis()
                val speedUpTime= speedUpEndTime - speedUpStartTime
                zeroToOneHundredId.text= (speedUpTime/1000).toString()
                speedUpStartTime = 0L
            }
            speedDownStartTime = System.currentTimeMillis()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun askForLocationPermission(){
        if (hasLocationPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->

                }
        }
        else
        {
            EasyPermissions.requestPermissions(
                this,
                "need permissions to find your location and calculate the speed",
                locationPerm,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }


    private fun hasLocationPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

 /*   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            val yes = "Allow"
            val no = "Deny"
            Toast.makeText(this,"onActivityResult", Toast.LENGTH_LONG).show()
        }
    }*/
}

