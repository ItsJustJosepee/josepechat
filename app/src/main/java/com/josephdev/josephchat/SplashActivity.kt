package com.josephdev.josephchat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    private lateinit var googleAuthClient: GoogleAuthClient
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Mostrar el splash mientras carga
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Inicializa el cliente de autenticación de Google
        googleAuthClient = GoogleAuthClient(this)

        // Llama a una función asíncrona para verificar el inicio de sesión
        GlobalScope.launch {
            if (googleAuthClient.isSingedIn()) {
                // El usuario ya está autenticado, redirigir a la pantalla principal
                startActivity(Intent(this@SplashActivity, jchat::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, jchatlogin::class.java))
            }

            // Finalizar SplashActivity después de verificar el estado
            finish()
        }
    }
}
