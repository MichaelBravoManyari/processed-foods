package com.mbm.alimentosprocesados.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        binding.btnRegistrar.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextContrasena.text.toString()
            val passwordRe = binding.editTextValContrasena.text.toString()
            if (isValidEmail(email)) {
                if (isValidPassword(password)) {
                    if (password == passwordRe) {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                findNavController().navigate(R.id.action_emailSignUpFragment_to_monthlyFoodOctagonsReportFragment)
                            } else {
                                showAlert("Error al crear usuario", requireContext())
                            }
                        }
                    } else {
                        binding.editTextValContrasena.error = "Las contraseñas no coinciden"
                    }
                } else {
                    binding.editTextContrasena.error = "Contraseña de baja seguridad"
                }
            } else {
                binding.editTextEmail.error = "Correo incorrecto"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}