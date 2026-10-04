package com.example.firechat.util

import android.util.Patterns

/** Reglas de validación de formularios. Devuelven el mensaje de error o `null` si es válido. */
object Validators {

    fun name(value: String): String? = when {
        value.isBlank() -> "El nombre es obligatorio"
        value.trim().length < 2 -> "El nombre debe tener al menos 2 caracteres"
        else -> null
    }

    fun email(value: String): String? = when {
        value.isBlank() -> "El correo es obligatorio"
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> "El formato del correo no es válido"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "La contraseña es obligatoria"
        value.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        value.none { it.isLetter() } || value.none { it.isDigit() } ->
            "La contraseña debe contener letras y números"
        else -> null
    }

    fun required(value: String, message: String = "Campo obligatorio"): String? =
        if (value.isEmpty()) message else null

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Confirma tu contraseña"
        password != confirm -> "Las contraseñas no coinciden"
        else -> null
    }
}
