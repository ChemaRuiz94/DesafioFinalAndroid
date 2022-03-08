package com.chema.eventoscompartidos.fragment

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.rv.AdapterRvOpinionFoto
import com.chema.eventoscompartidos.rv.AdapterRvUsers
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EventUsersFragment : Fragment() {

    private lateinit var homeViewModel: EventUsersViewModel
    private var _binding: FragmentEventUsersBinding? = null

    private val db = Firebase.firestore

    private lateinit var rv : RecyclerView
    private lateinit var miAdapter: AdapterRvUsers

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

        //cargarRV(view) //DA PROBELMAS A LA HORA DE CARGAR ESTE RECYCLERVIEW
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //+++++++++++++++++++++++++++++++++++++++


    private fun cargarRV(view: View){

        VariablesCompartidas.addMode = false
        VariablesCompartidas.checkMode = true //cambiamos a modo chek

        rv = view.findViewById(R.id.rv_event_users)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(view.context)
        miAdapter = AdapterRvUsers(view.context as AppCompatActivity, VariablesCompartidas.eventoActual!!.asistentes!!,false)
        rv.adapter = miAdapter
    }
}