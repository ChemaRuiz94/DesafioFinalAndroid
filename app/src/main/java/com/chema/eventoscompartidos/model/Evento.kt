package com.chema.eventoscompartidos.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Evento(
    var idEvento: String,
    var nombreEvento: String?,
    var horaEvento: Int = Calendar.HOUR,
    var minEvento: Int = Calendar.MINUTE,
    var diaEvento: Int = Calendar.DAY_OF_MONTH,
    var mesEvento: Int = Calendar.MONTH,
    var yearEvento: Int = Calendar.YEAR,
    var latUbi: String?,
    var lonUbi: String?,
    var asistentes: ArrayList<User>?,
    var emailAsistentes: ArrayList<String>?,
    var idAsistentesHora: HashMap<UUID,Date>?,
    var listaOpiniones: ArrayList<Opinion>?
) : Serializable
