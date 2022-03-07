package com.chema.eventoscompartidos.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.databinding.FragmentOpinionFotoBinding
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvAllUser
import com.chema.eventoscompartidos.rv.AdapterRvEventos
import com.chema.eventoscompartidos.rv.AdapterRvOpinionFoto
import com.chema.eventoscompartidos.ui.home.HomeViewModel
import com.chema.eventoscompartidos.utils.Constantes
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

class OpinionFotoFragment : Fragment() {

    private lateinit var homeViewModel: OpinionFotoViewModel
    private var _binding: FragmentOpinionFotoBinding? = null

    private val db = Firebase.firestore

    private lateinit var rv : RecyclerView
    private var opiniones : ArrayList<Opinion> = ArrayList<Opinion>()
    private lateinit var miAdapter: AdapterRvOpinionFoto

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(OpinionFotoViewModel::class.java)

        _binding = FragmentOpinionFotoBinding.inflate(inflater, container, false)
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

        rv = view.findViewById(R.id.rv_opinion_fotos)


        /*
        runBlocking {
            val job : Job = launch(context = Dispatchers.Default) {
                val datos : QuerySnapshot = getDataFromFireStore() as QuerySnapshot //Obtenermos la colección
                obtenerDatos(datos as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }

         */

        cargarRV(view)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //+++++++++++++++++++++++++++++++++++++++
    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionOpiniones}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        opiniones.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                var coment : String? = null
                if(dc.document.get("comentario") != null){
                    coment = dc.document.get("comentario").toString()
                    Log.d("CHEMA2_coment","${coment}")
                }
                var foto : String? = null
                if(dc.document.get("foto") != null){
                    foto = dc.document.get("foto").toString()
                    Log.d("CHEMA2_fot","${foto}")
                }

                var longLugarInteres : String? = null
                if(dc.document.get("longLugarInteres") != null){
                    longLugarInteres = dc.document.get("longLugarInteres").toString()
                    Log.d("CHEMA2_lon","${longLugarInteres}")
                }
                var latLugarInteres : String? = null
                if(dc.document.get("latLugarInteres") != null){
                    latLugarInteres = dc.document.get("latLugarInteres").toString()
                    Log.d("CHEMA2_lat","${latLugarInteres}")
                }



                var op = Opinion(
                    dc.document.get("idOpinion").toString(),
                    dc.document.get("idEvento").toString(),
                    dc.document.get("userNameAutor").toString(),
                    coment,
                    foto,
                    latLugarInteres,
                    longLugarInteres,
                    dc.document.get("horaOpinion").toString().toInt(),
                    dc.document.get("minOpinion").toString().toInt(),
                    dc.document.get("segOpinion").toString().toInt(),
                    dc.document.get("diaOpinion").toString().toInt(),
                    dc.document.get("mesOpinion").toString().toInt(),
                    dc.document.get("yearOpinion").toString().toInt()
                )
                if(op.idEvento!!.equals(VariablesCompartidas.eventoActual!!.idEvento) && (op.foto != null)){
                    opiniones.add(op)
                }
            }
        }
    }

    private fun cargarRV(view: View){
        opiniones.clear()
        for(opi in VariablesCompartidas.opinionesEventoActual){
            if (opi.foto!=null)  {
                opiniones.add(opi)
            }
        }
        rv = view.findViewById(R.id.rv_opinion_fotos)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(view.context)
        miAdapter = AdapterRvOpinionFoto(view.context as AppCompatActivity, opiniones)
        rv.adapter = miAdapter
    }
}