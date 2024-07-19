package com.mbm.alimentosprocesados.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.0.10:5000/"

    val api: PrototypeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(35, TimeUnit.SECONDS) // Tiempo de conexión máximo
                    .readTimeout(35, TimeUnit.SECONDS)    // Tiempo de espera máximo para leer datos
                    .writeTimeout(35, TimeUnit.SECONDS)   // Tiempo de espera máximo para escribir datos
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrototypeApi::class.java)
    }
}
