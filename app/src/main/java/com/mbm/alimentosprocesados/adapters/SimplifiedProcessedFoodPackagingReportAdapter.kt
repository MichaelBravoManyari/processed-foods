package com.mbm.alimentosprocesados.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mbm.alimentosprocesados.R
import com.mbm.alimentosprocesados.data.SimplifiedProcessedFoodPackagingReport
import com.mbm.alimentosprocesados.databinding.ItemPackagingFoodBrandBinding

val imageMap = mapOf(
    "CHOMP_NARANJA_38G" to R.drawable.chomp_naranja_38g,
    "GALLETA_RELLENITAS_FRESA_GN_6_GALLETAS" to R.drawable.galleta_rellenitas_fresa_gn_6_galletas,
    "GALLETA_RELLENITAS_CHOCOLATE_GN_6_GALLETAS" to R.drawable.galleta_rellenitas_chocolate_gn_6_galletas,
    "PEPSI_500ML" to R.drawable.pepsi_500ml,
    "GALLETA_OREO_4_GALLETAS" to R.drawable.galleta_oreo_4_galletas,
    "CHOCMAN_COSTA_28G" to R.drawable.chocman_costa_28g,
    "CHOMP_CHOCOLATE_38G" to R.drawable.chomp_chocolate_38g,
    "CEREAL_ANGEL_COPIX" to R.drawable.cereal_angel_copix,
    "GALLETA_RITZ_8_GALLETAS" to R.drawable.galleta_ritz_8_galletas,
    "GALLETA_RELLENITAS_COCO_GN_6_GALLETAS" to R.drawable.galleta_rellenitas_coco_gn_6_galletas
)

class SimplifiedProcessedFoodPackagingReportAdapter :
    ListAdapter<SimplifiedProcessedFoodPackagingReport, SimplifiedProcessedFoodPackagingReportAdapter.SimplifiedProcessedFoodPackagingReportViewHolder>(
        DiffCallback
    ) {

    class SimplifiedProcessedFoodPackagingReportViewHolder(private var binding: ItemPackagingFoodBrandBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: SimplifiedProcessedFoodPackagingReport) {
            binding.apply {
                // Formatear el nombre del paquete
                val formattedName = report.name.replace("_", " ").split(" ")
                    .joinToString(" ") {
                        it.lowercase().replaceFirstChar { char -> char.uppercase() }
                    }
                packagingName.text = formattedName

                txtIndividualNumPackaging.text = root.resources.getString(
                    R.string.alone_num_packaging,
                    report.numPackaging.toString()
                )

                // Asignar el porcentaje
                "${report.percentage.toInt()}%".also { percentPackaging.text = it }

                // Obtener la imagen correspondiente
                val imageResource = imageMap[report.name]
                imageResource?.let {
                    imagePackaging.setImageResource(imageResource)
                }

                // Configurar la visibilidad de los indicadores
                octagonHighSugar.visibility = if (report.highSugar) View.VISIBLE else View.GONE
                octagonHighSaturatedFats.visibility =
                    if (report.highSaturatedFats) View.VISIBLE else View.GONE
                octagonContentTransFats.visibility =
                    if (report.highTransFats) View.VISIBLE else View.GONE
                octagonHighSodium.visibility = if (report.highSodium) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimplifiedProcessedFoodPackagingReportViewHolder {
        val binding = ItemPackagingFoodBrandBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SimplifiedProcessedFoodPackagingReportViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SimplifiedProcessedFoodPackagingReportViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SimplifiedProcessedFoodPackagingReport>() {
                override fun areItemsTheSame(
                    oldItem: SimplifiedProcessedFoodPackagingReport,
                    newItem: SimplifiedProcessedFoodPackagingReport
                ): Boolean {
                    return oldItem.name == newItem.name
                }

                override fun areContentsTheSame(
                    oldItem: SimplifiedProcessedFoodPackagingReport,
                    newItem: SimplifiedProcessedFoodPackagingReport
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
