package com.chema.eventoscompartidos.rv

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Auxiliar
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

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

        if(usuario.activo){
            holder.switchActivated.isChecked = true

        }else{
            holder.switchActivated.setText(R.string.Disabled)
        }

        holder.switchActivated.setOnClickListener{
            AlertDialog.Builder(context).setTitle(R.string.changeActivateUser)
                .setPositiveButton(R.string.change) { view, _ ->
                    changeActivatedUser(usuario,holder)
                    view.dismiss()
                }.setNegativeButton(R.string.cancelar) { view, _ ->//cancela
                    if(holder.switchActivated.isChecked){
                        holder.switchActivated.isChecked = false
                    }else{
                        holder.switchActivated.isChecked = true
                    }
                    view.dismiss()
                }.create().show()
        }


    }

    private fun changeActivatedUser(user: User, holder: ViewHolder){
        if(user.activo){
            user.activo = false
            updateFirebaseUserActivated(user)
            holder.switchActivated.setText(R.string.Disabled)
            Toast.makeText(context,R.string.Disabled,Toast.LENGTH_SHORT).show()
        }else{
            user.activo = true
            updateFirebaseUserActivated(user)
            holder.switchActivated.setText(R.string.Activated)
            Toast.makeText(context,R.string.Activated,Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFirebaseUserActivated(user: User){
        val db = FirebaseFirestore.getInstance()

        db.collection("${Constantes.collectionUser}")
            .document(user.userId.toString())
            .set(user)
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txt_userName_allUser_item = view.findViewById<TextView>(R.id.txt_userName_allUser_item)
        val txt_email_allUser_item = view.findViewById<TextView>(R.id.txt_email_allUser_item)
        val img_item_allUser = view.findViewById<ImageView>(R.id.img_item_allUser)
        val switchActivated = view.findViewById<Switch>(R.id.switchActivated)

    }
}