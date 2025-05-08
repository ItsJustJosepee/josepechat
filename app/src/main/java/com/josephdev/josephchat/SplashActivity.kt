package com.josephdev.josephchat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import com.josephdev.josephchat.ActivityUtils.openActivityAndClear
class SplashActivity : ComponentActivity() {

    private lateinit var googleAuthClient: GoogleAuthClient
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        googleAuthClient = GoogleAuthClient(this)

        GlobalScope.launch(Dispatchers.Main) {
            if (!googleAuthClient.isSignedIn()) {
                // No está logeado, lo mandamos a jchatlogin
                startActivity(Intent(this@SplashActivity, jchatlogin::class.java))
            } else {
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid

                    // Verificamos si el UID ya está en Firestore
                    val userDoc = withContext(Dispatchers.IO) {
                        firestore.collection("users").document(uid).get().await()
                    }

                    if (userDoc.exists()) {
                        // Ya tiene datos en Firestore, lo mandamos a jchat
                        openActivityAndClear(this@SplashActivity, jchat::class.java)
                    } else {
                        // No tiene username ni doc creado, lo mandamos a jchatusr
                        openActivityAndClear(this@SplashActivity, jchatusr::class.java)
                    }
                } else {
                    // Algo raro pasó, no hay currentUser
                    openActivityAndClear(this@SplashActivity, jchatlogin::class.java)
                }
            }

            finish()
        }
    }
}
