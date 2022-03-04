package com.chema.eventoscompartidos.rv

import android.content.Intent
import android.util.Log
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
import com.chema.eventoscompartidos.activities.DetalleEventoActivity
import com.chema.eventoscompartidos.fragment.MyEventsFragments
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class AdapterRvEventos(
    private val context: AppCompatActivity,
    private val eventos: ArrayList<Evento>,
    private val admin: Boolean
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


        if(admin){
            holder.itemView.setOnClickListener{
                //EDITAR
                Toast.makeText(context, "EDITAR EVENTO / DETALLE", Toast.LENGTH_SHORT).show()

            }
            holder.itemView.setOnLongClickListener(View.OnLongClickListener {
                checkEliminar(evento)
                false
            })
        }else{
            holder.itemView.setOnClickListener{
                val homeIntent = Intent(context, DetalleEventoActivity::class.java).apply {
                    VariablesCompartidas.eventoActual = evento
                    putExtra("idEventoActual",evento.idEvento)
                }
                context.startActivity(homeIntent)
            }
        }


    }

    //++++++++++++++++METODOS++++++++++
    private fun checkEliminar(evento: Evento){

        AlertDialog.Builder(context).setTitle("¿Desea eliminar este evento?")
            .setPositiveButton("Eliminar") { view, _ ->
                //elimina evento
                val db = FirebaseFirestore.getInstance()
                //eliminarOpiniones(evento)
                checkEliminarOpiniones(evento)
                db.collection("${Constantes.collectionEvents}").document("${evento.idEvento}").delete()

                Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                view.dismiss()
            }.setNegativeButton("Cancelar") { view, _ ->//cancela
                view.dismiss()
            }.create().show()

    }

    private fun checkEliminarOpiniones(evento: Evento){
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore(evento) as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }
    }

    suspend fun getDataFromFireStore(evento: Evento)  : QuerySnapshot? {

        val db = FirebaseFirestore.getInstance()
        return try{
            val data = db.collection("${Constantes.collectionOpiniones}")
                .whereEqualTo("idEvento", evento.idEvento)
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        VariablesCompartidas.opinionesEventoActual.clear()
        val db = FirebaseFirestore.getInstance()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                val idOpi = dc.document.get("idOpinion").toString()
                var coment : String? = null
                if(dc.document.get("comentario") != null){
                    coment = dc.document.get("comentario").toString()
                }
                var foto : String? = null
                if(dc.document.get("foto") != null){
                    foto = dc.document.get("foto").toString()
                }

                var longLugarInteres : String? = null
                if(dc.document.get("longLugarInteres") != null){
                    longLugarInteres = dc.document.get("longLugarInteres").toString()
                }
                var latLugarInteres : String? = null
                if(dc.document.get("latLugarInteres") != null){
                    latLugarInteres = dc.document.get("latLugarInteres").toString()
                }



                var op = Opinion(
                    idOpi,
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
                Log.d("CHEMA2_op","${op}")
                VariablesCompartidas.opinionesEventoActual.add(op)
            }
        }
        for (opin in VariablesCompartidas.opinionesEventoActual){
            db.collection("${Constantes.collectionOpiniones}").document("${opin.idOpinion}").delete()
        }
    }


    //************************
    class ViewHolderEvento(view: View) : RecyclerView.ViewHolder(view) {

        val nombre_evento = view.findViewById<TextView>(R.id.nombreEvento_item)
        val fecha_evento = view.findViewById<TextView>(R.id.fechaEvento_item)
        val hora_evento = view.findViewById<TextView>(R.id.txt_hora_evento)

    }
}