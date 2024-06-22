package com.mbm.alimentosprocesados.database

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prototype_prefs", Context.MODE_PRIVATE)

    var isPrototypeOn: Boolean
        get() = prefs.getBoolean("isPrototypeOn", false)
        set(value) = prefs.edit().putBoolean("isPrototypeOn", value).apply()
}
