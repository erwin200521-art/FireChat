package com.example.firechat.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firechat.R
import com.example.firechat.data.model.Message
import com.example.firechat.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Locale

/** Historial de mensajes. Los propios se alinean a la derecha y los recibidos a la izquierda. */
class MessagesAdapter(
    private val currentUserId: String
) : ListAdapter<Message, MessagesAdapter.MessageViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val mine = message.senderId == currentUserId

            binding.root.gravity = if (mine) Gravity.END else Gravity.START
            binding.bubble.setBackgroundResource(
                if (mine) R.drawable.bg_bubble_sent else R.drawable.bg_bubble_received
            )

            binding.tvSender.text = message.senderName
            binding.tvTime.text = message.timestamp?.let { dateFormat.format(it) }.orEmpty()

            binding.tvText.apply {
                text = message.text
                visibility = if (message.text.isEmpty()) View.GONE else View.VISIBLE
            }

            val hasImage = !message.imageUrl.isNullOrEmpty()
            binding.ivImage.visibility = if (hasImage) View.VISIBLE else View.GONE
            if (hasImage) {
                Glide.with(binding.ivImage)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.ivImage)
            } else {
                Glide.with(binding.ivImage).clear(binding.ivImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MessageViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(old: Message, new: Message) = old.id == new.id
        override fun areContentsTheSame(old: Message, new: Message) = old == new
    }
}
