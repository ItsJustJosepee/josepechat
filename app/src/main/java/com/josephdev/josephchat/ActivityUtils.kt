package com.josephdev.josephchat

import android.app.Activity
import android.content.Context
import android.content.Intent

object ActivityUtils {

    // Abre una nueva actividad y permite volver atrás (no finaliza la actual)
    fun openActivity(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        context.startActivity(intent)
    }

    // Abre una nueva actividad y elimina la actual del stack (no se puede volver atrás)
    fun openActivityAndClear(activity: Activity, targetActivity: Class<*>) {
        val intent = Intent(activity, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}
