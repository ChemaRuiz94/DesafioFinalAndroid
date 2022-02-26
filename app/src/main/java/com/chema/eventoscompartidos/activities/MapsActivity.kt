package com.chema.eventoscompartidos.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chema.eventoscompartidos.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
//import com.chema.eventoscompartidos.activities.databinding.ActivityMapsBinding
import com.chema.eventoscompartidos.databinding.ActivityMapsBinding
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val LOCATION_REQUEST_CODE: Int = 0
    private var ubicacion_evento: String? = null
    private var tituloEvento: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        //mMap.setOnMyLocationButtonClickListener {}
        mMap.setOnMapClickListener(this)
        mMap.setOnMarkerClickListener(this)
        enableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.modo_normal -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.modo_hybrid -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.modo_satellite -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * función que usaremos a lo largo de nuestra app para comprobar si el permiso ha sido aceptado o no.
     */
    fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    /**
     * función que primero compruebe si el mapa ha sido inicializado, si no es así saldrá de la función gracias
     * a la palabra return, si por el contrario map ya ha sido inicializada, es decir que el mapa ya ha cargado,
     * pues comprobaremos los permisos.
     */
    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        if (!::mMap.isInitialized) return
        if (isPermissionsGranted()) {
            mMap.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    /**
     * Método que solicita los permisos.
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
        }
    }

    override fun onMapClick(p0: LatLng)  {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0!!).title("${tituloEvento}"))

    }

    override fun onMarkerClick(p0: Marker): Boolean {

        AlertDialog.Builder(this).setTitle("¿Seleccionar esta como ubicacion para el evento o eliminar marcador?")
            .setPositiveButton("Eliminar Marcador") { view, _ ->
                //elimina marcador
                p0.remove()
                view.dismiss()
            }.setNegativeButton("Seleccionar Ubicacion") { view, _ ->
                //guardamos la posi del marcador para usarla en otra activity
                VariablesCompartidas.marcadorActual = p0.position
                VariablesCompartidas.latEventoActual = p0.position.latitude.toString()
                VariablesCompartidas.lonEventoActual = p0.position.longitude.toString()
                finish()
                view.dismiss()
            }.create().show()
        return false
    }
}