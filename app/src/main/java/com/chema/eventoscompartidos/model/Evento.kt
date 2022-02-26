package com.chema.eventoscompartidos.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Evento(
    var idEvento: String,
    var nombreEvento: String?,
    var fecha: Calendar = Calendar.getInstance(),
    var ubicacion: LatLng?,
    var latUbi: String?,
    var lonUbi: String?,
    var asistentes: ArrayList<User>?,
    var idAsistentesHora: HashMap<UUID,Date>?,
    var listaOpiniones: ArrayList<Opinion>
) : Serializable
