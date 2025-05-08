package com.josephdev.josephchat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.josephdev.josephchat.databinding.ItemChatBinding
import kotlin.collections.toMutableList

class ChatAdapter(
    private val onChatClick: (chatId: String, receiverId: String) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chatList = mutableListOf<Chat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int = chatList.size

    fun submitList(newChatList: List<Chat>) {
        chatList = newChatList.toMutableList()
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chatName.text = chat.userName
            binding.lastMessage.text = chat.lastMessage
            Glide.with(binding.profileImage.context)
                .load(chat.userImageUrl)
                .into(binding.profileImage)

            // Al hacer clic en un chat, llamar a la función onChatClick
            itemView.setOnClickListener {
                onChatClick(chat.chatId, chat.userId) // Pasar el userId o el chatId, según lo que necesites
            }
        }
    }
}
