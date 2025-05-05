package com.josephdev.josephchat

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
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
                openActivityAndClear(this@jchatlogin, jchat::class.java)
            } catch (_: Exception) {
                validEmail.visibility = TextView.VISIBLE
            }
        }

        // Acción de "Iniciar sesión con Google"
        googleSignInButton.setOnClickListener {
            lifecycleScope.launch {
                val result = googleAuthClient.signIn()
                if (result) {
                    jtoast(applicationContext, "Inició sesión con Google")
                    openActivityAndClear(this@jchatlogin, jchat::class.java)
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
                jtoast(applicationContext, "Fallo al iniciar sesión con Google")
                openActivityAndClear(this, jchat::class.java)
            } catch (_: Exception) {
                validEmail.visibility = TextView.VISIBLE
            }
        }
    }

    private fun registerUserWithEmailPassword(email: String, password: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser

                    if (user != null) {
                        val userRef = firestore.collection("users").document(user.uid)

                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "photoUrl" to user.photoUrl?.toString(),
                            "name" to user.displayName,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        userRef.set(userData, SetOptions.merge())

                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result
                                userRef.update("fcmToken", token)
                            } else {
                                println("Error al obtener token FCM: ${tokenTask.exception}")
                            }
                        }

                        jtoast(applicationContext, "Cuenta creada correctamente")
                    }

                } else {
                    jtoast(applicationContext, "Error al crear cuenta")
                    println("Error al crear cuenta: ${task.exception?.message}")
                }
            }
    }


    private fun signInWithEmailPassword(email: String, password: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser

                    if (user != null) {
                        // Guardar info del usuario en Firestore
                        val userRef = firestore.collection("users").document(user.uid)

                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "photoUrl" to user.photoUrl?.toString(),
                            "name" to user.displayName,
                            "lastLogin" to FieldValue.serverTimestamp()
                        )

                        userRef.set(userData, SetOptions.merge())

                        // Obtener y guardar token FCM
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result
                                userRef.update("fcmToken", token)
                            } else {
                                println("Error al obtener token FCM: ${tokenTask.exception}")
                            }
                        }

                        jtoast(applicationContext, "Inicio de sesión exitoso")
                        // Aquí podrías redirigir a otra actividad si quieres
                        openActivityAndClear(this, jchat::class.java)
                    }

                } else {
                    jtoast(applicationContext, "Error en el inicio de sesión")
                    println("Error en el inicio de sesión: ${task.exception?.message}")
                }
            }
    }

}

