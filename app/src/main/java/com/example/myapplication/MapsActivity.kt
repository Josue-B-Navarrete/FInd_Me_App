package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var client: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var url: String = "http://docs.google.com/uc?export=download&id=1ktevKf_MTQN3Fl-sfCbMbreHnXD72ItU"
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (hasLocationPermission()) {
            trackLocation()
        }
    }

    private fun playTune() {
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

        if(!mediaPlayer!!.isPlaying){
            Toast.makeText(this,"Tune is now playing",Toast.LENGTH_SHORT).show()
            try{
                mediaPlayer!!.setDataSource(url)
                mediaPlayer.prepare()
                mediaPlayer.start()
            } catch (e: IOException){
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this,"Tune has Stopped",Toast.LENGTH_SHORT).show()
            try{
                mediaPlayer.pause()
                mediaPlayer.stop()
                mediaPlayer.reset()
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun trackLocation() {

        // Create location request
        locationRequest = LocationRequest.create()
            .setInterval(5000)
            .setFastestInterval(3000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    updateMap(location)
                }
            }
        }

        client = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun updateMap(location: Location) {

        // Get current location
        val currentLatLng = LatLng(location.latitude,
            location.longitude)

        // Remove previous marker
        googleMap.clear()

        // Place a marker at the current location
        val markerOptions = MarkerOptions()
            .title("Here you are!")
            .position(currentLatLng)
        googleMap.addMarker(markerOptions)
        // Move and zoom to current location at the street level
        val update: CameraUpdate =
            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
        googleMap.animateCamera(update)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun onPause() {
        super.onPause()
        client?.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            client?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, Looper.getMainLooper())
        }
    }

    private fun hasLocationPermission(): Boolean {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            trackLocation()
        }
    }
}