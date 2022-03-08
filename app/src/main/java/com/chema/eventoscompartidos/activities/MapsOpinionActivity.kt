package com.chema.eventoscompartidos.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.chema.eventoscompartidos.databinding.ActivityMapsOpinionBinding
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class MapsOpinionActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsOpinionBinding

    private val LOCATION_REQUEST_CODE: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsOpinionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_opinion) as SupportMapFragment
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
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.setOnMapClickListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        enableMyLocation()
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

    override fun onMapClick(p0: LatLng)  {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0!!).title("${VariablesCompartidas.userActual!!.userName}"))

    }

    override fun onMarkerClick(p0: Marker): Boolean {

        AlertDialog.Builder(this).setTitle("¿Seleccionar esta como ubicacion para el evento o eliminar marcador?")
            .setPositiveButton("Eliminar Marcador") { view, _ ->
                //elimina marcador
                p0.remove()
                view.dismiss()
            }.setNegativeButton("Seleccionar Ubicacion") { view, _ ->
                saveComentarioFirebase(crearComentario(null,null,p0.position.latitude.toString(),p0.position.longitude.toString()))
                finish()
                view.dismiss()
            }.create().show()
        return false
    }

    override fun onMyLocationButtonClick(): Boolean {
        //Toast.makeText(this, "Boton pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        //Toast.makeText(this, "Boton pulsado LOCATION", Toast.LENGTH_SHORT).show()
    }

    fun crearComentario(coment: String?, photo: String?, latImport: String?, longImport: String?):Opinion{
        val idOpin : String = UUID.randomUUID().toString()
        val userNameAutor : String = VariablesCompartidas.userActual!!.userName
        val fecha = Calendar.getInstance()
        val hora = fecha.get(Calendar.HOUR)
        val min = fecha.get(Calendar.MINUTE)
        val dia = fecha.get(Calendar.DAY_OF_MONTH)
        val mes = fecha.get(Calendar.MONTH)
        val year = fecha.get(Calendar.YEAR)
        return Opinion(idOpin,VariablesCompartidas.eventoActual!!.idEvento,userNameAutor,coment,photo,longImport,latImport,hora,min,dia,mes,year)
    }

    fun saveComentarioFirebase(opinion: Opinion){

        val db = Firebase.firestore

        //guardamos la opinion en firebase
        db.collection("${Constantes.collectionOpiniones}")
            .document(opinion.idOpinion.toString()) //Será la clave del documento.
            .set(opinion).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.Suscesfull), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this, getString(R.string.ERROR), Toast.LENGTH_SHORT).show()
            }

        var ev = VariablesCompartidas.eventoActual as Evento
        ev.listaOpiniones?.add(opinion)
        VariablesCompartidas.eventoActual = ev


        db.collection("${Constantes.collectionEvents}")
            .document(VariablesCompartidas.eventoActual!!.idEvento)
            .set(ev).addOnSuccessListener{
                //Toast.makeText(this, getString(R.string.Suscesfull), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, getString(R.string.ERROR), Toast.LENGTH_SHORT).show()
            }
    }
}