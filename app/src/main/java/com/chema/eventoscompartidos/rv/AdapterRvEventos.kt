package com.chema.eventoscompartidos.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.activities.ActivatedUserHomeActivity
import com.chema.eventoscompartidos.fragment.MyEventsFragments
import com.chema.eventoscompartidos.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class AdapterRvEventos(
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

        //holder?.item.text = this.valores!![position].toString()
        val evento: Evento = eventos[position]

        val fechaDia = evento.diaEvento
        val fechaMes = evento.mesEvento
        val fechaYear = evento.yearEvento
        val fechaHora = evento.horaEvento
        val fechaMin = evento.minEvento

        holder.nombre_evento.text = evento.nombreEvento
        holder.fecha_evento.text = "${fechaDia}/${fechaMes}/${fechaYear}"
        holder.hora_evento.text = "${fechaHora}:${fechaMin}"



    }

    //************************
    class ViewHolderEvento(view: View) : RecyclerView.ViewHolder(view) {

        val nombre_evento = view.findViewById<TextView>(R.id.nombreEvento_item)
        val fecha_evento = view.findViewById<TextView>(R.id.fechaEvento_item)
        val hora_evento = view.findViewById<TextView>(R.id.txt_hora_evento)

    }
}