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
import android.util.Log
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
import androidx.appcompat.widget.SearchView
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.gms.common.api.Status
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.PolyUtil
import com.rafid.navibus.data.api.OsrmApiService
import com.rafid.navibus.data.model.Halte
import com.rafid.navibus.data.model.HistoryTrip
import com.rafid.navibus.data.model.OsrmResponse
import com.rafid.navibus.data.repository.HalteRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomSheetHalte: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetDetail: BottomSheetBehavior<MaterialCardView>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var searchView: SearchView
    private lateinit var searchContainerTop: LinearLayout
    private lateinit var fab: FloatingActionButton
    private lateinit var tujuanAdapter: ArrayAdapter<String>
    private lateinit var osrmApiService: OsrmApiService
    
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

    //================================================================================
    // Lifecycle Methods
    //================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        osrmApiService = retrofit.create(OsrmApiService::class.java)

        tujuanAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchContainerTop = findViewById(R.id.searchContainerTop)
        fab = findViewById(R.id.fab)

        // Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        
        val headerView = navigationView.getHeaderView(0)
        val tvUserNameHeader = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmailHeader = headerView.findViewById<TextView>(R.id.tvUserEmailHeader)
        
        val sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString(Constants.KEY_EMAIL, "user@navibus.com")
        
        tvUserEmailHeader.text = userEmail
        tvUserNameHeader.text = userEmail?.split("@")?.get(0) ?: "Pengguna"

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
                    searchContainerTop.visibility = View.VISIBLE
                    bottomSheetHalte.state = BottomSheetBehavior.STATE_COLLAPSED
                    fab.show()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) { /* Do nothing */ }
        })
        
        setupUIControls()
        allHalteList.addAll(HalteRepository.getAllHalte())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = false

        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

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
                auth.signOut()

                val sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
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

    //================================================================================
    // UI Setup
    //================================================================================

    private fun setupUIControls() {
        val btnZoomIn = findViewById<ImageButton>(R.id.btnZoomIn)
        val btnZoomOut = findViewById<ImageButton>(R.id.btnZoomOut)
        val btnBurgerMenu = findViewById<ImageButton>(R.id.btnBurgerMenu)
        searchView = findViewById(R.id.searchView)

        btnZoomIn?.setOnClickListener { if (::mMap.isInitialized) mMap.animateCamera(CameraUpdateFactory.zoomIn()) }
        btnZoomOut?.setOnClickListener { if (::mMap.isInitialized) mMap.animateCamera(CameraUpdateFactory.zoomOut()) }
        
        btnBurgerMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        fab.setOnClickListener {
            if (::mMap.isInitialized && lastKnownLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation!!, 15f))
            } else {
                getDeviceLocation()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val foundHalte = allHalteList.find { h -> h.name.contains(it, ignoreCase = true) }
                    if (foundHalte != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(foundHalte.latLng, 17f))
                        activeMarkers[foundHalte.id]?.showInfoWindow()
                    } else {
                        Toast.makeText(this@MainActivity, "Halte tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAndDisplayHalte(newText)
                return true
            }
        })
    }

    //================================================================================
    // Core App Logic: Halte Details & Routing
    //================================================================================

    private fun showHalteDetail(halte: Halte, distance: Float) {
        fab.hide()
        searchContainerTop.visibility = View.GONE 
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
        
        val distanceText = if (distance < 0) "Jarak tidak diketahui" else if (distance < 1000) "${distance.toInt()}m" else String.format("%.1f km", distance / 1000)
        val walkingMinutes = if(distance >= 0) (distance / 83).toInt() else -1
        tvJarak.text = if (walkingMinutes > 0) "$distanceText • $walkingMinutes menit jalan" else distanceText
        
        val startCorridors = halte.koridor.split(" • ").toSet()
        val possibleDestinations = allHalteList.filter { otherHalte ->
            otherHalte.id != halte.id && otherHalte.koridor.split(" • ").any { it in startCorridors }
        }
        val destinationNames = possibleDestinations.map { it.name }
        
        tujuanAdapter.clear()
        tujuanAdapter.addAll(destinationNames)
        tujuanAdapter.notifyDataSetChanged()
        
        actvTujuan.setAdapter(tujuanAdapter)
        actvTujuan.text.clear()

        btnBayar.visibility = View.GONE

        actvTujuan.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            halteTujuan = possibleDestinations.find { it.name == selectedName }
            
            halteTujuan?.let {
                markerTujuan?.remove()
                val markerOptions = MarkerOptions().position(it.latLng).title(it.name)
                val icon = bitmapDescriptorFromVector(R.drawable.ic_bus_marker)
                if (icon != null) markerOptions.icon(icon)
                markerTujuan = mMap.addMarker(markerOptions)

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

    private fun drawRouteOnMap(start: Halte, end: Halte) {
        currentRoutePolyline?.remove()

        val coordinates = "${start.latLng.longitude},${start.latLng.latitude};${end.latLng.longitude},${end.latLng.latitude}"

        osrmApiService.getRoute(coordinates).enqueue(object : Callback<OsrmResponse> {
            override fun onResponse(call: Call<OsrmResponse>, response: Response<OsrmResponse>) {
                if (response.isSuccessful) {
                    val routes = response.body()?.routes
                    if (!routes.isNullOrEmpty()) {
                        val geometry = routes[0].geometry
                        val decodedPath = PolyUtil.decode(geometry)
                        currentRoutePolyline = mMap.addPolyline(PolylineOptions().addAll(decodedPath).color(Color.CYAN).width(15f))
                        
                        val bounds = LatLngBounds.Builder().include(start.latLng).include(end.latLng).build()
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    } else {
                        Toast.makeText(this@MainActivity, "Rute tidak ditemukan dari OSRM", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal mendapatkan rute dari OSRM: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OsrmResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Gagal mendapatkan rute dari OSRM: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("OsrmApi", "Error: ", t)
            }
        })
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
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Gagal menyimpan, pengguna tidak login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val trip = HistoryTrip(
                startHalte = halteKeberangkatan?.name ?: "N/A",
                destinationHalte = halteTujuan?.name ?: "N/A",
                busCode = busCode,
                timestamp = System.currentTimeMillis().toString()
            )

            database.reference.child("trip_history").child(user.uid).push().setValue(trip)
                .addOnSuccessListener {
                    dialog.dismiss()
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    bottomSheetDetail.state = BottomSheetBehavior.STATE_HIDDEN
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan riwayat: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        
        dialog.show()
    }

    //================================================================================
    // Location Handling
    //================================================================================

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

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    if (lastKnownLocation == null) { 
                         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                    lastKnownLocation = currentLatLng
                    updateMapMarkersAndList()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    updateMapMarkersAndList()
                } else {
                    loadDefaultLocationData()
                }
            }
        } catch (e: SecurityException) { }
    }

    private fun loadDefaultLocationData() {
        val defaultLocation = LatLng(-7.8332, 110.3831) // UAD 4
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
        updateMapMarkersAndList()
        Toast.makeText(this, "Lokasi tidak ditemukan, menggunakan lokasi default.", Toast.LENGTH_SHORT).show()
    }

    //================================================================================
    // Data & List Management
    //================================================================================

    private fun updateMapMarkersAndList() {
        val center = lastKnownLocation ?: return

        allHalteList.forEach { halte ->
            val results = FloatArray(1)
            Location.distanceBetween(
                center.latitude, center.longitude,
                halte.latLng.latitude, halte.latLng.longitude,
                results
            )
            val distance = results[0]

            if (distance <= 5000) {
                if (!activeMarkers.containsKey(halte.id)) {
                    addMarkerForHalte(halte)
                }
            } else {
                if (activeMarkers.containsKey(halte.id)) {
                    activeMarkers[halte.id]?.remove()
                    activeMarkers.remove(halte.id)
                }
            }
        }

        val query = searchView.query.toString()
        filterAndDisplayHalte(query)
    }
    
    private fun filterAndDisplayHalte(query: String?) {
        val userLocation = lastKnownLocation ?: return

        val filteredList = mutableListOf<HalteDistance>()
        allHalteList.forEach { halte ->
            val nameMatches = query.isNullOrEmpty() || halte.name.contains(query, ignoreCase = true)
            if (nameMatches) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    halte.latLng.latitude, halte.latLng.longitude,
                    results
                )
                val distance = results[0]

                if (distance <= 5000) {
                    filteredList.add(HalteDistance(halte, distance))
                }
            }
        }

        filteredList.sortBy { it.distance }
        updateBottomSheetList(filteredList, query)
    }

    private fun addMarkerForHalte(halte: Halte) {
        val markerOptions = MarkerOptions().position(halte.latLng).title(halte.name).snippet(halte.koridor)
        val icon = bitmapDescriptorFromVector(R.drawable.ic_bus_marker)
        if (icon != null) markerOptions.icon(icon)

        val marker = mMap.addMarker(markerOptions)
        marker?.tag = halte.id
        if (marker != null) activeMarkers[halte.id] = marker
    }

    private fun updateBottomSheetList(sortedList: List<HalteDistance>, query: String?) {
        val container = findViewById<LinearLayout>(R.id.containerHalteCards)
        container.removeAllViews()

        if (sortedList.isEmpty()) {
             val emptyView = TextView(this).apply { 
                text = if(query.isNullOrEmpty()) "Tidak ada halte dalam radius 5 km" else "Halte tidak ditemukan"
                setTextColor(Color.WHITE); setPadding(32, 32, 32, 32) 
            }
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
                
                val distanceText = if (item.distance < 0) "" else if (item.distance < 1000) "${item.distance.toInt()}m" else String.format("%.1f km", item.distance / 1000)
                tvJarak.text = distanceText
                
                ivArrow.setOnClickListener { showHalteDetail(item.halte, item.distance) }
                view.setOnClickListener { 
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.halte.latLng, 16f))
                    activeMarkers[item.halte.id]?.showInfoWindow()
                }
                container.addView(view)
            }
        }
    }

    //================================================================================
    // Utility & Helper Functions
    //================================================================================

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId) ?: return null
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    
    //================================================================================
    // Inner Data Classes
    //================================================================================

    data class HalteDistance(val halte: Halte, val distance: Float)
}