package com.mbm.alimentosprocesados.session

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import java.util.regex.Pattern

fun isValidEmail(email: String): Boolean {
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}

fun isValidPassword(password: String): Boolean {
    val regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(password)
    return matcher.matches()
}

fun showAlert(message: String, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.apply {
        setTitle("Error")
        setMessage(message)
        setNegativeButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}