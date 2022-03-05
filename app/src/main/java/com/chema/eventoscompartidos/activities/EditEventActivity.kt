package com.chema.eventoscompartidos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvUsers
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.DatePickerFragment
import com.chema.eventoscompartidos.utils.TimePickerFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import java.util.*
import kotlin.collections.ArrayList

class EditEventActivity : AppCompatActivity(), OnMapReadyCallback {

    private val db = Firebase.firestore

    private lateinit var flt_btn_edit_event : FloatingActionButton
    private lateinit var btn_change_location_edit : Button
    private lateinit var btn_fecha_edit: Button
    private lateinit var btn_hora_edit: Button

    private lateinit var ed_txt_fecha_edit: EditText
    private lateinit var ed_txt_titulo_evento_edit: EditText
    private lateinit var ed_txt_hora_edit: EditText
    private lateinit var ed_txt_ubicacion_edit: EditText

    private var idEvento : String? = null
    private var evento : Evento? = null
    private var horaEvento : Int? = null
    private var minEvento : Int? = null
    private var diaEvento : Int? = null
    private var mesEvento : Int? = null
    private var yearEvento : Int? = null

    private var ubicacionActual : LatLng? = LatLng(-33.852, 151.211) //UBI EN SIDNEY
    private lateinit var mMap: GoogleMap
    private var ubicacionCambiada = false

    private lateinit var rv : RecyclerView
    var usuarios : ArrayList<User> = ArrayList<User>()
    var usuariosAsis : ArrayList<User> = ArrayList<User>()
    private lateinit var miAdapter: AdapterRvUsers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        flt_btn_edit_event = findViewById(R.id.flt_btn_edit_event)
        btn_change_location_edit = findViewById(R.id.btn_change_location_edit)
        btn_fecha_edit = findViewById(R.id.btn_fecha_edit)
        btn_hora_edit = findViewById(R.id.btn_hora_edit)
        ed_txt_fecha_edit = findViewById(R.id.ed_txt_fecha_edit)
        ed_txt_titulo_evento_edit = findViewById(R.id.ed_txt_titulo_evento_edit)
        ed_txt_hora_edit = findViewById(R.id.ed_txt_hora_edit)
        ed_txt_ubicacion_edit = findViewById(R.id.ed_txt_ubicacion_edit)

        val bundle:Bundle? = intent.extras
        idEvento = (bundle?.getString("idEvento"))


        runBlocking {
            Log.e("preuba1","Prueba1")
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                Log.e("preuba1",datos.toString())
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            Log.e("preuba1",job.toString())
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        runBlocking {
            Log.e("preuba2","Prueba2")
            val job2 : Job = launch(context = Dispatchers.Default) {
                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colección
                Log.e("preuba1",datos2.toString())
                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job2.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        btn_fecha_edit.setOnClickListener{
            val newFragment = DatePickerFragment(ed_txt_fecha_edit)
            newFragment.show(supportFragmentManager, "datePicker")
        }
        btn_hora_edit.setOnClickListener{
            val newFragment = TimePickerFragment(ed_txt_hora_edit)
            newFragment.show(supportFragmentManager, "timePicker")
        }

        cargarUserAsist()
        cargarDatosEvento()
        cargarRV()
        cargarMapa()

    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++
    private fun cargarDatosEvento() {
        horaEvento = evento!!.horaEvento
        minEvento = evento!!.minEvento
        diaEvento = evento!!.diaEvento
        mesEvento = evento!!.mesEvento
        yearEvento = evento!!.yearEvento
        ubicacionActual = LatLng(evento!!.latUbi!!.toDouble(),evento!!.lonUbi!!.toDouble())

        ed_txt_titulo_evento_edit.setText("${evento!!.nombreEvento}")
        ed_txt_fecha_edit.setText("${diaEvento}/${mesEvento}/${yearEvento}")
        ed_txt_hora_edit.setText("${horaEvento}:${minEvento}")
        ed_txt_titulo_evento_edit.setText("${evento!!.nombreEvento}")
        ed_txt_ubicacion_edit.setText("${ubicacionActual.toString()}")
    }



    //+++++++++++++++ASISTENTES+++++++++++++++++++
    private fun cargarUserAsist() {
        for(us in usuarios){
            if(evento!!.emailAsistentes!!.contains(us.email)){
                usuariosAsis.add(us)
            }
        }
    }

    private fun cargarRV(){
        rv = findViewById(R.id.rv_usuarios_edit)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        miAdapter = AdapterRvUsers(this, usuariosAsis,true)
        rv.adapter = miAdapter
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++

    private fun cargarMapa() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.frm_MapLocation_edit) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        mMap.mapType=GoogleMap.MAP_TYPE_NORMAL
        mMap?.addMarker(MarkerOptions().position(ubicacionActual!!).title("${ed_txt_titulo_evento_edit.text}"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual,11f))

    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++

    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionEvents}")
                .whereEqualTo("idEvento","${idEvento}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    suspend fun getDataFromFireStore2()  : QuerySnapshot? {
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
        evento = null

        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){
                var ev = Evento(
                    dc.document.get("idEvento").toString(),
                    dc.document.get("nombreEvento").toString(),
                    dc.document.get("horaEvento").toString().toInt(),
                    dc.document.get("minEvento").toString().toInt(),
                    dc.document.get("diaEvento").toString().toInt(),
                    dc.document.get("mesEvento").toString().toInt(),
                    dc.document.get("yearEvento").toString().toInt(),
                    dc.document.get("latUbi").toString(),
                    dc.document.get("lonUbi").toString(),
                    dc.document.get("asistentes") as ArrayList<User>?,
                    dc.document.get("emailAsistentes") as ArrayList<String>?,
                    dc.document.get("idAsistentesHora") as HashMap<UUID, Date>?,
                    dc.document.get("listaOpiniones") as ArrayList<Opinion>?
                )
                evento = ev
                Log.e("preuba1",evento.toString())
                //VariblesComunes.eventoActual = evento
            }
        }

    }


    private fun obtenerDatos2(datos: QuerySnapshot?) {
        usuarios.clear()
        usuariosAsis.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                var user = User(
                    dc.document.get("userId").toString(),
                    dc.document.get("userName").toString(),
                    dc.document.get("email").toString(),
                    dc.document.get("phone").toString().toInt(),
                    dc.document.get("rol") as ArrayList<Rol>,
                    dc.document.get("activo") as Boolean,
                    dc.document.get("img").toString(),
                    dc.document.get("eventos") as ArrayList<Evento>
                )
                usuarios.add(user)
            }
        }
    }
}