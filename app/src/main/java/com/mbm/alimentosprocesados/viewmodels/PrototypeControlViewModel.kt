package com.mbm.alimentosprocesados.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbm.alimentosprocesados.service.RetrofitInstance
import com.mbm.alimentosprocesados.service.StatusResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class PrototypeControlViewModel : ViewModel() {
    private val _currentStatus = MutableLiveData<PrototypeControlState>()
    val currentStatus: LiveData<PrototypeControlState> get() = _currentStatus

    init {
        getCurrentPrototypeStatus()
    }

    private fun getCurrentPrototypeStatus() {
        viewModelScope.launch {
            _currentStatus.postValue(
                try {
                    val response = RetrofitInstance.api.getPrototypeStatus()
                    if (response.isSuccessful) {
                        PrototypeControlState(
                            serverStatus = parseServerStatus(response.body()?.status),
                            message = "",
                            isShowMessage = false
                        )
                    } else {
                        createErrorState(
                            ServerStatus.ERROR_CONNECTION,
                            "Conecte el prototipo a su fuente de energía o póngase al alcance de la red WIFI de la I.E."
                        )
                    }
                } catch (e: Exception) {
                    Log.e("PrototypeControlViewModel", e.message.toString())
                    createErrorState(
                        ServerStatus.ERROR_CONNECTION,
                        "Conecte el prototipo a su fuente de energía o póngase al alcance de la red WIFI de la I.E."
                    )
                }
            )
        }
    }

    fun turnOnPrototype() {
        updatePrototypeStatus(
            loadingMessage = "Encendiendo el prototipo...",
            errorStatus = ServerStatus.ERROR_WHEN_TURNING_ON_PROTOTYPE,
            errorMessage = "Hubo un error al iniciar el prototipo, comuníquese con el administrador o reinicie el prototipo",
            apiCall = { RetrofitInstance.api.startPrototype() }
        )
    }

    fun turnOffPrototype() {
        updatePrototypeStatus(
            loadingMessage = "Apagando el prototipo...",
            errorStatus = ServerStatus.ERROR_TURNING_OFF_PROTOTYPE,
            errorMessage = "Hubo un error al apagar el prototipo, comuníquese con el administrador",
            apiCall = { RetrofitInstance.api.stopPrototype() }
        )
    }

    fun restartPrototype() {
        updatePrototypeStatus(
            loadingMessage = "Reiniciando el prototipo...",
            errorStatus = ServerStatus.ERROR_WHEN_TURNING_ON_PROTOTYPE,
            errorMessage = "Hubo un error al reiniciar el prototipo, comuníquese con el administrador",
            apiCall = { RetrofitInstance.api.restartPrototype() }
        )
    }

    private fun updatePrototypeStatus(
        loadingMessage: String,
        errorStatus: ServerStatus,
        errorMessage: String,
        apiCall: suspend () -> Response<StatusResponse>
    ) {
        viewModelScope.launch {
            _currentStatus.postValue(
                PrototypeControlState(
                    ServerStatus.LOADING,
                    loadingMessage,
                    false
                )
            )
            _currentStatus.postValue(
                try {
                    val response = apiCall()
                    if (response.isSuccessful) {
                        PrototypeControlState(
                            serverStatus = parseServerStatus(response.body()?.status),
                            message = "",
                            isShowMessage = false
                        )
                    } else {
                        createErrorState(errorStatus, errorMessage)
                    }
                } catch (e: Exception) {
                    createErrorState(
                        ServerStatus.ERROR_CONNECTION,
                        "Conecte el prototipo a su fuente de energía o póngase al alcance de la red WIFI de la I.E."
                    )
                }
            )
        }
    }

    private fun createErrorState(serverStatus: ServerStatus, message: String) =
        PrototypeControlState(serverStatus, message, false)

    fun setIsShowMessage() {
        _currentStatus.value?.let {
            _currentStatus.postValue(it.copy(isShowMessage = true))
        }
    }

    private fun parseServerStatus(serverStatus: String?): ServerStatus {
        return when (serverStatus) {
            "prototype_started" -> ServerStatus.PROTOTYPE_STARTED
            "error_when_turning_on_prototype" -> ServerStatus.ERROR_WHEN_TURNING_ON_PROTOTYPE
            "prototype_is_already_running" -> ServerStatus.PROTOTYPE_ALREADY_RUNNING
            "prototype_off" -> ServerStatus.PROTOTYPE_OFF
            "error_turning_off_prototype" -> ServerStatus.ERROR_TURNING_OFF_PROTOTYPE
            "prototype_is_not_running" -> ServerStatus.PROTOTYPE_NOT_RUNNING
            "prototype_restarted" -> ServerStatus.PROTOTYPE_RESTARTED
            else -> ServerStatus.ERROR_CONNECTION
        }
    }
}

data class PrototypeControlState(
    val serverStatus: ServerStatus,
    val message: String,
    val isShowMessage: Boolean
)

enum class ServerStatus {
    PROTOTYPE_STARTED,
    ERROR_WHEN_TURNING_ON_PROTOTYPE,
    PROTOTYPE_ALREADY_RUNNING,
    PROTOTYPE_OFF,
    ERROR_TURNING_OFF_PROTOTYPE,
    PROTOTYPE_NOT_RUNNING,
    PROTOTYPE_RESTARTED,
    ERROR_CONNECTION,
    LOADING,
}
