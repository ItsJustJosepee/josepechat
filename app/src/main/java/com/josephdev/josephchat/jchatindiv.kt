package com.josephdev.josephchat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.josephdev.josephchat.databinding.ActivityJchatindivBinding
import org.json.JSONObject

class jchatindiv : AppCompatActivity() {

    private lateinit var binding: ActivityJchatindivBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var chatId: String
    private lateinit var receiverId: String

    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityJchatindivBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        chatId = intent.getStringExtra("chatId") ?: ""
        receiverId = intent.getStringExtra("receiverId") ?: ""

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessagesAdapter()
        binding.messagesRecyclerView.adapter = messagesAdapter

        loadMessages()

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }
    }

    private fun loadMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val messages = result.documents.mapNotNull { it.toObject(Message::class.java) }
                messagesAdapter.submitList(messages)
                binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessage(message: String) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        val newMessage = hashMapOf(
            "senderId" to currentUserId,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(newMessage)
            .addOnSuccessListener {
                binding.messageInput.text?.clear()
                loadMessages()

                // Obtener username del remitente
                db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("username") ?: "Nuevo mensaje"

                        // Obtener token del receptor
                        db.collection("users").document(receiverId).get()
                            .addOnSuccessListener { receiverDoc ->
                                val token = receiverDoc.getString("fcmToken")
                                if (!token.isNullOrEmpty()) {
                                    sendNotificationToReceiver(token, message, username)
                                }
                            }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotificationToReceiver(fcmToken: String, message: String, username: String) {
        val url = "https://illegal-felisha-itsjustjosepee-b06a9408.koyeb.app/send-notification"

        val json = JSONObject().apply {
            put("token", fcmToken)
            put("title", username)  // Se mostrará como título de la notificación
            put("body", message)    // Se mostrará como texto
            put("chatId", chatId)   // Necesario para abrir el chat específico
            put("receiverId", receiverId)
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, json,
            { response -> println("Notificación enviada: $response") },
            { error -> println("Error al enviar notificación: ${error.message}") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/json")
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}