package com.example.firechat.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.data.model.User
import com.example.firechat.databinding.ItemUserBinding

class UsersAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, UsersAdapter.UserViewHolder>(DiffCallback) {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvInitial.text = user.name.firstOrNull()?.uppercase().orEmpty()
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            binding.root.setOnClickListener { onUserClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(old: User, new: User) = old.uid == new.uid
        override fun areContentsTheSame(old: User, new: User) = old == new
    }
}
