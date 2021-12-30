package com.srmstudios.commentsold.ui.inventory.create_edit

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.database.entity.toInventory
import com.srmstudios.commentsold.data.database.entity.toInventoryJoinProduct
import com.srmstudios.commentsold.databinding.FragmentCreateEditInventoryBinding
import com.srmstudios.commentsold.ui.view_model.InventoryDetailViewModel
import com.srmstudios.commentsold.util.Resource
import com.srmstudios.commentsold.util.productSizes
import com.srmstudios.commentsold.util.updateText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateEditInventoryFragment : Fragment(R.layout.fragment_create_edit_inventory) {
    private lateinit var binding: FragmentCreateEditInventoryBinding
    private val viewModel: InventoryDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreateEditInventoryBinding.bind(view)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        viewModel.isCreateScreen.observe(viewLifecycleOwner) { isCreateScreen ->
            if (!isCreateScreen) {
                // User came to edit a inventory
                binding.btnCreateUpdate.text = getString(R.string.edit)

                viewModel.inventory.observe(viewLifecycleOwner) { databaseProduct ->
                    databaseProduct?.toInventoryJoinProduct()?.let { inventory ->
                        binding.apply {
                            edtQuantity.updateText(inventory.quantity?.toString() ?: "0")
                            edtWeight.updateText(inventory.weight ?: "0")
                            edtPrice.updateText(inventory.priceCents?.toString() ?: "0")
                            edtSalePrice.updateText(inventory.salePriceCents?.toString() ?: "0")
                            edtCostPrice.updateText(inventory.costCents?.toString() ?: "0")
                            edtLength.updateText(inventory.length ?: "0")
                            edtWidth.updateText(inventory.width ?: "0")
                            edtHeight.updateText(inventory.height ?: "0")
                            edtNote.updateText(inventory.note ?: "")
                            (tilColors.editText as? AutoCompleteTextView)?.setText(
                                inventory.color,
                                false
                            )
                            (tilSize.editText as? AutoCompleteTextView)?.setText(
                                inventory.size,
                                false
                            )
                        }
                    }
                }
            }
        }

        viewModel.productColors.observe(viewLifecycleOwner) { result ->
            result.data?.colors?.let { productColors ->
                val colorsAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.product_style_item,
                    productColors
                )
                (binding.tilColors.editText as? AutoCompleteTextView)?.setAdapter(colorsAdapter)
            }

            binding.apply {
                progressBar.isVisible =
                    result is Resource.Loading && result.data == null
                btnRetryColors.isVisible = result is Resource.Error && result.data == null
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

        val sizesAdapter = ArrayAdapter(
            requireContext(),
            R.layout.product_style_item,
            productSizes
        )
        (binding.tilSize.editText as? AutoCompleteTextView)?.setAdapter(sizesAdapter)
    }

    private fun setupListeners() {
        binding.apply {
            btnRetryColors.setOnClickListener {
                viewModel.fetchProductColors()
            }

            btnCreateUpdate.setOnClickListener {
                viewModel.createEditInventory(
                    (tilColors.editText as? AutoCompleteTextView)?.text?.toString() ?: "",
                    (tilSize.editText as? AutoCompleteTextView)?.text?.toString() ?: "",
                    edtQuantity.text.toString(),
                    edtWeight.text.toString(),
                    edtPrice.text.toString(),
                    edtSalePrice.text.toString(),
                    edtCostPrice.text.toString(),
                    edtLength.text.toString(),
                    edtWidth.text.toString(),
                    edtHeight.text.toString(),
                    edtNote.text?.toString()
                )
            }
        }
    }
}