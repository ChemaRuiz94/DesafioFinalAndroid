package com.chema.eventoscompartidos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import com.google.android.gms.maps.model.LatLng


class NewEventoActivity : AppCompatActivity(){

    private val db = Firebase.firestore

    private lateinit var flt_btn_save_event : FloatingActionButton
    private lateinit var btn_change_location : Button
    private lateinit var btn_fecha: Button
    private lateinit var btn_hora: Button
    private lateinit var btn_ubicacion: Button

    private lateinit var ed_txt_fecha: EditText
    private lateinit var ed_txt_titulo_evento: EditText
    private lateinit var ed_txt_hora: EditText
    private lateinit var ed_txt_ubicacion: EditText

    //*****************************************
    private var localizacionSeleccionada: String? = null
    private var ubicacionActual : LatLng? = LatLng(-33.852, 151.211) //UBI EN SIDNEY
    //*****************************************

    private var eventoActual : Evento? = null

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
        btn_ubicacion = findViewById(R.id.btn_ubicacion)
        flt_btn_save_event = findViewById(R.id.flt_btn_save_event)
        btn_change_location = findViewById(R.id.btn_change_location)
        ed_txt_hora = findViewById(R.id.ed_txt_hora)
        ed_txt_titulo_evento = findViewById(R.id.ed_txt_titulo_evento)
        ed_txt_ubicacion = findViewById(R.id.ed_txt_ubicacion)
        //frm_MapLocation = findViewById(R.id.frm_MapLocation)

        eventoActual = VariablesCompartidas.eventoActual

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

        btn_fecha.setOnClickListener{
            val newFragment = DatePickerFragment(ed_txt_fecha)
            newFragment.show(supportFragmentManager, "datePicker")
        }
        btn_hora.setOnClickListener{
            val newFragment = TimePickerFragment(ed_txt_hora)
            newFragment.show(supportFragmentManager, "timePicker")
        }

        btn_ubicacion.setOnClickListener{
            val mapIntent = Intent(this, MapsActivity::class.java).apply {
                //putExtra("email",email)
            }
            startActivity(mapIntent)
        }

        flt_btn_save_event.setOnClickListener{
            if(check_aceptar()){
                guardar_evento()
            }
        }
    }
    //*****************************************
    override fun onRestart() {
        super.onRestart()
        cambiar_UbicacionActual()
    }
    override fun onResume() {
        super.onResume()
        cambiar_UbicacionActual()
    }
    //*****************************************

    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionUser2}")
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

                if(!dc.document.get("provider").toString().equals("BASIC")){prov= ProviderType.GOOGLE}

//                var al = User(
//                    prov,
//                    dc.document.get("userName").toString(),
//                    dc.document.get("email").toString(),
//                    dc.document.get("phone").toString().toInt(),
//                    dc.document.get("rol").toString(),
//                    dc.document.get("img").toString()
//                )
                //Log.e(TAG, al.toString())
//                usuarios.add(al)
            }
        }
    }

    private fun cargarRV(){

        rv = findViewById(R.id.rv_usuarios)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        miAdapter = AdapterRvUsers(this, usuarios)
        rv.adapter = miAdapter

    }

    //***************************************
    //***************************************
    private fun check_aceptar():Boolean{
        var correcto = true

        if(ed_txt_titulo_evento.text.isEmpty()){
            correcto = false
            Toast.makeText(this,"Ponle un titulo al evento", Toast.LENGTH_SHORT).show()
        }
        if(ed_txt_hora.text.isEmpty()){
            correcto = false
            Toast.makeText(this,"Seleccione una hora", Toast.LENGTH_SHORT).show()
        }

        if(ed_txt_fecha.text.isEmpty()){
            correcto = false
            Toast.makeText(this,"Seleccione una fecha", Toast.LENGTH_SHORT).show()
        }

        if(ed_txt_ubicacion.text.isEmpty()){
            correcto = false
            Toast.makeText(this,"Seleccione una ubicacion", Toast.LENGTH_SHORT).show()
        }
        /*
        if(localizacionSeleccionada != null){
            correcto = false
            Toast.makeText(this,"Seleccione una ubicacion", Toast.LENGTH_SHORT).show()
        }

         */

        if(VariablesCompartidas.usuariosEventoActual.size<0){
            correcto = false
            Toast.makeText(this,"Añade por lo menos un invitado", Toast.LENGTH_SHORT).show()
        }

        return correcto
    }

    private fun guardar_evento(){
        var userLlegados = ArrayList<String>()
        var horaUserLlegados = ArrayList<String>()
        var evento = hashMapOf(

            "nombreEvento" to ed_txt_titulo_evento.text.toString(),
            "fecha" to ed_txt_fecha.text.toString().trim(),
            "hora" to ed_txt_hora.text.toString().trim(),
            "ubicacion" to VariablesCompartidas.marcadorActual,
            "latUbi" to VariablesCompartidas.latEventoActual,
            "lonUbi" to VariablesCompartidas.lonEventoActual,
            "emailAsistentes" to VariablesCompartidas.usuariosEventoActual,
            "emailAsistentesLlegada" to userLlegados,
            "asistentesLlegadaHora" to horaUserLlegados,
        )
        var id_evento = "${ed_txt_titulo_evento.text.toString()}"
        //var time = Timestamp(System.currentTimeMillis())
        //val rnds = (0..100).random()
        //id_evento += "_id${time}${rnds} "

        db.collection("${Constantes.collectionEvents}")
            .document(id_evento) //Será la clave del documento.
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
        }
    }
}