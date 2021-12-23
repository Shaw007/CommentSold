package com.srmstudios.commentsold.ui.product.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.database.entity.toProduct
import com.srmstudios.commentsold.databinding.FragmentProductDetailBinding
import com.srmstudios.commentsold.ui.view_model.ProductDetailViewModel
import com.srmstudios.commentsold.util.convertCentsToDollars
import com.srmstudios.commentsold.util.loadImageUrl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {
    private lateinit var binding: FragmentProductDetailBinding
    private val viewModel: ProductDetailViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProductDetailBinding.bind(view)

        setupViews()
    }

    private fun setupViews() {
        viewModel.product.observe(viewLifecycleOwner) { databaseProduct ->
            databaseProduct?.toProduct()?.let { product ->
                binding.apply {
                    img.loadImageUrl(product.url ?: "")
                    txtName.text = product.productName ?: ""
                    txtDesc.text = product.description ?: ""
                    txtStyle.text = product.style ?: ""
                    txtBrand.text = product.brand ?: ""
                    txtType.text = product.productType ?: ""
                    txtPrice.text = convertCentsToDollars(product.shippingPrice)
                    txtNote.text = product.note ?: ""
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

        viewModel.productDeleted.observe(viewLifecycleOwner) { isProductDeleted ->
            if (isProductDeleted) {
                // Pop ProductDetailFragment
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit_delete, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> {
                findNavController().navigate(
                    ProductDetailFragmentDirections.actionProductDetailFragmentToCreateEditProductFragment(
                        getString(R.string.edit),
                        args.product
                    )
                )
            }
            R.id.menu_delete -> {
                showDeleteProductDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteProductDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_product))
            .setMessage(getString(R.string.delete_product_confirmation_mesg))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ ->

            }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteProduct()
            }
            .show()
    }
}












