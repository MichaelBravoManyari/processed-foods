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
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.R
import com.mbm.alimentosprocesados.databinding.FragmentMonthlyFoodOctagonsReportBinding
import com.mbm.alimentosprocesados.viewmodels.MonthlyFoodOctagonsReportUiState
import com.mbm.alimentosprocesados.viewmodels.MonthlyFoodOctagonsReportViewModel
import kotlinx.coroutines.launch


class MonthlyFoodOctagonsReportFragment : Fragment() {

    private var _binding: FragmentMonthlyFoodOctagonsReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val viewModel: MonthlyFoodOctagonsReportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyFoodOctagonsReportBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeUiState()
        setupSearchButton()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MonthlyFoodOctagonsReportUiState.Loading -> handleLoadingState()
                        is MonthlyFoodOctagonsReportUiState.Success -> handleSuccessState(uiState)
                        is MonthlyFoodOctagonsReportUiState.Failure -> handleFailureState()
                        is MonthlyFoodOctagonsReportUiState.NoData -> handleNoDataState()
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
        Log.d("MonthlyFoodOctagonsReportFragment", "Loading data")
    }

    private fun handleSuccessState(uiState: MonthlyFoodOctagonsReportUiState.Success) {
        binding.apply {
            progressCircular.visibility = View.GONE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.GONE
            setReportVisibility(View.VISIBLE)
            updateReportTextWithAnimation(uiState)
            updateBarChart(uiState)
        }
        Log.d("MonthlyFoodOctagonsReportFragment", "Success data")
    }

    private fun handleFailureState() {
        setReportVisibility(View.INVISIBLE)
        binding.apply {
            progressCircular.visibility = View.GONE
            noDataImage.visibility = View.GONE
            failureImage.visibility = View.VISIBLE
        }
        showAlertDialog("Error", "Hubo un error, intentalo más tarde.")
        Log.d("MonthlyFoodOctagonsReportFragment", "Failure data")
    }

    private fun handleNoDataState() {
        setReportVisibility(View.INVISIBLE)
        binding.apply {
            progressCircular.visibility = View.GONE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.VISIBLE
            barChart.clear()
        }
        showAlertDialog("No hay datos", "No se encontraron datos para esta consulta.")
        Log.d("MonthlyFoodOctagonsReportFragment", "No data")
    }

    private fun updateReportTextWithAnimation(uiState: MonthlyFoodOctagonsReportUiState.Success) {
        binding.apply {
            animateTextChange(txtAltoGrasasSaturadas, getString(
                R.string.percent_high_saturated_fats, uiState.percentageOfPackagesHighInSaturatedFats.toString()
            ))
            animateTextChange(txtContieneGrasasTrans, getString(
                R.string.percent_content_trans_fats, uiState.percentageOfPackagesHighInTransFats.toString()
            ))
            animateTextChange(txtAltoAzucar, getString(
                R.string.percent_high_sugar, uiState.percentageOfPackagesHighInSugar.toString()
            ))
            animateTextChange(txtAltoSodio, getString(
                R.string.percent_high_sodium, uiState.percentageOfPackagesHighInSodium.toString()
            ))
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

    private fun updateBarChart(uiState: MonthlyFoodOctagonsReportUiState.Success) {
        val entries = listOf(
            BarEntry(1f, uiState.numberOfPackagesHighInSaturatedFats.toFloat()),
            BarEntry(2f, uiState.numberOfPackagesHighInTransFats.toFloat()),
            BarEntry(3f, uiState.numberOfPackagesHighInSugar.toFloat()),
            BarEntry(4f, uiState.numberOfPackagesHighInSodium.toFloat())
        )

        val barDataSet = BarDataSet(entries, "Empaques desechados en el mes").apply {
            colors = listOf(
                ColorTemplate.rgb("#FF6F61"),
                ColorTemplate.rgb("#6B5B95"),
                ColorTemplate.rgb("#88B04B"),
                ColorTemplate.rgb("#F7CAC9")
            )
            valueTextColor = Color.BLACK
            valueTextSize = 15f
        }

        binding.barChart.apply {
            data = BarData(barDataSet)
            customizeChart()
            invalidate()
        }
    }

    private fun BarChart.customizeChart() {
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawLabels(false)
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

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val month = getMonthPosition(binding.spinnerMonth.text.toString())
            Log.d("MonthlyFoodOctagonsReportFragment", "Selected month: $month")
            val yearText = binding.editTextYear.text.toString()
            if (yearText.isNotEmpty()) {
                val year = yearText.toIntOrNull()
                if (year != null && year in 2024..2100) {
                    Log.d("MonthlyFoodOctagonsReportFragment", "Year valid")
                    if (month != -1) {
                        binding.editTextYear.error = null
                        hideKeyboard()
                        viewModel.updateReport(year, month)
                        Log.d("MonthlyFoodOctagonsReportFragment", "Month valid")
                    } else {
                        showAlertDialog("Seleccione un mes", "No ha seleccionado un mes.")
                        Log.d("MonthlyFoodOctagonsReportFragment", "Month invalid")
                    }
                } else {
                    binding.editTextYear.error = "Año no válido"
                    Log.d("MonthlyFoodOctagonsReportFragment", "Year invalid")
                }
            } else {
                binding.editTextYear.error = "Ingrese un año"
                Log.d("MonthlyFoodOctagonsReportFragment", "Year empty")
            }
        }
    }

    private fun setReportVisibility(visibility: Int) {
        val views = listOf(
            binding.titleReport, binding.barChart, binding.colorAltoGrasasSaturadas,
            binding.txtAltoGrasasSaturadas, binding.colorAltoGrasasTrans, binding.txtContieneGrasasTrans,
            binding.colorAltoAzucar, binding.txtAltoAzucar, binding.colorAltoSodio, binding.txtAltoSodio
        )

        views.forEach { it.visibility = visibility }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getMonthPosition(month: String): Int {
        val monthsArray = resources.getStringArray(R.array.months_array)
        return monthsArray.indexOfFirst { it.equals(month, ignoreCase = true) } + 1
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
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