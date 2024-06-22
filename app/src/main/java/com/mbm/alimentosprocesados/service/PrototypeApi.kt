package com.mbm.alimentosprocesados.service

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface PrototypeApi {

    @POST("start_prototype")
    suspend fun startPrototype(): Response<StatusResponse>

    @POST("stop_prototype")
    suspend fun stopPrototype(): Response<StatusResponse>

    @POST("restart_prototype")
    suspend fun restartPrototype(): Response<StatusResponse>

    @GET("prototype_status")
    suspend fun getPrototypeStatus(): Response<StatusResponse>
}

data class StatusResponse(val status: String)