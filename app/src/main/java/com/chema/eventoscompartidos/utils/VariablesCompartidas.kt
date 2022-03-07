package com.chema.eventoscompartidos.utils

import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList


object VariablesCompartidas {
    var userActual : User? = null

    var eventoActual : Evento? = null
    var horaEventoActual : Int? = null
    var minutoEventoActual : Int? = null
    var diaEventoActual : Int? = null
    var mesEventoActual : Int? = null
    var yearEventoActual : Int? = null

    var latEventoActual: String? = null
    var lonEventoActual: String? = null
    var marcadorActual : LatLng = LatLng(-33.852, 151.211)

    var emailUsuarioActual: String? = null
    var rolUsuarioActual: String? = null

    var adminMode : Boolean = false
    var addMode : Boolean = false
    var adminLikeUserMode : Boolean = false

    var opinionesEventoActual: ArrayList<Opinion> = ArrayList<Opinion>()
    var usuariosEventoActual: ArrayList<User> = ArrayList<User>()
    var emailUsuariosEventoActual: ArrayList<String> = ArrayList<String>()
    var eventosUser: ArrayList<Evento> = ArrayList<Evento>()
}