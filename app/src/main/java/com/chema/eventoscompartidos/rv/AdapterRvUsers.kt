package com.chema.eventoscompartidos.rv

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.firestore.FirebaseFirestore

class AdapterRvUsers (
    private val context: AppCompatActivity,
    private val usuarios: ArrayList<User>,
    private val editMode: Boolean
) : RecyclerView.Adapter<AdapterRvUsers.ViewHolder>() {

    override fun getItemCount(): Int {
        return usuarios.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_user_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder?.item.text = this.valores!![position].toString()
        var usuario: User = usuarios[position]
        holder.nombre.text = usuario.userName




        if(VariablesCompartidas.addMode){
            //MODO AÑADIR USER A UN EVENTO YA CREADO
            if(!VariablesCompartidas.eventoActual!!.emailAsistentes!!.contains(usuario.email)){
                holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
                holder.txt_asiste.text = "No asiste"

            }else{
                holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
                holder.txt_asiste.text = "Asiste"
            }

            holder.img_correcto.setOnClickListener {
                change_tick_add_mode(holder,usuario)
            }

        }

        else if(VariablesCompartidas.checkMode){
            //MODO CHEK PARA COMPROBAR QUE USUARIOS HAN LLEGADO A UN EVENTO YA CREADO
            if(VariablesCompartidas.eventoActual!!.idAsistentesHora!!.contains(usuario.userId)){
                holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
                holder.txt_asiste.text = "Ha llegado"
                holder.nombre.text = usuario.userName
            }else{
                holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
                holder.txt_asiste.text = "No ha llegado"
                holder.nombre.text = usuario.userName
            }

        }

        else{
            //MODO EDITAR PARA EXPULSAR DE UN EVENTO YA CREADO
            if(editMode){
                holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
                holder.txt_asiste.text = ""

                holder.itemView.setOnClickListener{
                    expulsarDelEvento(usuario)
                }
            }else{
                //MODO NORMAL, PARA AÑADIR USUARIOS A UN EVENTO SIN CREAR
                holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
                holder.nombre.text = usuario.userName

                checkAsist(holder,usuario)

                holder.itemView.setOnClickListener {
                    change_tick(holder,position)
                }
            }
        }
    }



    //+++++++++++++++++++++++++++++++++
    @SuppressLint("NotifyDataSetChanged")
    private fun expulsarDelEvento(usuario: User) {
        AlertDialog.Builder(context).setTitle(R.string.kickThisUser)
            .setPositiveButton(R.string.aceptar) { view, _ ->

                VariablesCompartidas.eventoActual!!.emailAsistentes!!.remove(usuario.email)
                VariablesCompartidas.eventoActual!!.asistentes!!.remove(usuario)
                val ev = VariablesCompartidas.eventoActual!!
                val db = FirebaseFirestore.getInstance()
                db.collection("${Constantes.collectionEvents}")
                    .document("${ev.idEvento}")
                    .set(ev)

                Toast.makeText(context, R.string.Suscesfull, Toast.LENGTH_SHORT).show()
                view.dismiss()
            }.setNegativeButton(R.string.cancelar) { view, _ ->//cancela
                view.dismiss()
            }.create().show()
        notifyDataSetChanged()
    }

    private fun checkAsist(holder: ViewHolder,usuario: User){
        if (VariablesCompartidas.emailUsuariosEventoActual.contains(usuario.email)){
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
            holder.txt_asiste.text = "Asiste"
        }else{
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
            holder.txt_asiste.text = "No asiste"

        }
    }

    private fun change_tick(holder: ViewHolder, position: Int){
        if (holder.txt_asiste.text.equals("Asiste")){
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
            holder.txt_asiste.text = "No asiste"
            VariablesCompartidas.emailUsuariosEventoActual.remove(usuarios[position].email)
            VariablesCompartidas.usuariosEventoActual.remove(usuarios[position])
        }else{
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
            holder.txt_asiste.text = "Asiste"
            VariablesCompartidas.emailUsuariosEventoActual.add(usuarios[position].email)
            VariablesCompartidas.usuariosEventoActual.add(usuarios[position])

        }
    }

    private fun change_tick_add_mode(holder: ViewHolder, usuario: User){
        if (holder.txt_asiste.text.equals("Asiste")){
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
            holder.txt_asiste.text = "No asiste"
            VariablesCompartidas.eventoActual!!.emailAsistentes!!.remove(usuario.email)
            VariablesCompartidas.eventoActual!!.asistentes!!.remove(usuario)
        }else{
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
            holder.txt_asiste.text = "Asiste"
            VariablesCompartidas.eventoActual!!.emailAsistentes!!.add(usuario.email)
            VariablesCompartidas.eventoActual!!.asistentes!!.add(usuario)

        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombre = view.findViewById<TextView>(R.id.txt_userName_item)
        val txt_asiste = view.findViewById<TextView>(R.id.txt_asiste_item_user)
        val img_correcto = view.findViewById<ImageView>(R.id.img_item_user_correcto)

    }
}