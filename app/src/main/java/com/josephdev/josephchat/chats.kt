package com.josephdev.josephchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.josephdev.josephchat.databinding.FragmentChatsBinding

class chats : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter { chatId, receiverId ->
            openChat(chatId, receiverId)
        }

        binding.chatListRecycler.layoutManager = LinearLayoutManager(context)
        binding.chatListRecycler.adapter = chatAdapter

        loadChats()
    }

    private fun loadChats() {
        db.collection("chats")
            .whereArrayContains("participants", currentUserId ?: return)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatsFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val chatList = mutableListOf<Chat>()
                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>() // para esperar las consultas

                snapshots?.forEach { doc ->
                    val participants = doc.get("participants") as? List<String> ?: return@forEach
                    val otherUserId = participants.firstOrNull { it != currentUserId } ?: return@forEach
                    val lastMessage = doc.getString("lastMessage") ?: ""

                    val task = db.collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val userName = userDoc.getString("username") ?: ""
                            val userImageUrl = userDoc.getString("photoUrl") ?: ""

                            chatList.add(
                                Chat(
                                    chatId = doc.id,
                                    userId = otherUserId,
                                    userName = userName,
                                    lastMessage = lastMessage,
                                    userImageUrl = userImageUrl
                                )
                            )
                        }
                    tasks.add(task)
                }

                // Espera a que todas las consultas a "users" terminen
                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    chatAdapter.submitList(chatList)
                }
            }
    }


    private fun openChat(chatId: String, receiverId: String) {
        val intent = Intent(requireContext(), jchatindiv::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("receiverId", receiverId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
