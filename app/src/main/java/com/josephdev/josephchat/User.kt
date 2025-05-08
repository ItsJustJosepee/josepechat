package com.josephdev.josephchat

import java.util.Date

data class User(
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Date = Date(),
    val fcmToken: String = "",
    val uid: String = ""
)