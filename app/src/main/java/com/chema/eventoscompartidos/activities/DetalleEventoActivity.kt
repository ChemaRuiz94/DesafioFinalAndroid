package com.chema.eventoscompartidos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvEventos
import com.chema.eventoscompartidos.rv.AdapterRvOpiniones
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
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

class DetalleEventoActivity : AppCompatActivity() {
    private var a = 0

    private val db = Firebase.firestore

    private var idEventoActual : String? = null
    private lateinit var opiniones : ArrayList<Opinion>

    private lateinit var rv : RecyclerView
    private lateinit var miAdapter: AdapterRvOpiniones
    private lateinit var ed_txt_comentario_detalle : EditText
    private lateinit var flt_btn_sendComentario : FloatingActionButton
    private lateinit var txt_nombreEvento_detalle : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_evento)


        val bundle:Bundle? = intent.extras
        idEventoActual = (bundle?.getString("idEventoActual"))
        opiniones = ArrayList<Opinion>()

        Log.d("CHEMA2_coment","pre run")

        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        Log.d("CHEMA2_coment","post run")


        /*
        val idOpi = UUID.randomUUID().toString()
        val fecha = Calendar.getInstance()
        val opinion = Opinion(idOpi,idEventoActual.toString(),"ejemplito",null,null,null,fecha)
        opiniones.add(opinion)

         */
        ed_txt_comentario_detalle = findViewById(R.id.ed_txt_comentario_detalle)
        flt_btn_sendComentario = findViewById(R.id.flt_btn_sendComentario)
        txt_nombreEvento_detalle = findViewById(R.id.txt_nombreEvento_detalle)
        txt_nombreEvento_detalle.setText(VariablesCompartidas.eventoActual!!.nombreEvento)



        flt_btn_sendComentario.setOnClickListener{

            if(ed_txt_comentario_detalle.text.toString().trim().isNotEmpty()){

                //guarda el comentario a firebase
                saveComentarioFirebase(crearComentario())
                refreshRV()
            }
        }
        cargarRV()
    }



    //++++++++++++CREAR OPINION DE TEXTO++++++++++++++++++
    fun crearComentario():Opinion{
        val idOpin : String = UUID.randomUUID().toString()
        val st = ed_txt_comentario_detalle.text.toString()
        val fecha = Calendar.getInstance()
        val hora = fecha.get(Calendar.HOUR)
        val min = fecha.get(Calendar.MINUTE)
        val dia = fecha.get(Calendar.DAY_OF_MONTH)
        val mes = fecha.get(Calendar.MONTH)
        val year = fecha.get(Calendar.YEAR)
        return Opinion(idOpin,idEventoActual,st,null,null,null,hora,min,dia,mes,year)
    }

    fun saveComentarioFirebase(opinion: Opinion){

        //guardamos la opinion en firebase
        db.collection("${Constantes.collectionOpiniones}")
            .document(opinion.idOpinion.toString()) //Será la clave del documento.
            .set(opinion).addOnSuccessListener {
                //Toast.makeText(this, getString(R.string.Suscesfull), Toast.LENGTH_SHORT).show()
                ed_txt_comentario_detalle.setText("")
            }.addOnFailureListener{
                Toast.makeText(this, getString(R.string.ocurridoErrorAutenticacion), Toast.LENGTH_SHORT).show()
            }

        var ev = VariablesCompartidas.eventoActual as Evento
        ev.listaOpiniones?.add(opinion)
        VariablesCompartidas.eventoActual = ev


        db.collection("${Constantes.collectionEvents}")
            .document(idEventoActual.toString())
            .set(ev).addOnSuccessListener{
                ed_txt_comentario_detalle.setText("")
            }
            .addOnFailureListener{
                Toast.makeText(this, getString(R.string.ocurridoErrorAutenticacion), Toast.LENGTH_SHORT).show()
            }
    }

    //++++++++++++++++ CARGAR OPINIONES ++++++++++++++++++++++

    private fun cargarRV(){
        rv = findViewById(R.id.rv_opiniones_detalle)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        miAdapter = AdapterRvOpiniones(this, opiniones)
        rv.adapter = miAdapter
    }

    private fun refreshRV(){

        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
        cargarRV()
    }

    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionOpiniones}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        opiniones.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                var coment : String? = null
                if(dc.document.get("comentario") != null){
                    coment = dc.document.get("comentario").toString()
                    Log.d("CHEMA2_coment","${coment}")
                }
                var foto : String? = null
                if(dc.document.get("foto") != null){
                    foto = dc.document.get("foto").toString()
                    Log.d("CHEMA2_fot","${foto}")
                }

                var longLugarInteres : String? = null
                if(dc.document.get("longLugarInteres") != null){
                    longLugarInteres = dc.document.get("longLugarInteres").toString()
                    Log.d("CHEMA2_lon","${longLugarInteres}")
                }
                var latLugarInteres : String? = null
                if(dc.document.get("latLugarInteres") != null){
                    latLugarInteres = dc.document.get("latLugarInteres").toString()
                    Log.d("CHEMA2_lat","${latLugarInteres}")
                }



                var op = Opinion(
                    dc.document.get("idOpinion").toString(),
                    dc.document.get("idEvento").toString(),
                    coment,
                    foto,
                    longLugarInteres,
                    latLugarInteres,
                    dc.document.get("horaOpinion").toString().toInt(),
                    dc.document.get("minOpinion").toString().toInt(),
                    dc.document.get("diaOpinion").toString().toInt(),
                    dc.document.get("mesOpinion").toString().toInt(),
                    dc.document.get("yearOpinion").toString().toInt()
                )
                opiniones.add(op)
                Log.d("CHEMA2_op","${op}")

            }
        }
    }

    //++++++++++++MENU++++++++++++++++++
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.event_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.foto_opinion -> a  =1
            R.id.location_opinion ->  a = 2
        }
        return super.onOptionsItemSelected(item)
    }
}