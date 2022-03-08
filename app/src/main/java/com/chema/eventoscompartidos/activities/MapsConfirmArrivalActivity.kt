package com.chema.eventoscompartidos.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chema.eventoscompartidos.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.chema.eventoscompartidos.databinding.ActivityMapsConfirmArrivalBinding
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.HashMap

class MapsConfirmArrivalActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsConfirmArrivalBinding


    private lateinit var circulo : Circle
    private val LOCATION_REQUEST_CODE: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsConfirmArrivalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_confirm) as SupportMapFragment
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

        val evento = LatLng(VariablesCompartidas.eventoActual!!.latUbi.toString().toDouble(), VariablesCompartidas.eventoActual!!.lonUbi.toString().toDouble())
        val mark = mMap.addMarker(MarkerOptions().position(evento).title("${VariablesCompartidas.eventoActual!!.nombreEvento}"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mark!!.getPosition(), 14F))

        mMap.setOnMyLocationClickListener(this)
        enableMyLocation()
        pintarCirculoCentro()
    }

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
     * función que usaremos a lo largo de nuestra app para comprobar si el permiso ha sido aceptado o no.
     */
    fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

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


    fun pintarCirculoCentro(){
        //val markerCIFP = LatLng(38.69332,-4.10860)
        val evento = LatLng(VariablesCompartidas.eventoActual!!.latUbi.toString().toDouble(), VariablesCompartidas.eventoActual!!.lonUbi.toString().toDouble())
        circulo = mMap.addCircle(CircleOptions().run{
            center(evento)
            //radius(9.0)
            radius(100.0)
            strokeColor(Color.BLUE)
            fillColor(Color.TRANSPARENT)
        })
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Boton pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        //Toast.makeText(this, "Boton pulsado LOCATION", Toast.LENGTH_SHORT).show()
        check_llegadaUsarioEvento(p0)
    }

    private fun check_llegadaUsarioEvento(p0: Location){
        val results = FloatArray(1)

        Location.distanceBetween(
            p0.latitude,
            p0.longitude,
            VariablesCompartidas.eventoActual!!.latUbi.toString().toDouble(),
            VariablesCompartidas.eventoActual!!.lonUbi.toString().toDouble(),
            results
        )
        val distanceInMeters = results[0]
        val isWithin100m = distanceInMeters < 100

        if(isWithin100m){

            llegadaUsarioEvento()
        }else{
            Toast.makeText(this,"No estas en la ubicacion del evento",Toast.LENGTH_SHORT).show()
        }
    }

    private fun llegadaUsarioEvento(){
        val fecha = Calendar.getInstance()

        var eventoAntiguo : Evento? = VariablesCompartidas.eventoActual
        var eventoActualizdo : Evento = eventoAntiguo!!

        if(eventoAntiguo.idAsistentesHora!!.contains(VariablesCompartidas.userActual!!.userId)){
            Toast.makeText(this, "Ya has confirmado tu llegada al evento", Toast.LENGTH_SHORT).show()
        }else{
            eventoActualizdo.idAsistentesHora!!.put("${VariablesCompartidas.userActual!!.userId.toString()}", fecha)


            val db = FirebaseFirestore.getInstance()
            db.collection("${Constantes.collectionEvents}")
                .document("${VariablesCompartidas.eventoActual!!.idEvento}") //Será la clave del documento.
                .set(eventoActualizdo).addOnSuccessListener {
                    Toast.makeText(this, "Has llegado al evento", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener{
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
        }

    }
}