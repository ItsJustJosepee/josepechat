package com.josephdev.josephchat

data class Chat(
    val chatId: String,
    val userId: String = "",
    val userName: String = "",
    val lastMessage: String = "",
    val userImageUrl: String = ""
)