package com.example.facecheckapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class LocationCheckActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var locationCallback: LocationCallback

    private lateinit var tvSubjectDetail: TextView
    private lateinit var btnScan: Button
    private lateinit var tvOutRange: TextView
    private lateinit var btnBack: ImageView

    private var subjectCode = ""
    private var className = ""
    private var classRoom = ""
    private var classTime = ""
    private var dayTime = ""
    private var classId = ""

    private val UTCC = LatLng(37.4219980, -122.0840000)
    private val RANGE_METERS = 200.0

    private var selfMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_check)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupMap()
        setupListeners()
    }

    private fun initViews() {
        tvSubjectDetail = findViewById(R.id.tvSubjectDetail)
        btnScan = findViewById(R.id.btnStartScan)
        tvOutRange = findViewById(R.id.tvOutRange)
        btnBack = findViewById(R.id.btnBack)

        subjectCode = intent.getStringExtra("subjectCode") ?: ""
        className = intent.getStringExtra("className") ?: ""
        classRoom = intent.getStringExtra("classRoom") ?: ""
        classTime = intent.getStringExtra("classTime") ?: ""
        dayTime = intent.getStringExtra("dayTime") ?: ""
        classId = intent.getStringExtra("classId") ?: ""

        btnScan.isEnabled = false
        btnScan.alpha = 0.5f

        tvSubjectDetail.text = "$subjectCode $className\nอาคาร $classRoom\n$classTime"
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        drawAreaCircle()
        checkLocationPermission()
    }

    private fun drawAreaCircle() {
        mMap.addCircle(
            CircleOptions()
                .center(UTCC)
                .radius(RANGE_METERS)
                .strokeColor(0x8800A2FF.toInt())
                .fillColor(0x2200A2FF)
                .strokeWidth(4f)
        )

        mMap.addMarker(MarkerOptions().position(UTCC).title("จุดเช็คชื่อ"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UTCC, 16f))
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
            return
        }

        mMap.isMyLocationEnabled = true
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val userLatLng = LatLng(location.latitude, location.longitude)

                selfMarker?.remove()

                selfMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("ตำแหน่งของคุณ")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f))

                checkDistance(location)
            }
        }

        fusedLocation.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )
    }

    private fun checkDistance(location: Location) {
        val distance = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            UTCC.latitude, UTCC.longitude,
            distance
        )

        if (distance[0] <= RANGE_METERS) {
            btnScan.isEnabled = true
            btnScan.alpha = 1f
            tvOutRange.text = "✓ คุณอยู่ในพื้นที่ สามารถเช็คชื่อได้"
            tvOutRange.setTextColor(getColor(R.color.green))
        } else {
            btnScan.isEnabled = false
            btnScan.alpha = 0.5f
            tvOutRange.text = "❌ คุณอยู่นอกพื้นที่ ไม่สามารถเช็คชื่อได้"
            tvOutRange.setTextColor(getColor(R.color.red))
        }
    }

    private fun setupListeners() {

        btnBack.setOnClickListener { finish() }

        btnScan.setOnClickListener {
            val intent = Intent(this, FaceScanActivity::class.java)
            intent.putExtra("classId", classId)
            intent.putExtra("subjectCode", subjectCode)
            intent.putExtra("className", className)
            intent.putExtra("classRoom", classRoom)
            intent.putExtra("classTime", classTime)
            intent.putExtra("dayTime", dayTime)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocation.removeLocationUpdates(locationCallback)
    }
}
