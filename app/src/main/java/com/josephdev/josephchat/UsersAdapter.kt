package com.josephdev.josephchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.josephdev.josephchat.databinding.ItemUserBinding

class UsersAdapter(private val onUserSelected: (String) -> Unit) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private var usersList = listOf<User>()
    fun submitList(users: List<User>) {
        usersList = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Inflar el layout para cada item con ViewBinding
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Asignar los datos al holder
        val user = usersList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = usersList.size

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Bind de los datos al item usando ViewBinding
        fun bind(user: User) {
            binding.usernameTextView.text = user.username
            binding.root.setOnClickListener {
                // Llamar al callback cuando un usuario es seleccionado
                onUserSelected(user.uid)
            }
        }
    }
}
