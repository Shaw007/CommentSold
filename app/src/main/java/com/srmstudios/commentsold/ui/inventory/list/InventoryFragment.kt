package com.srmstudios.commentsold.ui.inventory.list

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.database.entity.toInventory
import com.srmstudios.commentsold.data.database.entity.toInventoryJoinProduct
import com.srmstudios.commentsold.databinding.FragmentInventoryBinding
import com.srmstudios.commentsold.ui.adapter.InventoryAdapter
import com.srmstudios.commentsold.ui.view_model.InventoryViewModel
import com.srmstudios.commentsold.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventoryFragment : Fragment(R.layout.fragment_inventory),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentInventoryBinding
    private lateinit var adapter: InventoryAdapter
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // in order to create inventory we need the product id
        // there should be a drop down of products where user
        // can select a product and add the inventory against it
        // skipping this optional task

        //setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInventoryBinding.bind(view)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        adapter = InventoryAdapter { inventory ->
            findNavController().navigate(
                InventoryFragmentDirections.actionInventoryFragmentToInventoryDetailFragment(
                    inventory.productName ?: getString(R.string.details),
                    inventory
                )
            )
        }

        binding.apply {
            swipeRefreshLayout.setOnRefreshListener(this@InventoryFragment)
            recyclerViewInventory.setHasFixedSize(true)
            recyclerViewInventory.adapter = adapter
        }

        viewModel.progressBarPagination.observe(viewLifecycleOwner) { visibility ->
            binding.progressBarPagination.isVisible = visibility
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.doneShowingMessage()
            }
        }

        viewModel.inventoryList.observe(viewLifecycleOwner) { result ->
            result.data?.let { inventoryList ->
                adapter.submitList(inventoryList.toInventoryJoinProduct())
            }

            binding.apply {
                swipeRefreshLayout.isRefreshing =
                    result is Resource.Loading && result.data.isNullOrEmpty()

                val showErrorViews = result is Resource.Error && result.data.isNullOrEmpty()
                txtErrorMessage.isVisible = showErrorViews
                btnRetry.isVisible = showErrorViews
                txtErrorMessage.text = result.error?.message
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnRetry.setOnClickListener {
                viewModel.fetchInventoryList()
            }

            recyclerViewInventory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (adapter.itemCount > 0 && lastPosition == adapter.itemCount - 1) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    // pull to refresh
    override fun onRefresh() {
        binding.swipeRefreshLayout.isRefreshing = viewModel.fetchInventoryList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create -> {
                findNavController().navigate(
                    InventoryFragmentDirections.actionInventoryFragmentToCreateEditInventoryFragment(
                        getString(R.string.create),
                        null
                    )
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }
}