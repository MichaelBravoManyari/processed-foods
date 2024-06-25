package com.mbm.alimentosprocesados.reports

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.R
import com.mbm.alimentosprocesados.databinding.FragmentMonthlyDetailedNutritionalFoodReportBinding
import com.mbm.alimentosprocesados.viewmodels.MonthlyDetailedNutritionalFoodReportUiState
import com.mbm.alimentosprocesados.viewmodels.MonthlyDetailedNutritionalFoodReportViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class MonthlyDetailedNutritionalFoodReportFragment : Fragment() {

    private var _binding: FragmentMonthlyDetailedNutritionalFoodReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MonthlyDetailedNutritionalFoodReportViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentMonthlyDetailedNutritionalFoodReportBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTextYear.setText(LocalDate.now().year.toString())
        val monthsArray = resources.getStringArray(R.array.months_array)
        val actualMonth = monthsArray[LocalDate.now().month.value - 1]
        binding.spinnerMonth.setText(actualMonth, false)
        binding.spinnerMonth.setOnClickListener {
            hideKeyboard()
        }
        observeUiState()
        setupSearchButton()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MonthlyDetailedNutritionalFoodReportUiState.Loading -> handleLoadingState()
                        is MonthlyDetailedNutritionalFoodReportUiState.Success -> handleSuccessState(
                            uiState
                        )

                        is MonthlyDetailedNutritionalFoodReportUiState.Failure -> handleFailureState()
                        is MonthlyDetailedNutritionalFoodReportUiState.NoData -> handleNoDataState()
                    }
                }
            }
        }
    }

    private fun handleLoadingState() {
        setReportVisibility(View.INVISIBLE)
        binding.apply {
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.GONE
            progressCircular.visibility = View.VISIBLE
        }
        Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Loading data")
    }

    private fun handleSuccessState(uiState: MonthlyDetailedNutritionalFoodReportUiState.Success) {
        binding.apply {
            progressCircular.visibility = View.GONE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.GONE
            setReportVisibility(View.VISIBLE)
            updateReportTextWithAnimation(uiState)
            updateHorizontalBarChart(uiState)
        }
        Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Success data $uiState")
    }

    private fun handleFailureState() {
        setReportVisibility(View.INVISIBLE)
        binding.apply {
            progressCircular.visibility = View.GONE
            noDataImage.visibility = View.GONE
            failureImage.visibility = View.VISIBLE
        }
        showAlertDialog("Error", "Hubo un error, intentalo más tarde.")
        Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Failure data")
    }

    private fun handleNoDataState() {
        setReportVisibility(View.INVISIBLE)
        binding.apply {
            progressCircular.visibility = View.GONE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.VISIBLE
            horizontalBarChart.clear()
        }
        showAlertDialog("No hay datos", "No se encontraron datos para esta consulta.")
        Log.d("MonthlyDetailedNutritionalFoodReportFragment", "No data")
    }

    private fun updateHorizontalBarChart(uiState: MonthlyDetailedNutritionalFoodReportUiState.Success) {
        val entries = listOf(
            BarEntry(0f, uiState.averageSaturatedFats),
            BarEntry(1f, uiState.averageTransFats),
            BarEntry(2f, uiState.averageSugar),
            BarEntry(3f, uiState.averageSodium)
        )

        val barDataSet = BarDataSet(entries, "Valores promedio en el mes").apply {
            colors = listOf(
                ColorTemplate.rgb("#FF6F61"),
                ColorTemplate.rgb("#6B5B95"),
                ColorTemplate.rgb("#88B04B"),
                ColorTemplate.rgb("#F7CAC9")
            )
            valueTextColor = Color.BLACK
            valueTextSize = 15f
        }

        binding.horizontalBarChart.apply {
            data = BarData(barDataSet)
            customizeHorizontalChart()
            invalidate()
        }
    }

    private fun BarChart.customizeHorizontalChart() {
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawLabels(true)
            textSize = 15f
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                private val labels = listOf("Grasas Sat.", "Grasas Trans", "Azúcar", "Sodio")
                override fun getFormattedValue(value: Float): String {
                    return labels.getOrNull(value.toInt()) ?: value.toString()
                }
            }
        }
        axisLeft.apply {
            textColor = Color.BLACK
            textSize = 15f
            setDrawGridLines(true)
        }
        axisRight.isEnabled = false
        legend.isEnabled = false
        setBackgroundColor(Color.WHITE)
        description.isEnabled = false
        animateY(500)
        animateX(500)
    }

    private fun setReportVisibility(visibility: Int) {
        val views = listOf(
            binding.titleReport,
            binding.horizontalBarChart,
            binding.txtNumPackaging,
            binding.colorAltoGrasasSaturadas,
            binding.txtAltoGrasasSaturadas,
            binding.colorAltoGrasasTrans,
            binding.txtContieneGrasasTrans,
            binding.colorAltoAzucar,
            binding.txtAltoAzucar,
            binding.colorAltoSodio,
            binding.txtAltoSodio
        )

        views.forEach { it.visibility = visibility }
    }

    private fun updateReportTextWithAnimation(uiState: MonthlyDetailedNutritionalFoodReportUiState.Success) {
        binding.apply {
            animateTextChange(
                txtNumPackaging,
                getString(R.string.num_packaging, uiState.numPackaging.toString())
            )
            animateTextChange(
                txtAltoGrasasSaturadas, getString(
                    R.string.percent_average_saturated_fats,
                    uiState.percentageOfAverageSaturatedFats.toString()
                )
            )
            animateTextChange(
                txtContieneGrasasTrans, getString(
                    R.string.percent_average_trans_fats,
                    uiState.percentageOfAverageTransFats.toString()
                )
            )
            animateTextChange(
                txtAltoAzucar, getString(
                    R.string.percent_average_sugar, uiState.percentageOfAverageSugar.toString()
                )
            )
            animateTextChange(
                txtAltoSodio, getString(
                    R.string.percent_average_sodium, uiState.percentageOfAverageSodium.toString()
                )
            )
        }
    }

    private fun animateTextChange(textView: TextView, newText: String) {
        val fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f)
        fadeOut.duration = 250
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                textView.text = newText
                val fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f)
                fadeIn.duration = 250
                fadeIn.start()
            }
        })
        fadeOut.start()
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val month = getMonthPosition(binding.spinnerMonth.text.toString())
            Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Selected month: $month")
            val yearText = binding.editTextYear.text.toString()
            if (yearText.isNotEmpty()) {
                val year = yearText.toIntOrNull()
                if (year != null && year in 2024..2100) {
                    Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Year valid")
                    if (month != -1) {
                        binding.editTextYear.error = null
                        hideKeyboard()
                        viewModel.updateReport(year, month)
                        Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Month valid")
                    } else {
                        showAlertDialog("Seleccione un mes", "No ha seleccionado un mes.")
                        Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Month invalid")
                    }
                } else {
                    binding.editTextYear.error = "Año no válido"
                    Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Year invalid")
                }
            } else {
                binding.editTextYear.error = "Ingrese un año"
                Log.d("MonthlyDetailedNutritionalFoodReportFragment", "Year empty")
            }
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getMonthPosition(month: String): Int {
        val monthsArray = resources.getStringArray(R.array.months_array)
        return monthsArray.indexOfFirst { it.equals(month, ignoreCase = true) } + 1
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_monthlyFoodOctagonsReportFragment_to_authFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}