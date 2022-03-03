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
import com.chema.eventoscompartidos.utils.Auxiliar
import com.chema.eventoscompartidos.utils.VariablesCompartidas

class AdapterRvAllUser (
    private val context: AppCompatActivity,
    private val usuarios: ArrayList<User>
) : RecyclerView.Adapter<AdapterRvAllUser.ViewHolder>() {

    override fun getItemCount(): Int {
        return usuarios.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_all_user_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder?.item.text = this.valores!![position].toString()
        var usuario: User = usuarios[position]
        holder.txt_userName_allUser_item.text = usuario.userName
        holder.txt_email_allUser_item.text = usuario.email
        if(usuario.img != null){
            holder.img_item_allUser.setImageBitmap(Auxiliar.StringToBitMap(usuario.img))
        }else{
            holder.img_item_allUser.setImageResource(R.drawable.ic_no_photography)
        }



        holder.itemView.setOnClickListener {
            //Toast.makeText(context, "${usuario.userName} añadido", Toast.LENGTH_SHORT).show()
        }

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txt_userName_allUser_item = view.findViewById<TextView>(R.id.txt_userName_allUser_item)
        val txt_email_allUser_item = view.findViewById<TextView>(R.id.txt_email_allUser_item)
        val img_item_allUser = view.findViewById<ImageView>(R.id.img_item_allUser)

    }
}