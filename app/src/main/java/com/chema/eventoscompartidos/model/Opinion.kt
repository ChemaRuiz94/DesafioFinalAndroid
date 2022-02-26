package com.chema.eventoscompartidos.model

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Opinion(
    var idOpinion: UUID = UUID.randomUUID(),
    var comentario: String?,
    var foto: String?,
    var lugarInteres: LatLng?,
    var fecha: Calendar = Calendar.getInstance()
)
