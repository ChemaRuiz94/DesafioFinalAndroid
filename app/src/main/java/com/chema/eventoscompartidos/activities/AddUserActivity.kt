package com.chema.eventoscompartidos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvUsers
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AddUserActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    private lateinit var rv : RecyclerView
    var usuarios : ArrayList<User> = ArrayList<User>()
    var usuariosNoInvitados : ArrayList<User> = ArrayList<User>()
    private lateinit var miAdapter: AdapterRvUsers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)



        runBlocking {
            Log.e("preuba2","Prueba2")
            val job2 : Job = launch(context = Dispatchers.Default) {
                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colección
                Log.e("preuba1",datos2.toString())
                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job2.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        cargarRV()
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++
    suspend fun getDataFromFireStore2()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionUser}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos2(datos: QuerySnapshot?) {
        usuarios.clear()
        usuariosNoInvitados.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                var user = User(
                    dc.document.get("userId").toString(),
                    dc.document.get("userName").toString(),
                    dc.document.get("email").toString(),
                    dc.document.get("phone").toString().toInt(),
                    dc.document.get("rol") as ArrayList<Rol>,
                    dc.document.get("activo") as Boolean,
                    dc.document.get("img").toString(),
                    dc.document.get("eventos") as ArrayList<Evento>
                )
                usuarios.add(user)
            }
        }
    }

    //+++++++++++++++++++++++++++++++


    private fun cargarRV(){
        rv = findViewById(R.id.rv_add_users_to_event)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)
        miAdapter = AdapterRvUsers(this, usuarios,false)
        rv.adapter = miAdapter
    }

    override fun onBackPressed() {

        AlertDialog.Builder(this).setTitle(R.string.confirmSelection)
            .setPositiveButton(R.string.aceptar) { view, _ ->


                val ev = VariablesCompartidas.eventoActual!!
                val db = FirebaseFirestore.getInstance()
                db.collection("${Constantes.collectionEvents}")
                    .document("${ev.idEvento}")
                    .set(ev)

                Toast.makeText(this, R.string.Suscesfull, Toast.LENGTH_SHORT).show()
                view.dismiss()
                finish()
            }.setNegativeButton(R.string.cancelar) { view, _ ->//cancela
                view.dismiss()
            }.create().show()
        VariablesCompartidas.addMode = false
    }
}