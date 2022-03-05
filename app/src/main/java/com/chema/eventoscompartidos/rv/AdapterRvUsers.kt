package com.chema.eventoscompartidos.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.VariablesCompartidas

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

        if(editMode){
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_yes)
            holder.txt_asiste.text = ""
        }else{
            holder.img_correcto.setImageResource(R.drawable.ic_baseline_check_24_no)
            holder.nombre.text = usuario.userName

            holder.itemView.setOnClickListener {
                change_tick(holder,position)
                //Toast.makeText(context, "${usuario.userName} añadido", Toast.LENGTH_SHORT).show()
            }
        }



    }

    fun change_tick(holder: ViewHolder, position: Int){
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nombre = view.findViewById<TextView>(R.id.txt_userName_item)
        val txt_asiste = view.findViewById<TextView>(R.id.txt_asiste_item_user)
        val img_correcto = view.findViewById<ImageView>(R.id.img_item_user_correcto)

    }
}