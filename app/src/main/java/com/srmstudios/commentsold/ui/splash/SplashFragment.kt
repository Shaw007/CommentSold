package com.srmstudios.commentsold.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.databinding.FragmentSplashBinding
import com.srmstudios.commentsold.ui.view_model.SPLASH_NAVIGATION
import com.srmstudios.commentsold.ui.view_model.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {
    private lateinit var binding: FragmentSplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashBinding.bind(view)

        setupViews()
    }

    private fun setupViews() {
        viewModel.splashNavigation.observe(viewLifecycleOwner) { screen ->
            when (screen) {
                SPLASH_NAVIGATION.LOGIN -> {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
                SPLASH_NAVIGATION.HOME -> {
                    findNavController().navigate(R.id.action_splashFragment_to_productsFragment)
                }
            }
        }
    }
}







