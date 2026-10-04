package com.example.firechat.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.repository.AuthRepository
import com.example.firechat.util.ErrorMapper
import com.example.firechat.util.Resource
import com.example.firechat.util.Validators
import kotlinx.coroutines.launch

/** Errores por campo; `null` significa que el campo es válido. */
data class FormErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null
) {
    val hasErrors get() = listOf(name, email, password, confirmPassword).any { it != null }
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _formErrors = MutableLiveData(FormErrors())
    val formErrors: LiveData<FormErrors> = _formErrors

    private val _authState = MutableLiveData<Resource<Unit>?>()
    val authState: LiveData<Resource<Unit>?> = _authState

    fun isLoggedIn() = repository.isLoggedIn()

    fun login(email: String, password: String) {
        val errors = FormErrors(
            email = Validators.email(email),
            password = Validators.required(password, "La contraseña es obligatoria")
        )
        _formErrors.value = errors
        if (errors.hasErrors) return

        execute { repository.login(email, password) }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        val errors = FormErrors(
            name = Validators.name(name),
            email = Validators.email(email),
            password = Validators.password(password),
            confirmPassword = Validators.confirmPassword(password, confirmPassword)
        )
        _formErrors.value = errors
        if (errors.hasErrors) return

        execute { repository.register(name, email, password) }
    }

    private fun execute(block: suspend () -> Unit) {
        _authState.value = Resource.Loading
        viewModelScope.launch {
            _authState.value = try {
                block()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(ErrorMapper.message(e))
            }
        }
    }
}
