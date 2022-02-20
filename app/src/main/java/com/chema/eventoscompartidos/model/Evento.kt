package com.chema.eventoscompartidos.model

import java.io.Serializable

data class Evento(
    var nombreEvento: String?,
    var fecha: String?,
    var hora: String?,
    var ubicacion: String?,
    var latUbi: String?,
    var lonUbi: String?,
    var emailAsistentes: ArrayList<String>?,
    var emailAsistentesLlegada: ArrayList<String>?,
    var asistentesLlegadaHora: ArrayList<String>?
) : Serializable
