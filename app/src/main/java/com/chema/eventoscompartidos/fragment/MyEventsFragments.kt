package com.chema.eventoscompartidos.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.databinding.FragmentEventsBinding
import com.chema.eventoscompartidos.databinding.FragmentProfileBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyEventsFragments : Fragment() {

    private lateinit var rv_events : RecyclerView
    private lateinit var fl_btn_new_event : FloatingActionButton

    private lateinit var homeViewModel: MyEventsViewModel
    private var _binding: FragmentEventsBinding? = null

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

        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

         */

        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_events = view.findViewById(R.id.rv_events)
        fl_btn_new_event = view.findViewById(R.id.fl_btn_new_event)

        fl_btn_new_event.setOnClickListener{
            Toast.makeText(requireContext(),"Nuevo EVENTO",Toast.LENGTH_SHORT).show()
        }

        //cargarDatosUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}