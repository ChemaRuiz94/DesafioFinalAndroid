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

        cargarRV(view)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //+++++++++++++++++++++++++++++++++++++++


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