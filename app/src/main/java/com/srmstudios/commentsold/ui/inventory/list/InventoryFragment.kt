package com.srmstudios.commentsold.ui.inventory.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.database.entity.toInventoryJoinProduct
import com.srmstudios.commentsold.data.database.entity.toProducts
import com.srmstudios.commentsold.databinding.FragmentInventoryBinding
import com.srmstudios.commentsold.ui.adapter.InventoryAdapter
import com.srmstudios.commentsold.ui.model.Product
import com.srmstudios.commentsold.ui.view_model.InventoryViewModel
import com.srmstudios.commentsold.util.Resource
import com.srmstudios.commentsold.util.Util
import com.srmstudios.commentsold.util.productSizes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InventoryFragment : Fragment(R.layout.fragment_inventory),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentInventoryBinding
    private lateinit var adapter: InventoryAdapter
    @Inject lateinit var util: Util
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // in order to create inventory we need the product id
        // there should be a drop down of products where user
        // can select a product and add the inventory against it
        // skipping this optional task

        setHasOptionsMenu(true)
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

        viewModel.products.observe(viewLifecycleOwner) { products ->
            context?.let {
                val adapter = ArrayAdapter(
                    it,
                    R.layout.product_style_item,
                    products.toProducts()
                )
                (binding.tilProducts.editText as? AutoCompleteTextView?)?.setAdapter(adapter)
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
                progressBarPagination.isVisible =
                    result is Resource.Loading && result.data == null
                btnRetryColors.isVisible = result is Resource.Error && result.data == null
                result.error?.message?.let { errorMessage ->
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
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
                val showEmptyDataViews = result is Resource.Success && result.data.isNullOrEmpty()

                txtErrorMessage.isVisible = showErrorViews || showEmptyDataViews
                btnRetry.isVisible = showErrorViews || showEmptyDataViews
                if (showEmptyDataViews) {
                    txtErrorMessage.text = getString(R.string.no_inventory_found)
                } else {
                    txtErrorMessage.text = util.parseApiErrorThrowable(result.error)
                }
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
            (tilProducts.editText as? AutoCompleteTextView?)?.setOnItemClickListener { adapterView, _, position, _ ->
                val product = adapterView.getItemAtPosition(position) as? Product
                product?.let {
                    viewModel.setSelectedProduct(it)
                }
            }

            (tilColors.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
                val color = parent.getItemAtPosition(position) as String?
                viewModel.setSelectedColor(color)
            }

            (tilSize.editText as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
                val size = parent.getItemAtPosition(position) as String?
                viewModel.setSelectedSize(size)
            }

            edtQuantity.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    viewModel.setSelectedQuantity(p0?.toString())
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            btnRetryColors.setOnClickListener {
                viewModel.fetchProductColors()
            }

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