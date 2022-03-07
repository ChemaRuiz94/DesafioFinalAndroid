package com.chema.eventoscompartidos.rv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class AdapterRvOpinionFoto (
    private val context: AppCompatActivity,
    private val opiniones: ArrayList<Opinion>
) : RecyclerView.Adapter<AdapterRvOpinionFoto.ViewHolder>() {

    val storage = Firebase.storage("gs://eventoscompartidos-43253.appspot.com")

    override fun getItemCount(): Int {
        return opiniones.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRvOpinionFoto.ViewHolder {


        return AdapterRvOpinionFoto.ViewHolder(

            LayoutInflater.from(context).inflate(R.layout.item_opinon_foto_layout, parent, false)
        )
    }


    override fun onBindViewHolder(holder: AdapterRvOpinionFoto.ViewHolder, position: Int) {

        var opinion: Opinion = opiniones[position]

        val autor = opinion.userNameAutor

        //AQUI PONEMOS LA FECHA
        val hora = opinion.horaOpinion
        val min = opinion.minOpinion
        val dia = opinion.diaOpinion
        val mon = opinion.yearOpinion
        val fechaST = "${dia}/${mon} ${hora}:${min}"



        if(opinion.foto != null){
            holder.txt_hora_comentario.text = (fechaST)
            holder.txt_nombreUser_detalle.text = (opinion.userNameAutor)
            val img : Bitmap? = getFotoStorage(opinion.foto!!)
            holder.img_opinion.setImageBitmap(img)
            /*
            if(opinion.userNameAutor.equals(VariablesCompartidas.userActual!!.userName)){
                holder.ed_txt_multiline_opinion.setOnLongClickListener(View.OnLongClickListener {
                    //eliminar foto
                    false
                })
            }

             */

        }


    }


    private fun getFotoStorage(idFoto: String): Bitmap?{
        //val storageRef = storage.reference
        val storageRef = Firebase.storage.reference
        //val imagesRef = storageRef.child("/images/${idFoto}")
        var bitmap : Bitmap? = null
        //val ONE_MEGABYTE = (1024 * 1024).toLong()

        storageRef.child("images/${idFoto}").getBytes(Long.MAX_VALUE).addOnSuccessListener {
            bitmap = Auxiliar.getBitmap(it)
        }.addOnFailureListener {
            Toast.makeText(context,"Se produjo un ERROR al bajar la imagen",Toast.LENGTH_SHORT).show()
        }

        return bitmap
    }



    private fun getFoto(idFoto: String) : Bitmap?{
        val storageRef = storage.reference
        val imagesRef = storageRef.child("/images/${idFoto}")
        var bitmap : Bitmap? = null
        val ONE_MEGABYTE = (1024 * 1024).toLong()
        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener{

            bitmap = BitmapFactory.decodeByteArray(it,0,it.size)
            Log.d("BITMAP","${bitmap}")
        }.addOnFailureListener{
            Toast.makeText(context, R.string.ERROR, Toast.LENGTH_SHORT).show()
        }
        return bitmap
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val img_opinion = view.findViewById<ImageView>(R.id.img_foto_comentario)
        val txt_hora_comentario = view.findViewById<TextView>(R.id.txt_hora_comentario_foto)
        val txt_nombreUser_detalle = view.findViewById<TextView>(R.id.txt_nombreUser_detalle_foto)

    }
}