package com.mbm.alimentosprocesados.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.R
import com.mbm.alimentosprocesados.databinding.FragmentEmailLoginBinding

class EmailLoginFragment : Fragment() {

    private var _binding: FragmentEmailLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailLoginBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        binding.btnCancelar.setOnClickListener {
            navigateToAuthFragment(view)
        }
        binding.btnIniciarSesion.setOnClickListener {
            handleLogin()
        }
    }

    private fun navigateToAuthFragment(view: View) {
        view.findNavController().navigate(R.id.action_emailLoginFragment_to_authFragment)
    }

    private fun handleLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextContrasena.text.toString().trim()
        if (isValidEmail(email) && password.isNotEmpty()) {
            signInWithEmail(email, password)
        } else {
            showValidationErrors(email, password)
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navigateToMonthlyReportFragment()
            } else {
                showAlert("Error al identificar el usuario", requireContext())
            }
        }
    }

    private fun navigateToMonthlyReportFragment() {
        findNavController().navigate(R.id.action_emailLoginFragment_to_monthlyFoodOctagonsReportFragment)
    }

    private fun showValidationErrors(email: String, password: String) {
        if (email.isNotEmpty()) {
            if (!isValidEmail(email)) {
                binding.editTextLayoutEmail.error = "Correo incorrecto"
            }
        } else {
            binding.editTextLayoutEmail.error = "Ingrese su correo"
        }
        if (password.isEmpty()) {
            binding.editTextLayoutContrasena.error = "Ingrese contraseña"
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
