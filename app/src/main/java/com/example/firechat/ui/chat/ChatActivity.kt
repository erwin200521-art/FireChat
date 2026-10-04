package com.example.firechat.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firechat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: MessagesAdapter

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.sendImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = viewModel.partnerName
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MessagesAdapter(viewModel.currentUserId)
        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.rvMessages.adapter = adapter

        setupInput()
        observeViewModel()
    }

    private fun setupInput() {
        // El botón enviar solo se habilita cuando hay texto (evita mensajes vacíos)
        binding.btnSend.isEnabled = false
        binding.etMessage.doAfterTextChanged {
            binding.btnSend.isEnabled = !it.isNullOrBlank()
        }
        binding.btnSend.setOnClickListener {
            viewModel.sendText(binding.etMessage.text.toString())
            binding.etMessage.text?.clear()
        }
        binding.btnAttach.setOnClickListener { pickImage.launch("image/*") }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.rvMessages.scrollToPosition(messages.lastIndex)
            }
        }
        viewModel.uploading.observe(this) { uploading ->
            binding.uploadProgress.visibility = if (uploading) View.VISIBLE else View.GONE
            binding.btnAttach.isEnabled = !uploading
        }
        viewModel.error.observe(this) { event ->
            event.getContentIfNotHandled()?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }
    }

    override fun onStart() {
        super.onStart()
        ActiveChat.partnerId = viewModel.partnerId
    }

    override fun onStop() {
        super.onStop()
        ActiveChat.partnerId = null
    }

    companion object {
        fun newIntent(context: Context, partnerId: String, partnerName: String) =
            Intent(context, ChatActivity::class.java)
                .putExtra(ChatArgs.PARTNER_ID, partnerId)
                .putExtra(ChatArgs.PARTNER_NAME, partnerName)
    }
}
