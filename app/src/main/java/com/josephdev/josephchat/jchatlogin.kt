package com.josephdev.josephchat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.josephdev.josephchat.ActivityUtils.openActivityAndClear
import com.josephdev.josephchat.ToastUtils.jtoast
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class jchatlogin : AppCompatActivity() {
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_jchatlogin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val channel = NotificationChannel(
            "chat_channel",
            "Mensajes del Chat",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        // Aquí pasas el contexto de la Activity actual
        googleAuthClient = GoogleAuthClient(this)

        // Obtener las vistas
        val validEmail = findViewById<TextView>(R.id.validEmail)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)

        // Acción de "Iniciar sesión" con email/contraseña
        signInButton.setOnClickListener {
            try {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                validEmail.visibility = TextView.GONE

                // Aquí agregas tu lógica de autenticación con Firebase para correo y contraseña
                signInWithEmailPassword(email, password)
            } catch (_: Exception) {
                validEmail.visibility = TextView.VISIBLE
            }
        }

        googleSignInButton.setOnClickListener {
            lifecycleScope.launch {
                val result = googleAuthClient.signIn()

                if (result) {
                    jtoast(applicationContext, "Inició sesión con Google")

                    val user = FirebaseAuth.getInstance().currentUser
                    val userRef = FirebaseFirestore.getInstance().collection("users").document(user!!.uid)
                    val snapshot = userRef.get().await()
                    val username = snapshot.getString("username")

                    if (username.isNullOrEmpty()) {
                        openActivityAndClear(this@jchatlogin, jchatusr::class.java)
                    } else {
                        openActivityAndClear(this@jchatlogin, jchat::class.java)
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    FirebaseFirestore.getInstance().collection("users")
                                        .document(userId)
                                        .update("fcmToken", token)
                                }
                            }
                        }
                    }

                } else {
                    jtoast(applicationContext, "Fallo al iniciar sesión con Google")
                }
            }
        }




        // Acción de "Crear cuenta"
        createAccountButton.setOnClickListener {
            try {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                validEmail.visibility = TextView.GONE

                registerUserWithEmailPassword(email, password)
            } catch (_: Exception) {
                validEmail.visibility = TextView.VISIBLE
            }
        }
    }

    private fun registerUserWithEmailPassword(email: String, password: String) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                lifecycleScope.launch {
                    if (task.isSuccessful) {
                        jtoast(applicationContext, "Registrado con correo electrónico")

                        val user = FirebaseAuth.getInstance().currentUser
                        val userRef = FirebaseFirestore.getInstance().collection("users").document(user!!.uid)
                        val snapshot = userRef.get().await()
                        val username = snapshot.getString("username")

                        if (username.isNullOrEmpty()) {
                            openActivityAndClear(this@jchatlogin, jchatusr::class.java)
                        } else {
                            openActivityAndClear(this@jchatlogin, jchat::class.java)
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        FirebaseFirestore.getInstance().collection("users")
                                            .document(userId)
                                            .update("fcmToken", token)
                                    }
                                }
                            }
                        }

                    } else {
                        println("Error al crear cuenta: ${task.exception?.message}")
                        if (task.exception?.message == "The email address is already in use by another account.") {
                            jtoast(applicationContext, "El correo electrónico ya está en uso")
                        } else {
                            jtoast(applicationContext, "Error al crear cuenta")
                        }
                    }
                }
            }
    }


    private fun signInWithEmailPassword(email: String, password: String) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    lifecycleScope.launch {
                        if (task.isSuccessful) {
                            jtoast(applicationContext, "Inició sesión con correo electrónico")

                            val user = FirebaseAuth.getInstance().currentUser
                            val userRef = FirebaseFirestore.getInstance().collection("users").document(user!!.uid)
                            val snapshot = userRef.get().await()
                            val username = snapshot.getString("username")

                            if (username.isNullOrEmpty()) {
                                openActivityAndClear(this@jchatlogin, jchatusr::class.java)
                            } else {
                                openActivityAndClear(this@jchatlogin, jchat::class.java)
                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null) {
                                            FirebaseFirestore.getInstance().collection("users")
                                                .document(userId)
                                                .update("fcmToken", token)
                                        }
                                    }
                                }
                            }

                        } else {
                            println("Error al crear cuenta: ${task.exception?.message}")
                            if (task.exception?.message == "The email address is already in use by another account.") {
                                jtoast(applicationContext, "El correo electrónico ya está en uso")
                            } else {
                                jtoast(applicationContext, "Error al crear cuenta")
                            }
                        }
                    }
                } else {
                    println("Error en el inicio de sesión: ${task.exception?.message}")
                    if (task.exception?.message == "The supplied auth credential is incorrect, malformed or has expired.") {
                        jtoast(applicationContext, "Contraseña o correo incorrectos")
                    } else {
                        jtoast(applicationContext, "Error en el inicio de sesión")
                    }
                }
            }
    }


}

