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
        binding.btnCancelar.setOnClickListener {
            view.findNavController().navigate(R.id.action_emailLoginFragment_to_authFragment)
        }
        binding.btnIniciarSesion.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextContrasena.text.toString()
            if (isValidEmail(email)) {
                if (password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful)
                            findNavController().navigate(R.id.action_emailLoginFragment_to_monthlyFoodOctagonsReportFragment)
                        else
                            showAlert("Error al identificar el usuario", requireContext())
                    }
                } else {
                    binding.editTextContrasena.error = "Ingrese contraseña"
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