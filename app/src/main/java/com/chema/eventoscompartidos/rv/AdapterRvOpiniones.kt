package com.chema.eventoscompartidos.rv

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.utils.Auxiliar
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import java.util.*
import kotlin.collections.ArrayList

class AdapterRvOpiniones (
    private val context: AppCompatActivity,
    private val opiniones: ArrayList<Opinion>
) : RecyclerView.Adapter<AdapterRvOpiniones.ViewHolder>() {

    override fun getItemCount(): Int {
        return opiniones.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRvOpiniones.ViewHolder {

        return AdapterRvOpiniones.ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_opinion_comentario_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AdapterRvOpiniones.ViewHolder, position: Int) {
        var opinion: Opinion = opiniones[position]

        //AQUI PONEMOS LA FECHA
        val hora = opinion.horaOpinion
        val min = opinion.minOpinion
        val dia = opinion.diaOpinion
        val mon = opinion.yearOpinion
        val fechaST = "${dia}/${mon} ${hora}:${min}"

        holder.txt_hora_comentario.text = (fechaST)
        holder.txt_nombreUser_detalle.text = (opinion.userNameAutor)

        if(opinion.comentario != null){
            holder.ed_txt_multiline_opinion.setText(opinion.comentario)
        }

        /*
        //AQUI OCULTAMOS UNOS COMPONENTES U OTROS SEGUN EL COMENTARIO
        if(opinion.comentario != null){
            holder.frm_map_opinion.isEnabled = false
            holder.frm_map_opinion.isVisible = false
            holder.img_opinion.isEnabled = false
            holder.img_opinion.isVisible = false
            holder.ed_txt_multiline_opinion.setText(opinion.comentario)
        }else if(opinion.foto != null){
            holder.frm_map_opinion.isEnabled = false
            holder.frm_map_opinion.isVisible = false
            holder.ed_txt_multiline_opinion.isEnabled = false
            holder.ed_txt_multiline_opinion.isVisible = false
            val imgSt = opinion.foto
            val img : Bitmap? = Auxiliar.StringToBitMap(imgSt)
            holder.img_opinion.setImageBitmap(img)
        }else{
            holder.img_opinion.isEnabled = false
            holder.img_opinion.isVisible = false
            holder.ed_txt_multiline_opinion.isEnabled = false
            holder.ed_txt_multiline_opinion.isVisible = false

        }


         */


        holder.itemView.setOnClickListener {
            //Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
        }

    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ed_txt_multiline_opinion = view.findViewById<EditText>(R.id.ed_txt_multiline_opinion)
        //val img_opinion = view.findViewById<ImageView>(R.id.img_opinion)
        //val frm_map_opinion = view.findViewById<FrameLayout>(R.id.frm_map_opinion)
        val txt_hora_comentario = view.findViewById<TextView>(R.id.txt_hora_comentario)
        val txt_nombreUser_detalle = view.findViewById<TextView>(R.id.txt_nombreUser_detalle)

    }
}