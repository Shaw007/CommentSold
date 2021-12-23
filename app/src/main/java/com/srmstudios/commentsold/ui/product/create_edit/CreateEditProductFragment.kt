package com.srmstudios.commentsold.ui.product.create_edit

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.database.entity.toProduct
import com.srmstudios.commentsold.databinding.FragmentCreateEditProductBinding
import com.srmstudios.commentsold.ui.view_model.ProductDetailViewModel
import com.srmstudios.commentsold.util.Resource
import com.srmstudios.commentsold.util.updateText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateEditProductFragment : Fragment(R.layout.fragment_create_edit_product) {
    private lateinit var binding: FragmentCreateEditProductBinding
    private val viewModel: ProductDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateEditProductBinding.bind(view)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        viewModel.isCreateScreen.observe(viewLifecycleOwner) { isCreateScreen ->
            if (!isCreateScreen) {
                // User came to edit a product
                binding.btnCreateUpdate.text = getString(R.string.edit)

                viewModel.product.observe(viewLifecycleOwner) { databaseProduct ->
                    databaseProduct?.toProduct()?.let { product ->
                        binding.apply {
                            edtName.updateText(product.productName ?: "")
                            edtDesc.updateText(product.description ?: "")
                            edtBrand.updateText(product.brand ?: "")
                            edtPrice.updateText(product.shippingPrice.toString())
                            (tilStyles.editText as? AutoCompleteTextView)?.setText(
                                product.style,
                                false
                            )
                        }
                    }
                }
            }
        }

        viewModel.productStyles.observe(viewLifecycleOwner) { result ->
            result.data?.styles?.let { productStyles ->
                val stylesAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.product_style_item,
                    productStyles
                )
                (binding.tilStyles.editText as? AutoCompleteTextView)?.setAdapter(stylesAdapter)
            }

            binding.apply {
                progressBar.isVisible =
                    result is Resource.Loading && result.data == null
                btnRetryStyles.isVisible = result is Resource.Error && result.data == null
                result.error?.message?.let { errorMessage ->
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.progressBar.observe(viewLifecycleOwner) { visibility ->
            binding.progressBar.isVisible = visibility
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.doneShowingMessage()
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnRetryStyles.setOnClickListener {
                viewModel.fetchProductStyles()
            }

            btnCreateUpdate.setOnClickListener {
                viewModel.createEditProduct(
                    edtName.text.toString(),
                    edtDesc.text.toString(),
                    (tilStyles.editText as? AutoCompleteTextView)?.text?.toString() ?: "",
                    edtBrand.text.toString(),
                    edtPrice.text.toString()
                )
            }
        }
    }
}




