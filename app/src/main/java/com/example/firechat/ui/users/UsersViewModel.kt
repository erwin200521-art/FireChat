package com.example.firechat.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.model.User
import com.example.firechat.data.repository.AuthRepository
import com.example.firechat.data.repository.UserRepository
import com.example.firechat.util.ErrorMapper
import com.example.firechat.util.Event
import com.example.firechat.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    val users: LiveData<Resource<List<User>>> = userRepository.observeUsers()
        .map<List<User>, Resource<List<User>>> { Resource.Success(it) }
        .catch { emit(Resource.Error(ErrorMapper.message(it))) }
        .asLiveData()

    private val _loggedOut = MutableLiveData<Event<Unit>>()
    val loggedOut: LiveData<Event<Unit>> = _loggedOut

    init {
        // Mantiene actualizado el token FCM de este dispositivo
        viewModelScope.launch { runCatching { userRepository.saveFcmToken() } }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { userRepository.removeFcmToken() }
            authRepository.logout()
            _loggedOut.value = Event(Unit)
        }
    }
}
