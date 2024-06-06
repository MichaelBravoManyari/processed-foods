package com.mbm.alimentosprocesados.data

data class SimplifiedProcessedFoodPackagingReport(
    val name: String,
    val highSugar: Boolean,
    val highSaturatedFats: Boolean,
    val highTransFats: Boolean,
    val highSodium: Boolean,
    val percentage: Float
)
