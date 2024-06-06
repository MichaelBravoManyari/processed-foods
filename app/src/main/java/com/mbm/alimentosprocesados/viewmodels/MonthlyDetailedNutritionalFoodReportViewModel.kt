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

class MonthlyDetailedNutritionalFoodReportViewModel : ViewModel() {
    private val processedFoodPackagingWithOctagonsRepository =
        ProcessedFoodPackagingWithOctagonsRepository()

    private var _uiState =
        MutableStateFlow<MonthlyDetailedNutritionalFoodReportUiState>(
            MonthlyDetailedNutritionalFoodReportUiState.Loading
        )
    val uiState: StateFlow<MonthlyDetailedNutritionalFoodReportUiState> = _uiState.asStateFlow()

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
                        MonthlyDetailedNutritionalFoodReportUiState.NoData
                    } else {
                        val totalSaturatedFats =
                            reports.sumOf { it!!.saturatedFats.toDouble() }.toFloat()
                        val totalTransFats = reports.sumOf { it!!.transFats.toDouble() }.toFloat()
                        val totalSugar = reports.sumOf { it!!.sugar.toDouble() }.toFloat()
                        val totalSodium = reports.sumOf { it!!.sodium.toDouble() }.toFloat()

                        val averageSaturatedFats = totalSaturatedFats / totalReports
                        val averageTransFats = totalTransFats / totalReports
                        val averageSugar = totalSugar / totalReports
                        val averageSodium = totalSodium / totalReports

                        val totalAverage = averageSaturatedFats + averageTransFats + averageSugar + averageSodium

                        MonthlyDetailedNutritionalFoodReportUiState.Success(
                            averageSaturatedFats = "%.2f".format(averageSaturatedFats).toFloat(),
                            averageTransFats = "%.2f".format(averageTransFats).toFloat(),
                            averageSugar = "%.2f".format(averageSugar).toFloat(),
                            averageSodium = "%.2f".format(averageSodium).toFloat(),
                            percentageOfAverageSaturatedFats = "%.2f".format(((averageSaturatedFats  / totalAverage) * 100f))
                                .toFloat(),
                            percentageOfAverageTransFats = "%.2f".format(((averageTransFats  / totalAverage) * 100f))
                                .toFloat(),
                            percentageOfAverageSugar = "%.2f".format(((averageSugar / totalAverage) * 100f))
                                .toFloat(),
                            percentageOfAverageSodium = "%.2f".format(((averageSodium / totalAverage) * 100f))
                                .toFloat(),
                            totalReports
                        )
                    }
                }
                .catch { _ ->
                    emit(MonthlyDetailedNutritionalFoodReportUiState.Failure)
                }
                .collect { newState ->
                    _uiState.update { newState }
                }
        }
    }
}

sealed interface MonthlyDetailedNutritionalFoodReportUiState {
    data object Loading : MonthlyDetailedNutritionalFoodReportUiState

    data class Success(
        val averageSaturatedFats: Float,
        val averageTransFats: Float,
        val averageSugar: Float,
        val averageSodium: Float,
        val percentageOfAverageSaturatedFats: Float,
        val percentageOfAverageTransFats: Float,
        val percentageOfAverageSugar: Float,
        val percentageOfAverageSodium: Float,
        val numPackaging: Int
    ) : MonthlyDetailedNutritionalFoodReportUiState

    data object Failure : MonthlyDetailedNutritionalFoodReportUiState

    data object NoData : MonthlyDetailedNutritionalFoodReportUiState
}
