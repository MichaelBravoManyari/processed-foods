package com.mbm.alimentosprocesados.session

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.R
import com.mbm.alimentosprocesados.databinding.FragmentAuthBinding
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finishAffinity()
                }
            })

        binding.btnIniciarEmail.setOnClickListener {
            view.findNavController().navigate(R.id.action_authFragment_to_emailLoginFragment)
        }
        binding.btnIniciarGoogle.setOnClickListener {
            if (view.isEnabled) {
                view.isEnabled = false
                signInWithGoogle(view)
            }
        }
        binding.nuevoUsuario.setOnClickListener {
            view.findNavController().navigate(R.id.action_authFragment_to_emailSignUpFragment)
        }
    }

    private fun signInWithGoogle(view: View) {
        val googleIdOption =
            GetSignInWithGoogleOption.Builder(getString(R.string.web_client_id))
                .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(requireActivity())
                    .getCredential(requireActivity(), request)
                handleSignIn(result, view)
            } catch (e: GetCredentialException) {
                showAlert("Error al identificar usuario", requireContext())
                view.isEnabled = true
                Log.e("AuthFragment", "Error al identificar usuario", e)
            }
        }
    }

    private fun handleSignIn(result: androidx.credentials.GetCredentialResponse, view: View) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential).addOnCompleteListener {
                            if (it.isSuccessful)
                                findNavController().navigate(R.id.action_authFragment_to_monthlyFoodOctagonsReportFragment)
                            else {
                                showAlert("Error al autenticar usuario", requireContext())
                                view.isEnabled = true
                            }
                        }.addOnFailureListener {
                            view.isEnabled = true
                        }
                    } catch (e: GoogleIdTokenParsingException) {
                        view.isEnabled = true
                        showAlert("Error al autenticar usuario", requireContext())
                        Log.e(
                            "AuthFragment",
                            "Se recibió una respuesta de token de identificación de Google no válida",
                            e
                        )
                    }
                } else {
                    view.isEnabled = true
                    showAlert("Tipo inesperado de credencial", requireContext())
                    Log.e("AuthFragment", "Tipo inesperado de credencial")
                }
            }

            else -> {
                view.isEnabled = true
                showAlert("Tipo inesperado de credencial", requireContext())
                Log.e("AuthFragment", "Tipo inesperado de credencial")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}