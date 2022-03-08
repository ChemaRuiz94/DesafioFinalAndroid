package com.chema.eventoscompartidos.model

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Opinion(
    var idOpinion: String?,
    var idEvento: String?,
    var userNameAutor: String?,
    var comentario: String?,
    var foto: String?,
    var latLugarInteres: String?,
    var lonLugarInteres: String?,
    var horaOpinion: Int = Calendar.HOUR,
    var minOpinion: Int = Calendar.MINUTE,
    var segOpinion: Int = Calendar.SECOND,
    var diaOpinion: Int = Calendar.DAY_OF_MONTH,
    var mesOpinion: Int = Calendar.MONTH,
    var yearOpinion: Int = Calendar.YEAR,
)
