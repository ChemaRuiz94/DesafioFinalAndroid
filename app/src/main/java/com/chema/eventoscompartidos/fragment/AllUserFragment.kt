package com.chema.eventoscompartidos.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.activities.NewEventoActivity
import com.chema.eventoscompartidos.databinding.FragmentAllUserBinding
import com.chema.eventoscompartidos.databinding.FragmentEventsBinding
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvAllUser
import com.chema.eventoscompartidos.rv.AdapterRvEventos
import com.chema.eventoscompartidos.rv.AdapterRvUsers
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.ProviderType
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AllUserFragment : Fragment() {

    private val db = Firebase.firestore

    private lateinit var rv : RecyclerView
    private var usuarios : ArrayList<User> = ArrayList<User>()
    private lateinit var miAdapter: AdapterRvAllUser

    private lateinit var fl_btn_refresh_all_users : FloatingActionButton

    private lateinit var homeViewModel: MyEventsViewModel
    private var _binding: FragmentAllUserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        homeViewModel =
            ViewModelProvider(this).get(MyEventsViewModel::class.java)

        _binding = FragmentAllUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rv_all_user)
        fl_btn_refresh_all_users = view.findViewById(R.id.fl_btn_refresh_all_users)


        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }



        fl_btn_refresh_all_users.setOnClickListener{
            runBlocking {
                val job : Job = launch(context = Dispatchers.Default) {
                    val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                    obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
                }
                //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
                job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
            }


            cargarRV(view)
        }

        cargarRV(view)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //++++++++++++++++++++++++++++++++++
    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionUser}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        usuarios.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){
                //miAr.add(dc.document.toObject(User::class.java))

                var prov = ProviderType.BASIC
                var user = dc.document.data

                var us = User(
                    user.get("userId").toString(),
                    user.get("userName").toString(),
                    user.get("email").toString(),
                    user.get("phone").toString().toInt(),
                    user.get("rol") as ArrayList<Rol>,
                    user.get("activo") as Boolean,
                    user.get("img").toString(),
                    user.get("eventos") as ArrayList<Evento>
                )
                usuarios.add(us)
                Log.d("CHEMA2","${us.email}")
            }
        }
    }

    private fun cargarRV(view: View){

        rv = view.findViewById(R.id.rv_all_user)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(view.context)
        miAdapter = AdapterRvAllUser(view.context as AppCompatActivity,  usuarios)
        rv.adapter = miAdapter

    }
}