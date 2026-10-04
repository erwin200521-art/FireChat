package com.example.firechat.data.model

/** Documento `users/{uid}` en Firestore. */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)
