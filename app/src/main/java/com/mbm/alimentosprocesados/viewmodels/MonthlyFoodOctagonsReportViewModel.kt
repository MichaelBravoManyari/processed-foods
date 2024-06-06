package com.mbm.alimentosprocesados.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbm.alimentosprocesados.data.ProcessedFoodPackagingWithOctagonsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MonthlyFoodOctagonsReportViewModel : ViewModel() {
    private val processedFoodPackagingWithOctagonsRepository =
        ProcessedFoodPackagingWithOctagonsRepository()

    private var _uiState =
        MutableStateFlow<MonthlyFoodOctagonsReportUiState>(MonthlyFoodOctagonsReportUiState.Loading)
    val uiState: StateFlow<MonthlyFoodOctagonsReportUiState> = _uiState.asStateFlow()

    init {
        updateReport(LocalDate.now().year, LocalDate.now().month.value)
    }

    fun updateReport(year: Int, month: Int) {
        viewModelScope.launch {
            processedFoodPackagingWithOctagonsRepository.getProcessedFoodPackagingWithOctagonsDetections(
                year,
                month
            )
                .map { reports ->
                    val totalReports = reports.size
                    if (totalReports == 0) {
                        MonthlyFoodOctagonsReportUiState.NoData
                    } else {
                        val highSaturatedFatsCount = reports.count { it!!.highSaturatedFats }
                        val highTransFatsCount = reports.count { it!!.highTransFats }
                        val highSugarCount = reports.count { it!!.highSugar }
                        val highSodiumCount = reports.count { it!!.highSodium }

                        val highSaturatedFatsPercentage =
                            (highSaturatedFatsCount * 100) / totalReports
                        val highTransFatsPercentage = (highTransFatsCount * 100) / totalReports
                        val highSugarPercentage = (highSugarCount * 100) / totalReports
                        val highSodiumPercentage = (highSodiumCount * 100) / totalReports

                        MonthlyFoodOctagonsReportUiState.Success(
                            numberOfPackagesHighInSaturatedFats = highSaturatedFatsCount,
                            numberOfPackagesHighInTransFats = highTransFatsCount,
                            numberOfPackagesHighInSugar = highSugarCount,
                            numberOfPackagesHighInSodium = highSodiumCount,
                            percentageOfPackagesHighInSaturatedFats = highSaturatedFatsPercentage,
                            percentageOfPackagesHighInTransFats = highTransFatsPercentage,
                            percentageOfPackagesHighInSugar = highSugarPercentage,
                            percentageOfPackagesHighInSodium = highSodiumPercentage,
                            numPackaging = totalReports
                        )
                    }
                }
                .catch { _ ->
                    emit(MonthlyFoodOctagonsReportUiState.Failure)
                }
                .collect { newState ->
                    _uiState.update { newState }
                }
        }
    }
}

sealed interface MonthlyFoodOctagonsReportUiState {
    data object Loading : MonthlyFoodOctagonsReportUiState

    data class Success(
        val numberOfPackagesHighInSaturatedFats: Int,
        val numberOfPackagesHighInTransFats: Int,
        val numberOfPackagesHighInSugar: Int,
        val numberOfPackagesHighInSodium: Int,
        val percentageOfPackagesHighInSaturatedFats: Int,
        val percentageOfPackagesHighInTransFats: Int,
        val percentageOfPackagesHighInSugar: Int,
        val percentageOfPackagesHighInSodium: Int,
        val numPackaging: Int
    ) : MonthlyFoodOctagonsReportUiState

    data object Failure : MonthlyFoodOctagonsReportUiState

    data object NoData : MonthlyFoodOctagonsReportUiState
}