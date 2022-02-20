package com.chema.eventoscompartidos.rv

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.google.firebase.firestore.FirebaseFirestore

class AdapterRvEventos (
    private val context: AppCompatActivity,
    private val eventos: ArrayList<Evento>
) : RecyclerView.Adapter<AdapterRvEventos.ViewHolderEvento>() {

    override fun getItemCount(): Int {
        return eventos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRvEventos.ViewHolderEvento {

        return AdapterRvEventos.ViewHolderEvento(
            LayoutInflater.from(context).inflate(R.layout.item_evento_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AdapterRvEventos.ViewHolderEvento, position: Int) {
        /*
        //holder?.item.text = this.valores!![position].toString()
        var evento: Evento = eventos[position]
        holder.nombre_evento.text = evento.nombreEvento
        holder.fecha_evento.text = evento.fecha
        holder.hora_evento.text = evento.hora


         */
    }

    //************************
    class ViewHolderEvento(view: View) : RecyclerView.ViewHolder(view) {

        //val nombre_evento = view.findViewById<TextView>(R.id.nombreEvento_item)
        //val fecha_evento = view.findViewById<TextView>(R.id.fechaEvento_item)
        //val hora_evento = view.findViewById<TextView>(R.id.txt_hora_evento)

    }
}