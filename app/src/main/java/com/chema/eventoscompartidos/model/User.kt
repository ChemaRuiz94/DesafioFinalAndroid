package com.chema.eventoscompartidos.model

import android.graphics.Bitmap
import com.chema.eventoscompartidos.utils.ProviderType
import java.io.Serializable
import java.util.*

data class User(
    var userId: String?,
    var userName: String,
    var email: String,
    var phone: Int,
    var rol: ArrayList<Rol>,
    var activo: Boolean,
    var img: String?,
    var eventos: ArrayList<Evento>,
): Serializable
