package com.mbm.alimentosprocesados.data

data class ProcessedFoodPackagingWithOctagonsForReports(
    val sugar: Float,
    val saturatedFats: Float,
    val transFats: Float,
    val sodium: Float,
    val name: String,
    val highSugar: Boolean,
    val highSaturatedFats: Boolean,
    val highTransFats: Boolean,
    val highSodium: Boolean
)
