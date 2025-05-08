package com.josephdev.josephchat

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        println("FCM: Mensaje recibido: ${remoteMessage}")
        val chatId = remoteMessage.data["chatId"]
        val receiverId = remoteMessage.data["receiverId"]
        // Procesar mensaje como siempre
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Mensaje", it.body ?: "", chatId ?: "", receiverId ?: "")
        }
    }

    private fun showNotification(title: String, message: String, chatId: String, receiverId: String) {
        println("FCM: Mostrando notificación: $title - $message")

        val intent = Intent(this, jchatindiv::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("receiverId", receiverId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, "chat_channel")
            .setSmallIcon(R.mipmap.josephchat_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())

        println("FCM: Notificación enviada")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM: Nuevo token generado: $token")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    println("FCM: Token actualizado en Firestore")
                }
                .addOnFailureListener {
                    println("FCM: Error al actualizar token: ${it.message}")
                }
        } else {
            println("FCM: Usuario no logueado, no se sube el token")
        }
    }
}