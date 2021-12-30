package com.srmstudios.commentsold.ui.inventory.detail

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
import com.srmstudios.commentsold.data.database.entity.toInventory
import com.srmstudios.commentsold.data.database.entity.toInventoryJoinProduct
import com.srmstudios.commentsold.databinding.FragmentInventoryDetailBinding
import com.srmstudios.commentsold.ui.view_model.InventoryDetailViewModel
import com.srmstudios.commentsold.util.convertCentsToDollars
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryDetailFragment : Fragment(R.layout.fragment_inventory_detail) {
    private lateinit var binding: FragmentInventoryDetailBinding
    private val viewModel: InventoryDetailViewModel by viewModels()
    private val args: InventoryDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInventoryDetailBinding.bind(view)

        setupViews()
    }

    private fun setupViews() {
        viewModel.inventory.observe(viewLifecycleOwner) { databaseInventory ->
            databaseInventory?.toInventoryJoinProduct()?.let { inventory ->
                binding.apply {
                    txtProductName.text = inventory.productName ?: ""
                    txtColor.text = inventory.color ?: ""
                    txtSize.text = inventory.size ?: ""
                    txtQuantity.text = inventory.quantity?.toString() ?: ""
                    txtWeight.text = "${inventory.weight ?: ""} lb"
                    txtPrice.text = convertCentsToDollars(inventory.priceCents)
                    txtSalePrice.text = convertCentsToDollars(inventory.salePriceCents)
                    txtCostPrice.text = convertCentsToDollars(inventory.costCents)
                    txtDimensions.text =
                        "${inventory.length ?: "0"}l x ${inventory.width ?: "0"}w x ${inventory.height ?: "0"}h"
                    txtNote.text = inventory.note ?: ""
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

        viewModel.inventoryDeleted.observe(viewLifecycleOwner) { isInventoryDeleted ->
            if (isInventoryDeleted) {
                // Pop InventoryDetailFragment
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
                    InventoryDetailFragmentDirections.actionInventoryDetailFragmentToCreateEditInventoryFragment(
                        getString(R.string.edit),
                        args.inventory
                    )
                )
            }
            R.id.menu_delete -> {
                showDeleteInventoryDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteInventoryDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_inventory))
            .setMessage(getString(R.string.delete_inventory_confirmation_mesg))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ ->

            }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteInventory()
            }
            .show()
    }
}