package com.chema.eventoscompartidos.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.databinding.FragmentEventUsersBinding
import com.chema.eventoscompartidos.databinding.FragmentOpinionFotoBinding
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvOpinionFoto
import com.chema.eventoscompartidos.rv.AdapterRvUsers
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.ProviderType
import com.chema.eventoscompartidos.utils.VariablesCompartidas
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

class EventUsersFragment : Fragment() {

    private lateinit var homeViewModel: EventUsersViewModel
    private var _binding: FragmentEventUsersBinding? = null

    private val db = Firebase.firestore

    private lateinit var rv : RecyclerView
    private lateinit var miAdapter: AdapterRvUsers
    private var usuarios : ArrayList<User> = ArrayList<User>()
    private var usuariosLlegada : ArrayList<User> = ArrayList<User>()
    private var evento : Evento? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(EventUsersViewModel::class.java)

        _binding = FragmentEventUsersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })

        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        evento = VariablesCompartidas.eventoActual!!

        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        cargarRV(view) //DA PROBELMAS A LA HORA DE CARGAR ESTE RECYCLERVIEW
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //+++++++++++++++++++++++++++++++++++++++

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
        usuariosLlegada.clear()
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
        for(user in usuarios){
            if(evento!!.idAsistentesHora!!.containsKey(user.userId)){
                usuariosLlegada.add(user)
            }
        }
    }

    private fun cargarRV(view: View){

        VariablesCompartidas.addMode = false
        VariablesCompartidas.checkMode = true //cambiamos a modo chek

        rv = view.findViewById(R.id.rv_event_users)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(view.context)
        miAdapter = AdapterRvUsers(view.context as AppCompatActivity, usuariosLlegada,false)
        rv.adapter = miAdapter
    }
}