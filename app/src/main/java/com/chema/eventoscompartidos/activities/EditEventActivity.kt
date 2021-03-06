package com.chema.eventoscompartidos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.chema.eventoscompartidos.utils.VariablesCompartidas
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
    private lateinit var flt_btn_edit_add_user : FloatingActionButton
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
        flt_btn_edit_add_user = findViewById(R.id.flt_btn_edit_add_user)
        btn_change_location_edit = findViewById(R.id.btn_change_location_edit)
        btn_fecha_edit = findViewById(R.id.btn_fecha_edit)
        btn_hora_edit = findViewById(R.id.btn_hora_edit)
        ed_txt_fecha_edit = findViewById(R.id.ed_txt_fecha_edit)
        ed_txt_titulo_evento_edit = findViewById(R.id.ed_txt_titulo_evento_edit)
        ed_txt_hora_edit = findViewById(R.id.ed_txt_hora_edit)
        ed_txt_ubicacion_edit = findViewById(R.id.ed_txt_ubicacion_edit)

        val bundle:Bundle? = intent.extras
        idEvento = (bundle?.getString("idEvento"))
        evento = VariablesCompartidas.eventoActual

        runBlocking {
            Log.e("preuba2","Prueba2")
            val job2 : Job = launch(context = Dispatchers.Default) {
                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colecci??n
                Log.e("preuba1",datos2.toString())
                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colecci??n y la metemos en nuestro ArrayList
            }
            //Con este m??todo el hilo principal de onCreate se espera a que la funci??n acabe y devuelva la colecci??n con los datos.
            job2.join() //Esperamos a que el m??todo acabe: https://dzone.com/articles/waiting-for-coroutines
        }


        btn_fecha_edit.setOnClickListener{
            val newFragment = DatePickerFragment(ed_txt_fecha_edit)
            newFragment.show(supportFragmentManager, "datePicker")
        }
        btn_hora_edit.setOnClickListener{
            val newFragment = TimePickerFragment(ed_txt_hora_edit)
            newFragment.show(supportFragmentManager, "timePicker")
        }

        btn_change_location_edit.setOnClickListener{

            val mapIntent = Intent(this, MapsActivity::class.java).apply {
                //putExtra("email",email)
            }
            startActivityForResult(mapIntent,1)
        }

        flt_btn_edit_event.setOnClickListener{
            guardar_evento()
        }

        flt_btn_edit_add_user.setOnClickListener{
            VariablesCompartidas.addMode = true
            val intentAddUser = Intent(this,AddUserActivity::class.java)
            startActivity(intentAddUser)
        }

        cargarUserAsist()
        cargarDatosEvento()
        cargarRV()
        cargarMapa()

    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            Log.e("preuba2","Prueba2")
            val job2 : Job = launch(context = Dispatchers.Default) {
                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colecci??n
                Log.e("preuba1",datos2.toString())
                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colecci??n y la metemos en nuestro ArrayList
            }
            //Con este m??todo el hilo principal de onCreate se espera a que la funci??n acabe y devuelva la colecci??n con los datos.
            job2.join() //Esperamos a que el m??todo acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        cargarUserAsist()
        cargarRV()
    }

    override fun onRestart() {
        super.onRestart()
        runBlocking {
            Log.e("preuba2","Prueba2")
            val job2 : Job = launch(context = Dispatchers.Default) {
                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colecci??n
                Log.e("preuba1",datos2.toString())
                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colecci??n y la metemos en nuestro ArrayList
            }
            //Con este m??todo el hilo principal de onCreate se espera a que la funci??n acabe y devuelva la colecci??n con los datos.
            job2.join() //Esperamos a que el m??todo acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        cargarUserAsist()
        cargarRV()
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


    //++++++++++++++++++++++++++++++++++++++++++++++
    private fun guardar_evento(){
        val idEv = evento!!.idEvento
        val listaOpiniones: ArrayList<Opinion>? = evento!!.listaOpiniones
        val listaUsers: ArrayList<User>? = evento!!.asistentes
        val listaEmailUsers: ArrayList<String>? = evento!!.emailAsistentes
        val idAsistentesHora = evento!!.idAsistentesHora

        var evento = hashMapOf(

            "idEvento" to idEv,
            "nombreEvento" to ed_txt_titulo_evento_edit.text.toString(),
            "horaEvento" to VariablesCompartidas.horaEventoActual,
            "minEvento" to VariablesCompartidas.minutoEventoActual,
            "diaEvento" to VariablesCompartidas.diaEventoActual,
            "mesEvento" to VariablesCompartidas.mesEventoActual,
            "yearEvento" to VariablesCompartidas.yearEventoActual,
            "latUbi" to VariablesCompartidas.latEventoActual,
            "lonUbi" to VariablesCompartidas.lonEventoActual,
            "asistentes" to listaUsers,
            "emailAsistentes" to listaEmailUsers,
            "idAsistentesHora" to idAsistentesHora,
            "listaOpiniones" to listaOpiniones,

            )

        db.collection("${Constantes.collectionEvents}")
            .document(idEv) //Ser?? la clave del documento.
            .set(evento).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.Suscesfull), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener{
                Toast.makeText(this, getString(R.string.ocurridoErrorAutenticacion), Toast.LENGTH_SHORT).show()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(VariablesCompartidas.latEventoActual != null && VariablesCompartidas.lonEventoActual != null){
            ubicacionActual = LatLng(VariablesCompartidas.latEventoActual.toString().toDouble(),VariablesCompartidas.lonEventoActual.toString().toDouble())
            ed_txt_ubicacion_edit.setText("${ubicacionActual}")

            ubicacionCambiada = true

            val ubi = LatLng(VariablesCompartidas.latEventoActual.toString().toDouble(), VariablesCompartidas.lonEventoActual.toString().toDouble())
            mMap?.addMarker(MarkerOptions().position(ubi).title("${ed_txt_titulo_evento_edit.text}"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubi,15f))
        }
    }
}