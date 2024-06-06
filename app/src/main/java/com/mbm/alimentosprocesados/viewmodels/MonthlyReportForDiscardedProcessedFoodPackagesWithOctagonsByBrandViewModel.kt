package com.mbm.alimentosprocesados.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbm.alimentosprocesados.data.ProcessedFoodPackagingWithOctagonsRepository
import com.mbm.alimentosprocesados.data.SimplifiedProcessedFoodPackagingReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

class MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandViewModel : ViewModel() {
    private val processedFoodPackagingWithOctagonsRepository =
        ProcessedFoodPackagingWithOctagonsRepository()

    private var _uiState =
        MutableStateFlow<MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState>(MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Loading)
    val uiState: StateFlow<MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState> = _uiState

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
                        MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.NoData
                    } else {
                        val reportMap = reports.groupBy { it!!.name }.mapValues { entry ->
                            val name = entry.key
                            val highSugar = entry.value.any { it!!.highSugar }
                            val highSaturatedFats = entry.value.any { it!!.highSaturatedFats }
                            val highTransFats = entry.value.any { it!!.highTransFats }
                            val highSodium = entry.value.any { it!!.highSodium }
                            val percentage = (entry.value.size.toFloat() / totalReports) * 100
                            SimplifiedProcessedFoodPackagingReport(
                                name,
                                highSugar,
                                highSaturatedFats,
                                highTransFats,
                                highSodium,
                                percentage
                            )
                        }.values.toList()
                        MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Success(reportMap)
                    }
                }
                .catch { _ ->
                    emit(MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Failure)
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
}

sealed interface MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState {
    data object Loading : MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState

    data class Success(
        val reports: List<SimplifiedProcessedFoodPackagingReport>
    ) : MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState

    data object Failure : MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState

    data object NoData : MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState
}
