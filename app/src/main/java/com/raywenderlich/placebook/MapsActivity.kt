package com.raywenderlich.placebook

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.raywenderlich.placebook.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    //private var locationRequest: LocationRequest?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        //setContentView(binding.root)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap=googleMap
        getCurrentLocation()
        mMap.setOnPoiClickListener{
            Toast.makeText(this,it.name,Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLocationClient(){
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermissions(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION)
    }

    companion object{
        private const val REQUEST_LOCATION = 1
        private const val TAG="MapsActivity"
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        }

        //if (locationRequest == null) {
        //    locationRequest = LocationRequest.create()
        //    locationRequest?.let { locationRequest ->
        //        locationRequest.priority=LocationRequest?.PRIORITY_HIGH_ACCURACY
        //        locationRequest.interval = 5000
        //        locationRequest.fastestInterval=1000
        //        val locationCallback = object : LocationCallback() {
        //            override fun onLocationResult(locationequest: LocationRequest?) {
        //                getCurrentLocation()
        //            }
        //        }
        //        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)
        //    }
        //}

        mMap.isMyLocationEnabled=true

        fusedLocationClient.lastLocation.addOnCompleteListener {
            val location = it.result
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
               // mMap.clear()
               // mMap.addMarker(MarkerOptions().position(latLng).title("You are here!"))
                val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                mMap.moveCamera(update)
            } else {
                Log.e(TAG, "No location found")
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<String>, grantResults:IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }
}