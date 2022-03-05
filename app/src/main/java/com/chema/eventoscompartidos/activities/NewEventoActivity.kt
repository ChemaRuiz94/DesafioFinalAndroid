package com.chema.eventoscompartidos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvUsers
import com.chema.eventoscompartidos.utils.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.Rol
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class NewEventoActivity : AppCompatActivity(), OnMapReadyCallback{

    private val db = Firebase.firestore

    private lateinit var flt_btn_save_event : FloatingActionButton
    private lateinit var btn_change_location : Button
    private lateinit var btn_fecha: Button
    private lateinit var btn_hora: Button

    private lateinit var ed_txt_fecha: EditText
    private lateinit var ed_txt_titulo_evento: EditText
    private lateinit var ed_txt_hora: EditText
    private lateinit var ed_txt_ubicacion: EditText

    //*****************************************
    private var localizacionSeleccionada: String? = null
    private var ubicacionActual : LatLng? = LatLng(-33.852, 151.211) //UBI EN SIDNEY
    private lateinit var mMap: GoogleMap
    private var ubicacionCambiada = false
    //*****************************************

    private lateinit var rv : RecyclerView
    var usuarios : ArrayList<User> = ArrayList<User>()
    private lateinit var miAdapter: AdapterRvUsers


    //*****************************************
    //****************ON CREATE ********************
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_evento)

        btn_fecha = findViewById(R.id.btn_fecha)
        ed_txt_fecha = findViewById(R.id.ed_txt_fecha)
        btn_hora = findViewById(R.id.btn_hora)
        flt_btn_save_event = findViewById(R.id.flt_btn_save_event)
        btn_change_location = findViewById(R.id.btn_change_location)
        ed_txt_hora = findViewById(R.id.ed_txt_hora)
        ed_txt_titulo_evento = findViewById(R.id.ed_txt_titulo_evento)
        ed_txt_ubicacion = findViewById(R.id.ed_txt_ubicacion)
        //var a = findViewById(R.id.fragment_new_event_maps)


        //eventoActual = VariablesCompartidas.eventoActual

        //limpiamos los usuarios
        VariablesCompartidas.usuariosEventoActual.clear()

        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        cargarRV()
        cargarMapa()

        btn_fecha.setOnClickListener{
            val newFragment = DatePickerFragment(ed_txt_fecha)
            newFragment.show(supportFragmentManager, "datePicker")
        }
        btn_hora.setOnClickListener{
            val newFragment = TimePickerFragment(ed_txt_hora)
            newFragment.show(supportFragmentManager, "timePicker")
        }


        btn_change_location.setOnClickListener{
            val mapIntent = Intent(this, MapsActivity::class.java).apply {
                //putExtra("email",email)
            }
            startActivity(mapIntent)
        }

        flt_btn_save_event.setOnClickListener{
            if(check_aceptar()){
                guardar_evento()
            }else{
                Toast.makeText(this,getString(R.string.emptyCamps), Toast.LENGTH_SHORT).show()
            }
        }
    }
    //*****************************************

    override fun onResume() {
        super.onResume()
        //cargarMapa()
        cambiar_UbicacionActual()
    }

    override fun onRestart() {
        super.onRestart()
        usuarios.clear()
        VariablesCompartidas.usuariosEventoActual.clear()
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        //cargarMapa()
        cargarRV()

    }


    //*****************************************

    private fun cargarMapa() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.frm_MapLocation) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionUser}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        usuarios.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){
                //miAr.add(dc.document.toObject(User::class.java))

                var prov = ProviderType.BASIC
                var user = dc.document.data

                var us = User(
                    user.get("userId").toString(),
                    user.get("userName").toString(),
                    user.get("email").toString(),
                    user.get("phone").toString().toInt(),
                    user.get("rol") as ArrayList<Rol>,
                    user.get("activo") as Boolean,
                    user.get("img").toString(),
                    user.get("eventos") as ArrayList<Evento>
                )
                usuarios.add(us)
            }
        }
    }

    private fun cargarRV(){

        rv = findViewById(R.id.rv_usuarios)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        miAdapter = AdapterRvUsers(this, usuarios,false)
        rv.adapter = miAdapter

    }

    //***************************************
    //***************************************
    private fun check_aceptar():Boolean{
        var correcto = true

        if(ed_txt_titulo_evento.text.isEmpty()){
            correcto = false
            //Toast.makeText(this,"Ponle un titulo al evento", Toast.LENGTH_SHORT).show()
        }
        if(ed_txt_hora.text.isEmpty()){
            correcto = false
            //Toast.makeText(this,"Seleccione una hora", Toast.LENGTH_SHORT).show()
        }

        if(ed_txt_fecha.text.isEmpty()){
            correcto = false
            //Toast.makeText(this,"Seleccione una fecha", Toast.LENGTH_SHORT).show()
        }

        if(ed_txt_ubicacion.text.isEmpty()){
            correcto = false
            //Toast.makeText(this,"Seleccione una ubicacion", Toast.LENGTH_SHORT).show()
        }

        if(VariablesCompartidas.usuariosEventoActual.size<0){
            correcto = false
            //Toast.makeText(this,"Añade por lo menos un invitado", Toast.LENGTH_SHORT).show()
        }

        return correcto
    }

    private fun guardar_evento(){
        //VariablesCompartidas.usuariosEventoActual.add(VariablesCompartidas.userActual!!)
        //VariablesCompartidas.emailUsuariosEventoActual.add(VariablesCompartidas.emailUsuarioActual.toString())

        val idEv = UUID.randomUUID().toString()
        val idAsistentesHora: HashMap<UUID,Date>? = HashMap<UUID,Date>()
        var listaOpiniones: ArrayList<Opinion>? = ArrayList()

        var evento = hashMapOf(

            "idEvento" to idEv,
            "nombreEvento" to ed_txt_titulo_evento.text.toString(),
            "horaEvento" to VariablesCompartidas.horaEventoActual,
            "minEvento" to VariablesCompartidas.minutoEventoActual,
            "diaEvento" to VariablesCompartidas.diaEventoActual,
            "mesEvento" to VariablesCompartidas.mesEventoActual,
            "yearEvento" to VariablesCompartidas.yearEventoActual,
            "latUbi" to VariablesCompartidas.latEventoActual,
            "lonUbi" to VariablesCompartidas.lonEventoActual,
            "asistentes" to VariablesCompartidas.usuariosEventoActual,
            "emailAsistentes" to VariablesCompartidas.emailUsuariosEventoActual,
            "idAsistentesHora" to idAsistentesHora,
            "listaOpiniones" to listaOpiniones,

        )

        db.collection("${Constantes.collectionEvents}")
            .document(idEv) //Será la clave del documento.
            .set(evento).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.Suscesfull), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener{
                Toast.makeText(this, getString(R.string.ocurridoErrorAutenticacion), Toast.LENGTH_SHORT).show()
            }
    }

    private fun cambiar_UbicacionActual(){

        if(VariablesCompartidas.latEventoActual != null && VariablesCompartidas.lonEventoActual != null){
            ubicacionActual = LatLng(VariablesCompartidas.latEventoActual.toString().toDouble(),VariablesCompartidas.lonEventoActual.toString().toDouble())
            ed_txt_ubicacion.setText("${ubicacionActual}")

            ubicacionCambiada = true

            val ubi = LatLng(VariablesCompartidas.latEventoActual.toString().toDouble(), VariablesCompartidas.lonEventoActual.toString().toDouble())
            mMap?.addMarker(MarkerOptions().position(ubi).title("${ed_txt_titulo_evento.text}"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi,15f))
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        mMap.mapType=GoogleMap.MAP_TYPE_NORMAL
        val Kuta = LatLng(-33.852, 151.211)
        mMap?.addMarker(MarkerOptions().position(Kuta).title("${ed_txt_titulo_evento.text}"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(Kuta))

    }



}