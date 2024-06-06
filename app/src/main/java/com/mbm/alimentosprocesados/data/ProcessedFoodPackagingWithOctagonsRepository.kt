package com.mbm.alimentosprocesados.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProcessedFoodPackagingWithOctagonsRepository {

    private val db = Firebase.firestore

    fun getProcessedFoodPackagingWithOctagonsDetections(
        year: Int,
        month: Int
    ): Flow<List<ProcessedFoodPackagingWithOctagonsForReports?>> {
        val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

        val startDate = LocalDate.of(year, month, 1).atStartOfDay()
        val startTimestamp = dateFormat.format(startDate)

        val endDate = LocalDate.of(year, month, startDate.toLocalDate().lengthOfMonth()).atTime(23, 59, 59)
        val endTimestamp = dateFormat.format(endDate)

        return flow {
            val alimentosSnapshot = db.collection("alimentosProcesadosOctogonos").get().await()
            val alimentosMap = alimentosSnapshot.documents.associateBy { it.getString("nombre") ?: "" }

            db.collection("detections")
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)
                .snapshots()
                .collect { detectionsSnapshot ->
                    val results = mutableListOf<ProcessedFoodPackagingWithOctagonsForReports?>()
                    val detections = detectionsSnapshot.documents

                    for (detection in detections) {
                        val className = detection.getString("class_name") ?: continue
                        val alimento = alimentosMap[className]

                        if (alimento != null) {
                            val report = ProcessedFoodPackagingWithOctagonsForReports(
                                sugar = alimento.getDouble("azucar")?.toFloat() ?: 0f,
                                saturatedFats = alimento.getDouble("grasas_saturadas")?.toFloat() ?: 0f,
                                transFats = alimento.getDouble("grasas_trans")?.toFloat() ?: 0f,
                                sodium = alimento.getDouble("sodio")?.toFloat() ?: 0f,
                                name = alimento.getString("nombre") ?: "",
                                highSugar = alimento.getBoolean("alto_azucar") ?: false,
                                highSaturatedFats = alimento.getBoolean("alto_grasas_saturadas") ?: false,
                                highTransFats = alimento.getBoolean("alto_grasas_trans") ?: false,
                                highSodium = alimento.getBoolean("alto_sodio") ?: false
                            )
                            results.add(report)
                        }
                    }
                    emit(results)
                }
        }
    }
}