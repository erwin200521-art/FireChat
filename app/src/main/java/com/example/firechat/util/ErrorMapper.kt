package com.example.firechat.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

/** Traduce excepciones de Firebase a mensajes comprensibles para el usuario. */
object ErrorMapper {

    fun message(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil"
        is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo"
        is FirebaseFirestoreException ->
            if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                "No tienes permisos para realizar esta acción"
            else "Error de base de datos. Inténtalo de nuevo"
        else -> "Ocurrió un error inesperado. Inténtalo de nuevo"
    }
}
