package com.chema.eventoscompartidos.fragment

import android.annotation.SuppressLint
import android.content.ContentValues
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
import com.chema.eventoscompartidos.databinding.FragmentEventsBinding
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvEventos
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MyEventsFragments : Fragment() {

    private val db = Firebase.firestore

    private lateinit var rv : RecyclerView
    var eventos : ArrayList<Evento> = ArrayList<Evento>()
    var misEventos : ArrayList<Evento> = ArrayList<Evento>()
    private lateinit var miAdapter: AdapterRvEventos

    private lateinit var fl_btn_new_event : FloatingActionButton
    private lateinit var fl_btn_refresh : FloatingActionButton

    private lateinit var homeViewModel: MyEventsViewModel
    private var _binding: FragmentEventsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


   /*
   //BORRAR
    val idEv = UUID.randomUUID()
    val idUse = UUID.randomUUID()
    val idOp = UUID.randomUUID()
    var fecha: Calendar = Calendar.getInstance()
    var ubi: LatLng = LatLng(0.1,0.1)
    var lat: String = (0.1).toString()
    var lon: String = (0.1).toString()
    var opinion: Opinion = Opinion(idOp.toString(),"", null,null,fecha)
    val rol = Rol(1,"user")
    var listrol = ArrayList<Rol>(1)
    var listEv = ArrayList<Evento>(1)
    var listUs = ArrayList<User>(1)
    var listOpi = ArrayList<Opinion>(1)

    */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {




        homeViewModel =
            ViewModelProvider(this).get(MyEventsViewModel::class.java)

        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //view?.let { cargarRV(it) }



        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rv_events)
        fl_btn_new_event = view.findViewById(R.id.fl_btn_new_event)
        fl_btn_refresh = view.findViewById(R.id.fl_btn_refresh)


        /*
        //BORRAR
        listrol.add(rol)
        var user: User = User(idUse.toString(),"Prueba","email",0,listrol,true,null,listEv)
        listUs.add(user)
        listOpi.add(opinion)
        var evento1 = Evento(
            idEv.toString(),
            "Ejemplo1",
            fecha.get(Calendar.HOUR),
            fecha.get(Calendar.MINUTE),
            fecha.get(Calendar.DAY_OF_MONTH),
            fecha.get(Calendar.MONTH),
            fecha.get(Calendar.YEAR),
            ubi,
            lat,
            lon,
            listUs,
            null,
            listOpi
        )
        eventos.add(evento1)
        cargarRV(view)
        //BORRAR
         */

        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

        fl_btn_new_event.setOnClickListener{
            var intetnNewEvent = Intent(requireContext(),NewEventoActivity::class.java)
            startActivity(intetnNewEvent)
        }

        fl_btn_refresh.setOnClickListener{
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


    //***************************


    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionEvents}")
                .whereArrayContains("emailAsistentes", VariablesCompartidas.emailUsuarioActual!!)
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        eventos.clear()
        misEventos.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                var al = Evento(
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
                    dc.document.get("idAsistentesHora") as HashMap<UUID,Date>?,
                    dc.document.get("listaOpiniones") as ArrayList<Opinion>?
                )
                Log.d("CHEMA2"," ASISTENTES ${al.asistentes?.count()} ")
                eventos.add(al)
            }
        }

    }

    private fun filtrarEventos(){
        for (e in eventos){
            val listAsist : ArrayList<User>? = e.asistentes
            if (listAsist != null) {
                for(asist in listAsist){
                    if(asist.email.equals(VariablesCompartidas.emailUsuarioActual)){
                        misEventos.add(e)
                    }
                }
            }
        }
    }

    //********************************************
    private fun cargarRV(view: View){

        rv = view.findViewById(R.id.rv_events)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(view.context)
        miAdapter = AdapterRvEventos(view.context as AppCompatActivity, eventos)
        rv.adapter = miAdapter
    }
}