package com.example.firechat.ui.chat

/** Claves de los extras del Intent de ChatActivity (también leídas por el ViewModel vía SavedStateHandle). */
object ChatArgs {
    const val PARTNER_ID = "partner_id"
    const val PARTNER_NAME = "partner_name"
}

/** Conversación actualmente visible; evita notificar mensajes de un chat que el usuario ya está viendo. */
object ActiveChat {
    @Volatile
    var partnerId: String? = null
}
