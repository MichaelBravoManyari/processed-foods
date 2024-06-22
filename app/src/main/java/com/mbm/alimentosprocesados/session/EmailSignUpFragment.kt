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
import com.mbm.alimentosprocesados.databinding.FragmentEmailSignUpBinding

class EmailSignUpFragment : Fragment() {

    private var _binding: FragmentEmailSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailSignUpBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        binding.btnCancelar.setOnClickListener {
            view.findNavController().navigate(R.id.action_emailSignUpFragment_to_authFragment)
        }
        binding.btnRegistrar.setOnClickListener {
            handleSignUp()
        }
    }

    private fun handleSignUp() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextContrasena.text.toString().trim()
        val passwordRe = binding.editTextValContrasena.text.toString().trim()
        if (isInputValid(email, password, passwordRe)) {
            createUser(email, password)
        }
    }

    private fun isInputValid(email: String, password: String, passwordRe: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.editTextLayoutEmail.error = "Ingrese su correo"
                false
            }
            !isValidEmail(email) -> {
                binding.editTextLayoutEmail.error = "Correo incorrecto"
                false
            }
            password.isEmpty() -> {
                binding.editTextLayoutContrasena.error = "Ingrese una contraseña"
                false
            }
            !isValidPassword(password) -> {
                binding.editTextLayoutContrasena.error = "Contraseña de baja seguridad"
                false
            }
            password != passwordRe -> {
                binding.editTextLayoutValContrasena.error = "Las contraseñas no coinciden"
                false
            }
            else -> true
        }
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                findNavController().navigate(R.id.action_emailSignUpFragment_to_monthlyFoodOctagonsReportFragment)
            } else {
                showAlert("Error al crear usuario, correo ya registrado", requireContext())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
