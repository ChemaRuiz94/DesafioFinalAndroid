package com.chema.eventoscompartidos.model

import com.chema.eventoscompartidos.utils.ProviderType
import java.io.Serializable

data class User(var provider: ProviderType, var userName: String, var email: String, var phone: Int, var rol: String): Serializable
