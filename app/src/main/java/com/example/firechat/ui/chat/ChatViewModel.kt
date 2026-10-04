package com.example.firechat.ui.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.model.Message
import com.example.firechat.data.repository.ChatRepository
import com.example.firechat.util.ErrorMapper
import com.example.firechat.util.Event
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel(state: SavedStateHandle) : ViewModel() {

    private val repository = ChatRepository()

    val partnerId: String = checkNotNull(state[ChatArgs.PARTNER_ID])
    val partnerName: String = state[ChatArgs.PARTNER_NAME] ?: ""
    val currentUserId: String = repository.currentUserId

    private val chatId = repository.chatIdWith(partnerId)

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    private val _uploading = MutableLiveData(false)
    val uploading: LiveData<Boolean> = _uploading

    val messages: LiveData<List<Message>> = repository.observeMessages(chatId)
        .catch {
            _error.postValue(Event(ErrorMapper.message(it)))
            emit(emptyList())
        }
        .asLiveData()

    fun sendText(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty()) return // Nunca se envían mensajes vacíos

        viewModelScope.launch {
            runCatching { repository.sendText(chatId, partnerId, text) }
                .onFailure { _error.value = Event(ErrorMapper.message(it)) }
        }
    }

    fun sendImage(uri: Uri) {
        viewModelScope.launch {
            _uploading.value = true
            runCatching { repository.sendImage(chatId, partnerId, uri) }
                .onFailure { _error.value = Event(ErrorMapper.message(it)) }
            _uploading.value = false
        }
    }
}
