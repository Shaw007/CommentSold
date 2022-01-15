package com.srmstudios.commentsold.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.databinding.FragmentLoginBinding
import com.srmstudios.commentsold.ui.view_model.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        setupViews()
        setupListeners()
    }

    private fun setupViews(){
        // Testing Credentials
        binding.apply {
            edtEmail.setText("hortensia.dollison@blargmail.org")
            edtPassword.setText("AgcGcJxeig")

            /*edtEmail.setText("tamar.poyer@barmail.com")
            edtPassword.setText("hdSEjsQWxg")*/
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                viewModel.validateCredentialsAndLogin(
                    edtEmail.text.toString(),
                    edtPassword.text.toString()
                )
            }
        }

        viewModel.progressBar.observe(viewLifecycleOwner) { visibility ->
            binding.progressBarPagination.isVisible = visibility
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.doneShowingMessage()
            }
        }

        viewModel.navigateToHomeScreen.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.action_loginFragment_to_productsFragment)
                viewModel.doneNavigatingToHomeScreen()
            }
        }
    }
}












