package com.josephdev.josephchat

import android.view.LayoutInflater import android.view.View import android.view.ViewGroup import android.widget.ImageView import android.widget.TextView import androidx.core.view.isVisible import androidx.recyclerview.widget.RecyclerView import com.bumptech.glide.Glide import com.google.firebase.auth.FirebaseAuth import com.google.firebase.firestore.FirebaseFirestore import java.text.SimpleDateFormat import java.util.*

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messages = listOf<Message>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val userCache = mutableMapOf<String, Pair<String, String>>() // UID -> Pair<Username, ImageUrl>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val previousSenderId = if (position > 0) messages[position - 1].senderId else null
        val isFirstFromSender = message.senderId != previousSenderId
        holder.bind(message, isFirstFromSender)
    }

    override fun getItemCount(): Int = messages.size

    fun submitList(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val bubbleLeft: View = view.findViewById(R.id.bubbleLeft)
        private val bubbleRight: View = view.findViewById(R.id.bubbleRight)

        private val messageTextLeft: TextView = bubbleLeft.findViewById(R.id.messageTextLeft)
        private val timeTextLeft: TextView = bubbleLeft.findViewById(R.id.timeTextLeft)
        private val usernameTextLeft: TextView = bubbleLeft.findViewById(R.id.usernameTextLeft)
        private val profileImageLeft: ImageView = bubbleLeft.findViewById(R.id.profileImageLeft)

        private val messageTextRight: TextView = bubbleRight.findViewById(R.id.messageTextRight)
        private val timeTextRight: TextView = bubbleRight.findViewById(R.id.timeTextRight)

        fun bind(message: Message, isFirstFromSender: Boolean) {
            val isCurrentUser = message.senderId == currentUserId

            if (isCurrentUser) {
                bubbleRight.isVisible = true
                bubbleLeft.isVisible = false
                usernameTextLeft.isVisible = false
                profileImageLeft.isVisible = false

                messageTextRight.text = message.message
                timeTextRight.text = formatTime(message.timestamp)
            } else {
                bubbleRight.isVisible = false
                bubbleLeft.isVisible = true
                usernameTextLeft.isVisible = true
                profileImageLeft.isVisible = true

                messageTextLeft.text = message.message
                timeTextLeft.text = formatTime(message.timestamp)

                usernameTextLeft.isVisible = isFirstFromSender
                profileImageLeft.isVisible = isFirstFromSender

                if (isFirstFromSender) {
                    if (userCache.containsKey(message.senderId)) {
                        val (username, imageUrl) = userCache[message.senderId]!!
                        usernameTextLeft.text = username
                        Glide.with(profileImageLeft.context).load(imageUrl).into(profileImageLeft)
                    } else {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(message.senderId).get()
                            .addOnSuccessListener { doc ->
                                val username = doc.getString("username") ?: "Usuario"
                                val imageUrl = doc.getString("photoUrl") ?: R.drawable.ic_notification
                                userCache[message.senderId] = Pair(username, imageUrl.toString())
                                usernameTextLeft.text = username
                                Glide.with(profileImageLeft.context).load(imageUrl).into(profileImageLeft)
                            }
                            .addOnFailureListener {
                                usernameTextLeft.text = "Usuario"
                                profileImageLeft.setImageResource(R.drawable.ic_notification)
                            }
                    }
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

}

