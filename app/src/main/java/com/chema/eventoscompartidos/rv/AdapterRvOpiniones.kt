package com.chema.eventoscompartidos.rv

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Auxiliar
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener

import android.util.DisplayMetrics

import android.graphics.BitmapFactory
import com.google.firebase.storage.FirebaseStorage


class AdapterRvOpiniones (
    private val context: AppCompatActivity,
    private val opiniones: ArrayList<Opinion>
) : RecyclerView.Adapter<AdapterRvOpiniones.ViewHolder>() {

    /*
    private val LAYOUT_ONE = 0
    private val LAYOUT_TWO = 1
    private val LAYOUT_THREE = 2
    private var photo : Bitmap? = null


     */
    override fun getItemCount(): Int {
        return opiniones.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRvOpiniones.ViewHolder {
/*
//PARA INFLAR TRES LAYOUTS DIFERENTES EN EL MISMO ADAPTADOR
        var view: View? = null
        var viewHolder: RecyclerView.ViewHolder? = null

        view = LayoutInflater.from(context).inflate(R.layout.item_opinion_comentario_layout, parent, false)

        viewHolder = ViewHolderComentario(view)


        when(viewType){
            LAYOUT_ONE -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_opinion_comentario_layout, parent, false)

                viewHolder = ViewHolderComentario(view)
                return viewHolder
            }
            LAYOUT_TWO -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_opinon_foto_layout, parent, false)

                viewHolder = ViewHolderComentario(view)
                return viewHolder
            }
            LAYOUT_THREE -> {
                view = LayoutInflater.from(context).inflate(R.layout.item_opinon_ubicacion_layout, parent, false)

                viewHolder = ViewHolderUbicacion(view)
                return viewHolder
            }
        }

        return viewHolder

 */

        return AdapterRvOpiniones.ViewHolder(

            LayoutInflater.from(context).inflate(R.layout.item_opinion_comentario_layout, parent, false)
        )
    }

    /*
    override fun getItemViewType(position: Int): Int {
        //return if (position == 0) LAYOUT_ONE else LAYOUT_TWO
        /*
        var lay = LAYOUT_ONE
        when(position){
            0 -> {
                lay =  LAYOUT_ONE
            }
            1 -> {
                lay = LAYOUT_TWO
            }
            2 -> {
                lay = LAYOUT_THREE
            }
        }
        return lay

         */
    }

     */

    override fun onBindViewHolder(holder: AdapterRvOpiniones.ViewHolder, position: Int) {

        var opinion: Opinion = opiniones[position]

        val autor = opinion.userNameAutor

        //AQUI PONEMOS LA FECHA
        val hora = opinion.horaOpinion
        val min = opinion.minOpinion
        val dia = opinion.diaOpinion
        val mon = opinion.yearOpinion
        val fechaST = "${dia}/${mon} ${hora}:${min}"

        if(opinion.comentario != null){
            holder.txt_hora_comentario.text = (fechaST)
            holder.txt_nombreUser_detalle.text = (opinion.userNameAutor)
            holder.ed_txt_multiline_opinion.setText(opinion.comentario)
            if(opinion.userNameAutor.equals(VariablesCompartidas.userActual!!.userName)){
                holder.ed_txt_multiline_opinion.setOnLongClickListener(View.OnLongClickListener {
                    checkEliminar(opinion)
                    false
                })
            }

        }

        /*
        when(holder.itemViewType){
            LAYOUT_ONE -> {
                var viewHolderComent = holder as ViewHolderComentario

                viewHolderComent.txt_hora_comentario.text = (fechaST)
                viewHolderComent.txt_nombreUser_detalle.text = (autor)

                viewHolderComent.ed_txt_multiline_opinion.setText(opinion.comentario)
                if(opinion.userNameAutor.equals(VariablesCompartidas.userActual!!.userName)){
                    viewHolderComent.ed_txt_multiline_opinion.setOnLongClickListener(View.OnLongClickListener {
                        checkEliminar(opinion)
                        false
                    })
                }
            }
            LAYOUT_TWO -> {
                if(opinion.foto != null){
                    photo = getFotoStorage(opinion.foto!!)

                }

                var viewHolderFoto= holder as ViewHolderFoto

                viewHolderFoto.txt_hora_comentario.text = (fechaST)
                viewHolderFoto.txt_nombreUser_detalle.text = (autor)

                viewHolderFoto.img_opinion.setImageBitmap(photo)
            }
            LAYOUT_THREE -> {
                var viewHolderUbicacion = holder as ViewHolderUbicacion

                viewHolderUbicacion.txt_hora_comentario.text = (fechaST)
                viewHolderUbicacion.txt_nombreUser_detalle.text = (autor)
            }
        }

         */
    }


    //++++++++++++++++++++ ELIMINAR ++++++++++++++++++++++++++++++++++
    private fun checkEliminar(opinion: Opinion) {
        AlertDialog.Builder(context).setTitle(R.string.deleteThisOpinion)
            .setPositiveButton(R.string.delete) { view, _ ->

                val db = FirebaseFirestore.getInstance()
                checkEliminarOpinionesDelEvento(opinion)
                db.collection("${Constantes.collectionOpiniones}").document("${opinion.idOpinion}").delete()

                Toast.makeText(context, R.string.Suscesfull, Toast.LENGTH_SHORT).show()
                view.dismiss()
            }.setNegativeButton(R.string.cancelar) { view, _ ->//cancela
                view.dismiss()
            }.create().show()
    }

    private fun checkEliminarOpinionesDelEvento(opinion: Opinion){
        Log.d("PRUEBA1", "pre runBlock2")
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                delDatos(datos as QuerySnapshot?,opinion)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
    }

    suspend fun getDataFromFireStore()  : QuerySnapshot? {

        val db = FirebaseFirestore.getInstance()
        return try{
            val data = db.collection("${Constantes.collectionEvents}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun delDatos(datos: QuerySnapshot?, opinion: Opinion) {
        val db = FirebaseFirestore.getInstance()
        var eventos = ArrayList<Evento>()
        var opis = ArrayList<Opinion>()
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
                    dc.document.get("idAsistentesHora") as HashMap<String, Calendar>?,
                    dc.document.get("listaOpiniones") as ArrayList<Opinion>?
                )
                if(ev.listaOpiniones!!.contains(opinion)){
                    opis = ev.listaOpiniones!!
                    opis.remove(opinion)
                    ev.listaOpiniones = opis
                    Log.d("PRUEBA1","ID OPI ${opinion.idOpinion}")
                    eventos.add(ev)
                }
            }
        }
        for (event in eventos){
            db.collection("${Constantes.collectionEvents}").document("${event.idEvento}").set(event)
        }
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ed_txt_multiline_opinion = view.findViewById<EditText>(R.id.ed_txt_multiline_opinion)
        val txt_hora_comentario = view.findViewById<TextView>(R.id.txt_hora_comentario)
        val txt_nombreUser_detalle = view.findViewById<TextView>(R.id.txt_nombreUser_detalle)

    }

    /* TRES CLASES VIEWHOLDER
    class ViewHolderComentario(view: View) : RecyclerView.ViewHolder(view) {

        val ed_txt_multiline_opinion = view.findViewById<EditText>(R.id.ed_txt_multiline_opinion)
        val txt_hora_comentario = view.findViewById<TextView>(R.id.txt_hora_comentario)
        val txt_nombreUser_detalle = view.findViewById<TextView>(R.id.txt_nombreUser_detalle)

    }

    class ViewHolderFoto(view: View) : RecyclerView.ViewHolder(view) {

        val img_opinion = view.findViewById<ImageView>(R.id.um_foto_comentario)
        val txt_hora_comentario = view.findViewById<TextView>(R.id.txt_hora_comentario_foto)
        val txt_nombreUser_detalle = view.findViewById<TextView>(R.id.txt_nombreUser_detalle_foto)

    }

    class ViewHolderUbicacion(view: View) : RecyclerView.ViewHolder(view) {

        val frm_map_opinion = view.findViewById<FrameLayout>(R.id.frm_map_opinion)
        val txt_hora_comentario = view.findViewById<TextView>(R.id.txt_hora_comentario_ubicacion)
        val txt_nombreUser_detalle = view.findViewById<TextView>(R.id.txt_nombreUser_detalle_ubicacion)

    }

     */
}