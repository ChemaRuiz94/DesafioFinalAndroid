package com.chema.eventoscompartidos.utils

import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.User
import com.google.android.gms.maps.model.LatLng


object VariablesCompartidas {
    var userActual : User? = null

    var eventoActual : Evento? = null
    var latEventoActual: String? = null
    var lonEventoActual: String? = null
    var marcadorActual : LatLng = LatLng(-33.852, 151.211)

    var emailUsuarioActual: String? = null
    var rolUsuarioActual: String? = null

    var usuariosEventoActual: ArrayList<String> = ArrayList<String>()
}