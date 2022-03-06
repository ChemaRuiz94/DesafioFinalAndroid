package com.chema.eventoscompartidos.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvEventos
import com.chema.eventoscompartidos.rv.AdapterRvOpiniones
import com.chema.eventoscompartidos.utils.Auxiliar
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
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class DetalleEventoActivity : AppCompatActivity() {
    private var a = 0

    private val db = Firebase.firestore

    private var idEventoActual : String? = null
    private lateinit var opiniones : ArrayList<Opinion>
    private var opinionesOrdenadas : ArrayList<Opinion> = ArrayList()
    //private var opinionesFechas = mutableListOf<Date>()

    private lateinit var rv : RecyclerView
    private lateinit var miAdapter: AdapterRvOpiniones
    private lateinit var ed_txt_comentario_detalle : EditText
    private lateinit var flt_btn_sendComentario : FloatingActionButton
    private lateinit var txt_nombreEvento_detalle : TextView

    private var photo: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_evento)


        val bundle:Bundle? = intent.extras
        idEventoActual = (bundle?.getString("idEventoActual"))
        opiniones = ArrayList<Opinion>()


        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }


        ed_txt_comentario_detalle = findViewById(R.id.ed_txt_comentario_detalle)
        flt_btn_sendComentario = findViewById(R.id.flt_btn_sendComentario)
        txt_nombreEvento_detalle = findViewById(R.id.txt_nombreEvento_detalle)
        txt_nombreEvento_detalle.setText(VariablesCompartidas.eventoActual!!.nombreEvento)



        flt_btn_sendComentario.setOnClickListener{
            val text  = ed_txt_comentario_detalle.text.toString()
            if(text.trim().isNotEmpty()){

                //guarda el comentario a firebase
                saveComentarioFirebase(crearComentario(text,null,null,null))
                refreshRV()
            }
        }
        opinionesOrdenadas.clear()
        opinionesOrdenadas =  ordenarOpiniones()
        cargarRV()
    }

    private fun ordenarOpiniones() : ArrayList<Opinion>{
        val opiniOrdenadas = opiniones.sortedWith(compareBy({ it.yearOpinion }, { it.mesOpinion },{it.diaOpinion}, { it.horaOpinion },{it.minOpinion}))


        for (opi in opiniOrdenadas){
            opinionesOrdenadas.add(opi)
        }
        return opinionesOrdenadas
    }


    //++++++++++++CREAR OPINION DE TEXTO++++++++++++++++++
    fun crearComentario(coment: String?, photo: String?, latImport: String?, longImport: String?):Opinion{
        val idOpin : String = UUID.randomUUID().toString()
        val userNameAutor : String = VariablesCompartidas.userActual!!.userName
        val fecha = Calendar.getInstance()
        val hora = fecha.get(Calendar.HOUR)
        val min = fecha.get(Calendar.MINUTE)
        val dia = fecha.get(Calendar.DAY_OF_MONTH)
        val mes = fecha.get(Calendar.MONTH)
        val year = fecha.get(Calendar.YEAR)
        return Opinion(idOpin,idEventoActual,userNameAutor,coment,photo,longImport,latImport,hora,min,dia,mes,year)
    }

    fun crearOpinionFoto(){
        val idOpin : String = UUID.randomUUID().toString()
        val userNameAutor : String = VariablesCompartidas.userActual!!.userName
        val fecha = Calendar.getInstance()
        val hora = fecha.get(Calendar.HOUR)
        val min = fecha.get(Calendar.MINUTE)
        val dia = fecha.get(Calendar.DAY_OF_MONTH)
        val mes = fecha.get(Calendar.MONTH)
        val year = fecha.get(Calendar.YEAR)
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
        miAdapter = AdapterRvOpiniones(this, opinionesOrdenadas)
        //miAdapter.onCreateViewHolder(rv,2)
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
        opinionesOrdenadas.clear()
        opinionesOrdenadas =  ordenarOpiniones()
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
                    dc.document.get("userNameAutor").toString(),
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
                if(op.idEvento!!.equals(idEventoActual)){
                    opiniones.add(op)
                }
            }
        }
    }

    //++++++++++++CONFIRMAR ASIST++++++++++++++++++
    private fun confirmAsist(){
        val asistIntent = Intent(this, MapsConfirmArrivalActivity::class.java).apply {
            //putExtra("nombreEvento",evento.nombreEvento)
            //VariblesComunes.eventoActual = evento
        }
        startActivity(asistIntent)
    }

    //++++++++++++MENU++++++++++++++++++
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.event_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.foto_opinion ->  cambiarFoto()
            R.id.location_opinion ->  a = 2
            R.id.confirmArrival ->  confirmAsist()
        }
        return super.onOptionsItemSelected(item)
    }

    fun addFotoComent(){

    }

    fun cambiarFoto() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.chosePhoto))
            .setMessage(getString(R.string.strMensajeElegirFoto))
            .setPositiveButton(getString(R.string.strCamara)) { view, _ ->
                hacerFoto()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strGaleria)) { view, _ ->
                elegirDeGaleria()
                view.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun elegirDeGaleria() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Seleccione una imagen"),
            Constantes.CODE_GALLERY
        )
    }

    private fun hacerFoto() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                Constantes.CODE_CAMERA
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constantes.CODE_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constantes.CODE_CAMERA -> {

                if (resultCode == Activity.RESULT_OK) {
                    photo = data?.extras?.get("data") as Bitmap
                    saveComentarioFirebase(crearComentario(null,Auxiliar.ImageToString(photo!!),null,null))
                    refreshRV()
                }
            }

            Constantes.CODE_GALLERY -> {
                if (resultCode === Activity.RESULT_OK) {
                    val selectedImage = data?.data
                    val selectedPath: String? = selectedImage?.path
                    if (selectedPath != null) {
                        var imageStream: InputStream? = null
                        try {
                            imageStream = selectedImage.let {
                                this.contentResolver.openInputStream(
                                    it
                                )
                            }
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                        val bmp = BitmapFactory.decodeStream(imageStream)
                        photo = Bitmap.createScaledBitmap(bmp, 200, 300, true)
                        saveComentarioFirebase(crearComentario(null,Auxiliar.ImageToString(photo!!),null,null))
                        refreshRV()
                    }
                }
            }
        }
    }
}