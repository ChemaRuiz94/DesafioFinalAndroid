package com.chema.eventoscompartidos.rv

import android.annotation.SuppressLint
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

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

        holder.itemView.setOnLongClickListener(View.OnLongClickListener {
            checkEliminar(usuario)
            false
        })

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

    private fun checkEliminar(usuario: User) {
        AlertDialog.Builder(context).setTitle(R.string.deleteThisUser)
            .setPositiveButton(R.string.delete) { view, _ ->

                val db = FirebaseFirestore.getInstance()

                checkEliminarUsersEventos(usuario)
                db.collection("${Constantes.collectionUser}").document("${usuario.userId}").delete()

                Toast.makeText(context, R.string.Suscesfull, Toast.LENGTH_SHORT).show()
                view.dismiss()
            }.setNegativeButton(R.string.cancelar) { view, _ ->//cancela
                view.dismiss()
            }.create().show()
    }

    private fun checkEliminarUsersEventos(usuario: User) {
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                delDatos(datos as QuerySnapshot?,usuario)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
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

    //+++++++++++++++++++++++++++++++
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

    private fun delDatos(datos: QuerySnapshot?, user: User) {
        VariablesCompartidas.eventosUser.clear()
        val db = FirebaseFirestore.getInstance()
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
                if(ev.emailAsistentes!!.contains(user.email)){
                    ev.emailAsistentes!!.remove(user.email)
                    ev.asistentes!!.remove(user)
                    VariablesCompartidas.eventosUser.add(ev)
                }

                /*
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
                    dc.document.get("userNameAutor").toString(),
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

                 */
            }
        }
        for (even in VariablesCompartidas.eventosUser){
            db.collection("${Constantes.collectionEvents}").document("${even.idEvento}").set(even)
        }
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