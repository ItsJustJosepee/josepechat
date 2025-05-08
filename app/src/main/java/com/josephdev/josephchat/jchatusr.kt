package com.josephdev.josephchat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class jchatusr : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var saveUsernameButton: Button
    private lateinit var validUsernameText: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jchatusr)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usernameInput = findViewById(R.id.usernameInput)
        saveUsernameButton = findViewById(R.id.saveUsernameButton)
        validUsernameText = findViewById(R.id.validUsername)

        saveUsernameButton.setOnClickListener {
            val enteredUsername = usernameInput.text.toString().trim()

            if (!isValidUsername(enteredUsername)) {
                validUsernameText.text = "Tu nombre de usuario contiene caracteres inválidos."
                validUsernameText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            checkUsernameAvailability(enteredUsername)
        }
    }

    private fun isValidUsername(username: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9_]+$")
        return username.matches(regex)
    }

    private fun checkUsernameAvailability(username: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    validUsernameText.text = getString(R.string.usrTaken)
                    validUsernameText.visibility = TextView.VISIBLE
                } else {
                    saveUserData(username)
                }
            }
            .addOnFailureListener {
                validUsernameText.text = getString(R.string.usrErrorVerif)
                validUsernameText.visibility = TextView.VISIBLE
            }
    }

    private fun saveUserData(username: String) {
        val user = currentUser
        if (user != null) {
            val uid = user.uid
            val name = user.displayName ?: ""
            val email = user.email ?: ""
            val photoUrl = user.photoUrl?.toString() ?: ""

            val userData = hashMapOf(
                "uid" to uid,
                "username" to username,
                "name" to name,
                "email" to email,
                "photoUrl" to photoUrl,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val userRef = firestore.collection("users").document(uid)
            userRef.set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    // Subir FCM Token
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            userRef.update("fcmToken", token)
                        }
                    }
                    validUsernameText.visibility = TextView.GONE
                    finish()
                }
                .addOnFailureListener {
                    validUsernameText.text = getString(R.string.usrErrorTake)
                    validUsernameText.visibility = TextView.VISIBLE
                }
        } else {
            validUsernameText.text = "Error: usuario no autenticado."
            validUsernameText.visibility = TextView.VISIBLE
        }
    }
}
