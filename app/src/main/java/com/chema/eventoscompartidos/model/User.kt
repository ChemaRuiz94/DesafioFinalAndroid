package com.chema.eventoscompartidos.model

import com.chema.eventoscompartidos.utils.ProviderType

data class User(var provider: ProviderType, var userName: String, var email: String, var phone: Int, var rol: String)
