package com.rafid.navibus

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.gms.common.api.Status
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.rafid.navibus.data.model.Halte
import com.rafid.navibus.data.repository.HalteRepository

class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomSheetHalte: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetDetail: BottomSheetBehavior<MaterialCardView>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var databaseHelper: DatabaseHelper
    
    // Data
    private val allHalteList = mutableListOf<Halte>()
    private val activeMarkers = mutableMapOf<String, Marker>()
    private var currentRoutePolyline: Polyline? = null
    private var halteKeberangkatan: Halte? = null
    private var halteTujuan: Halte? = null
    private var markerTujuan: Marker? = null

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastKnownLocation: LatLng? = null

    // Permissions
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan.", Toast.LENGTH_LONG).show()
                loadDefaultLocationData()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCsSKh4uwwhXEv8dc8xxKEb1OGetNkrqrg")
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        
        // --- UPDATE: Set User Info in Drawer Header ---
        val headerView = navigationView.getHeaderView(0)
        val tvUserNameHeader = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmailHeader = headerView.findViewById<TextView>(R.id.tvUserEmailHeader)
        
        val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString(LoginActivity.KEY_EMAIL, "user@navibus.com")
        
        tvUserEmailHeader.text = userEmail
        // Set username from the part of email before "@"
        tvUserNameHeader.text = userEmail?.split("@")?.get(0) ?: "Pengguna"
        // --- END UPDATE ---

        // Setup Bottom Sheets
        val sheetHalte = findViewById<ConstraintLayout>(R.id.bottomSheetHalte)
        bottomSheetHalte = BottomSheetBehavior.from(sheetHalte)
        bottomSheetHalte.state = BottomSheetBehavior.STATE_COLLAPSED

        val sheetDetail = findViewById<MaterialCardView>(R.id.bottomSheetDetailHalte)
        bottomSheetDetail = BottomSheetBehavior.from(sheetDetail)
        bottomSheetDetail.state = BottomSheetBehavior.STATE_HIDDEN
        
        bottomSheetDetail.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    currentRoutePolyline?.remove()
                    currentRoutePolyline = null
                    markerTujuan?.remove()
                    markerTujuan = null
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) { /* Do nothing */ }
        })
        
        setupUIControls()
        setupPlacesAutocomplete()
        
        allHalteList.addAll(HalteRepository.getAllHalte())
    }

    private fun setupUIControls() {
        val btnZoomIn = findViewById<ImageButton>(R.id.btnZoomIn)
        val btnZoomOut = findViewById<ImageButton>(R.id.btnZoomOut)
        val btnBurgerMenu = findViewById<ImageButton>(R.id.btnBurgerMenu)

        btnZoomIn?.setOnClickListener { if (::mMap.isInitialized) mMap.animateCamera(CameraUpdateFactory.zoomIn()) }
        btnZoomOut?.setOnClickListener { if (::mMap.isInitialized) mMap.animateCamera(CameraUpdateFactory.zoomOut()) }
        
        btnBurgerMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = false

        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        drawRoute3A() // Example route
        checkLocationPermission()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showHalteDetail(halte: Halte, distance: Float) {
        halteKeberangkatan = halte

        val sheetView = findViewById<MaterialCardView>(R.id.bottomSheetDetailHalte)
        val tvNama = sheetView.findViewById<TextView>(R.id.tvNamaHalteDetail)
        val tvKoridor = sheetView.findViewById<TextView>(R.id.tvKoridorDetail)
        val tvJarak = sheetView.findViewById<TextView>(R.id.tvJarakDetail)
        val actvTujuan = sheetView.findViewById<AutoCompleteTextView>(R.id.actvTujuan)
        val btnBayar = sheetView.findViewById<MaterialButton>(R.id.btnBayar)
        val btnBack = sheetView.findViewById<ImageButton>(R.id.btnBack)

        tvNama.text = halte.name
        tvKoridor.text = halte.koridor
        
        val distanceText = if (distance < 1000) "${distance.toInt()}m" else String.format("%.1f km", distance / 1000)
        val walkingMinutes = (distance / 83).toInt()
        tvJarak.text = "$distanceText • $walkingMinutes menit jalan"
        
        val halteNames = allHalteList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, halteNames)
        actvTujuan.setAdapter(adapter)
        actvTujuan.text.clear()

        btnBayar.visibility = View.GONE

        actvTujuan.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            halteTujuan = allHalteList.find { it.name == selectedName }
            
            halteTujuan?.let {
                drawRouteOnMap(halte, it)
                btnBayar.visibility = View.VISIBLE
            }
        }
        
        btnBack.setOnClickListener {
            bottomSheetDetail.state = BottomSheetBehavior.STATE_HIDDEN
        }
        
        btnBayar.setOnClickListener { 
            showPaymentDialog()
        }

        bottomSheetHalte.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetDetail.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showPaymentDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val btnSelesai = dialog.findViewById<MaterialButton>(R.id.btnSelesai)
        val tvBusCode = dialog.findViewById<TextView>(R.id.tvBusCode)
        val busCode = "TJ${(100..999).random()}"
        tvBusCode.text = "Kode Bus: $busCode"

        btnSelesai.setOnClickListener {
            val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val userEmail = sharedPreferences.getString(LoginActivity.KEY_EMAIL, "") ?: ""

            databaseHelper.addTripHistory(
                userEmail = userEmail,
                start = halteKeberangkatan?.name ?: "N/A",
                destination = halteTujuan?.name ?: "N/A",
                busCode = busCode
            )
            dialog.dismiss()
            
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            
            bottomSheetDetail.state = BottomSheetBehavior.STATE_HIDDEN
        }
        
        dialog.show()
    }

    private fun drawRouteOnMap(start: Halte, end: Halte) {
        currentRoutePolyline?.remove()
        markerTujuan?.remove()

        val polylineOptions = PolylineOptions().add(start.latLng).add(end.latLng).color(Color.CYAN).width(15f)
        currentRoutePolyline = mMap.addPolyline(polylineOptions)
        
        if (!activeMarkers.containsKey(end.id)) {
            val markerOptions = MarkerOptions().position(end.latLng).title(end.name)
            val icon = bitmapDescriptorFromVector(R.drawable.ic_bus_marker)
            if (icon != null) markerOptions.icon(icon)
            markerTujuan = mMap.addMarker(markerOptions)
        }
        
        val bounds = LatLngBounds.Builder().include(start.latLng).include(end.latLng).build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    if (lastKnownLocation == null) { 
                         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                    lastKnownLocation = currentLatLng
                    updateMapMarkersAndList(currentLatLng)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateMapMarkersAndList(center: LatLng) {
        val nearbyHalteList = mutableListOf<HalteDistance>()
        allHalteList.forEach { halte ->
            val results = FloatArray(1)
            Location.distanceBetween(center.latitude, center.longitude, halte.latLng.latitude, halte.latLng.longitude, results)
            val distance = results[0]

            if (distance <= 5000) {
                nearbyHalteList.add(HalteDistance(halte, distance))
                if (!activeMarkers.containsKey(halte.id)) addMarkerForHalte(halte)
            } else {
                activeMarkers[halte.id]?.remove()
                activeMarkers.remove(halte.id)
            }
        }

        nearbyHalteList.sortBy { it.distance }
        updateBottomSheetList(nearbyHalteList)
    }

    private fun addMarkerForHalte(halte: Halte) {
        val markerOptions = MarkerOptions().position(halte.latLng).title(halte.name).snippet(halte.koridor)
        val icon = bitmapDescriptorFromVector(R.drawable.ic_bus_marker)
        if (icon != null) markerOptions.icon(icon)

        val marker = mMap.addMarker(markerOptions)
        marker?.tag = halte.id
        if (marker != null) activeMarkers[halte.id] = marker
    }

    private fun updateBottomSheetList(sortedList: List<HalteDistance>) {
        val container = findViewById<LinearLayout>(R.id.containerHalteCards)
        container.removeAllViews()

        if (sortedList.isEmpty()) {
             val emptyView = TextView(this).apply { text = "Tidak ada halte dalam radius 5 km"; setTextColor(Color.WHITE); setPadding(32, 32, 32, 32) }
             container.addView(emptyView)
        } else {
            val inflater = LayoutInflater.from(this)
            sortedList.forEach { item ->
                val view = inflater.inflate(R.layout.item_halte_card, container, false)
                val tvNama = view.findViewById<TextView>(R.id.tvNamaHalte)
                val tvJarak = view.findViewById<TextView>(R.id.tvJarak)
                val tvKoridor = view.findViewById<TextView>(R.id.tvKoridor)
                val ivArrow = view.findViewById<ImageView>(R.id.ivArrow)
                
                tvNama.text = item.halte.name
                tvKoridor.text = item.halte.koridor
                val distanceText = if (item.distance < 1000) "${item.distance.toInt()}m" else String.format("%.1f km", item.distance / 1000)
                tvJarak.text = distanceText
                
                ivArrow.setOnClickListener { showHalteDetail(item.halte, item.distance) }
                view.setOnClickListener { 
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.halte.latLng, 16f))
                    activeMarkers[item.halte.id]?.showInfoWindow()
                }
                container.addView(view)
            }
        }
        
        if (bottomSheetHalte.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetHalte.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
            startLocationUpdates()
        }
    }

    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    updateMapMarkersAndList(latLng)
                } else {
                    loadDefaultLocationData()
                }
            }
        } catch (e: SecurityException) { }
    }

    private fun loadDefaultLocationData() {
        val defaultLocation = LatLng(-7.8332, 110.3831) // UAD 4
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
        updateMapMarkersAndList(defaultLocation)
        Toast.makeText(this, "Lokasi tidak ditemukan, menggunakan lokasi default.", Toast.LENGTH_SHORT).show()
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId) ?: return null
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun drawRoute3A() {
        val route = PolylineOptions().apply {
            addAll(HalteRepository.getRoute3APolyline())
            width(12f)
            color(Color.parseColor("#E91E63"))
            geodesic(true)
        }
        mMap.addPolyline(route)
    }

    private fun setupPlacesAutocomplete() {
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment ?: return
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setCountries("ID")
        autocompleteFragment.setHint("Cari lokasi...")
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
                    updateMapMarkersAndList(it)
                }
            }
            override fun onError(status: Status) {
                Toast.makeText(this@MainActivity, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    data class HalteDistance(val halte: Halte, val distance: Float)
}