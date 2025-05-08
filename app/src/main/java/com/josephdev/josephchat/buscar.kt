package com.josephdev.josephchat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class buscar : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragment
        val view = inflater.inflate(R.layout.fragment_buscar, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)

        // Establecer el layout manager para el RecyclerView
        usersRecyclerView.layoutManager = LinearLayoutManager(context)

        // Crear el adaptador del RecyclerView
        usersAdapter = UsersAdapter { userId ->
            // Cuando se selecciona un usuario, creamos un chat
            println("Selected user ID: $userId")
            createNewChat(userId)
        }
        usersRecyclerView.adapter = usersAdapter

        // Escuchar el cambio en el campo de búsqueda
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchUsers(query)
                } else {
                    usersAdapter.submitList(emptyList())
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })


        return view
    }

    private fun searchUsers(query: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + "\uf8ff") // Búsqueda difusa
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { document ->
                    // Aquí verificamos qué datos estamos obteniendo de Firestore
                    println("Document Data: ${document.data}") // Imprime los datos del documento
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        println("User Data: ${user.username}, ${user.uid}") // Verifica el uid y el username
                    }
                    user
                }
                usersAdapter.submitList(users)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al buscar usuarios", Toast.LENGTH_SHORT).show()
            }
    }


    private fun createNewChat(participantId: String) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return
        val chatId = listOf(currentUserId, participantId).sorted().joinToString("_")

        // Verificar si los valores de currentUserId y participantId están correctos
        println("currentUserId: $currentUserId")
        println("participantId: $participantId")
        println("chatId: $chatId")

        val chatRef = db.collection("chats").document(chatId)

        chatRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    val newChat = hashMapOf(
                        "participants" to listOf(currentUserId, participantId),
                        "lastMessage" to "",
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    // Verificar el contenido que estamos a punto de escribir
                    println("Creating new chat with data: $newChat")

                    chatRef.set(newChat)
                        .addOnSuccessListener {
                            openChat(chatId, participantId)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al crear chat", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    openChat(chatId, participantId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al verificar chat", Toast.LENGTH_SHORT).show()
            }
    }


    private fun openChat(chatId: String, receiverId: String) {
        val intent = Intent(requireContext(), jchatindiv::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("receiverId", receiverId)
        startActivity(intent)
    }
}
