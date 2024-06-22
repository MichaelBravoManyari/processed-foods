package com.mbm.alimentosprocesados.reports

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.R
import com.mbm.alimentosprocesados.adapters.SimplifiedProcessedFoodPackagingReportAdapter
import com.mbm.alimentosprocesados.databinding.FragmentMonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandBinding
import com.mbm.alimentosprocesados.viewmodels.MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState
import com.mbm.alimentosprocesados.viewmodels.MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment : Fragment() {
    private var _binding: FragmentMonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandBinding? =
        null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val viewModel: MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentMonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandBinding.inflate(
                inflater,
                container,
                false
            )
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val simplifiedProcessedFoodPackagingReportAdapter =
            SimplifiedProcessedFoodPackagingReportAdapter()
        binding.recyclerViewBrand.adapter = simplifiedProcessedFoodPackagingReportAdapter
        binding.editTextYear.setText(LocalDate.now().year.toString())
        val monthsArray = resources.getStringArray(R.array.months_array)
        val actualMonth = monthsArray[LocalDate.now().month.value - 1]
        binding.spinnerMonth.setText(actualMonth, false)
        observeUiState(simplifiedProcessedFoodPackagingReportAdapter)
        setupSearchButton()
    }

    private fun observeUiState(adapter: SimplifiedProcessedFoodPackagingReportAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Loading -> handleLoadingState()
                        is MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Success -> handleSuccessState(
                            uiState, adapter
                        )

                        is MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Failure -> handleFailureState()
                        is MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.NoData -> handleNoDataState()
                    }
                }
            }
        }
    }

    private fun handleLoadingState() {
        binding.apply {
            recyclerViewBrand.visibility = View.INVISIBLE
            titleReport.visibility = View.INVISIBLE
            txtNumPackaging.visibility = View.INVISIBLE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.GONE
            progressCircular.visibility = View.VISIBLE
        }
        Log.d(
            "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
            "Loading data"
        )
    }

    private fun handleSuccessState(
        uiState: MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandUiState.Success,
        adapter: SimplifiedProcessedFoodPackagingReportAdapter
    ) {
        binding.apply {
            progressCircular.visibility = View.GONE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.GONE
            recyclerViewBrand.visibility = View.VISIBLE
            titleReport.visibility = View.VISIBLE
            txtNumPackaging.visibility = View.VISIBLE
            txtNumPackaging.text =
                getString(R.string.num_packaging, uiState.numPackaging.toString())
        }
        adapter.submitList(uiState.reports)
        Log.d(
            "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
            "Success data"
        )
    }

    private fun handleFailureState() {
        binding.apply {
            recyclerViewBrand.visibility = View.INVISIBLE
            titleReport.visibility = View.INVISIBLE
            txtNumPackaging.visibility = View.INVISIBLE
            progressCircular.visibility = View.GONE
            noDataImage.visibility = View.GONE
            failureImage.visibility = View.VISIBLE
        }
        showAlertDialog("Error", "Hubo un error, intentalo más tarde.")
        Log.d(
            "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
            "Failure data"
        )
    }

    private fun handleNoDataState() {
        binding.apply {
            titleReport.visibility = View.INVISIBLE
            recyclerViewBrand.visibility = View.INVISIBLE
            txtNumPackaging.visibility = View.INVISIBLE
            progressCircular.visibility = View.GONE
            failureImage.visibility = View.GONE
            noDataImage.visibility = View.VISIBLE
        }
        showAlertDialog("No hay datos", "No se encontraron datos para esta consulta.")
        Log.d(
            "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
            "No data"
        )
    }

    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val month = getMonthPosition(binding.spinnerMonth.text.toString())
            Log.d(
                "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
                "Selected month: $month"
            )
            val yearText = binding.editTextYear.text.toString()
            if (yearText.isNotEmpty()) {
                val year = yearText.toIntOrNull()
                if (year != null && year in 2024..2100) {
                    Log.d(
                        "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
                        "Year valid"
                    )
                    if (month != -1) {
                        binding.editTextYear.error = null
                        hideKeyboard()
                        viewModel.updateReport(year, month)
                        Log.d(
                            "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
                            "Month valid"
                        )
                    } else {
                        showAlertDialog("Seleccione un mes", "No ha seleccionado un mes.")
                        Log.d(
                            "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
                            "Month invalid"
                        )
                    }
                } else {
                    binding.editTextYear.error = "Año no válido"
                    Log.d(
                        "MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment",
                        "Year invalid"
                    )
                }
            } else {
                binding.editTextYear.error = "Ingrese un año"
                Log.d("MonthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment", "Year empty")
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