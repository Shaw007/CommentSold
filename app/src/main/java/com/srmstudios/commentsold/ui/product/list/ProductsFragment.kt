package com.srmstudios.commentsold.ui.product.list

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
import com.srmstudios.commentsold.data.database.entity.toProducts
import com.srmstudios.commentsold.databinding.FragmentProductsBinding
import com.srmstudios.commentsold.ui.adapter.ProductAdapter
import com.srmstudios.commentsold.ui.model.Product
import com.srmstudios.commentsold.ui.view_model.ProductViewModel
import com.srmstudios.commentsold.util.Resource
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentProductsBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProductsBinding.bind(view)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        gridLayoutManager = GridLayoutManager(context, 2)

        adapter = ProductAdapter { product ->
            findNavController().navigate(
                ProductsFragmentDirections.actionProductsFragmentToProductDetailFragment(
                    product.productName ?: getString(R.string.product_details),
                    product
                )
            )
        }

        binding.apply {
            swipeRefreshLayout.setOnRefreshListener(this@ProductsFragment)
            recyclerViewProducts.setHasFixedSize(true)
            recyclerViewProducts.layoutManager = gridLayoutManager
            recyclerViewProducts.adapter = adapter
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.doneShowingMessage()
            }
        }

        viewModel.navigateToLoginScreen.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.action_productsFragment_to_loginFragment)
                viewModel.doneNavigatingToLoginScreen()
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { result ->
            binding.apply {
                result.data?.let { products ->
                    adapter.submitList(products.toProducts())
                }

                swipeRefreshLayout.isRefreshing =
                    result is Resource.Loading && result.data.isNullOrEmpty()

                val showErrorViews = result is Resource.Error && result.data.isNullOrEmpty()
                val showEmptyDataViews = result is Resource.Success && result.data.isNullOrEmpty()

                txtErrorMessage.isVisible = showErrorViews || showEmptyDataViews
                btnRetry.isVisible = showErrorViews || showEmptyDataViews
                if (showEmptyDataViews) {
                    txtErrorMessage.text = getString(R.string.no_products_found)
                } else {
                    txtErrorMessage.text = result.error?.message
                }
            }
        }

        viewModel.isLoadMoreInProgress.observe(viewLifecycleOwner) { isLoadMoreInProgress ->
            if(isLoadMoreInProgress) {
                val updatedProducts = adapter.currentList.toMutableList()
                updatedProducts.add(Product(id = -1))
                adapter.submitList(updatedProducts)
            }else{
                val updatedProducts = adapter.currentList.toMutableList()
                updatedProducts.removeAll { it.id == -1 }
                adapter.submitList(updatedProducts)
            }
        }
    }

    private fun setupListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.fetchProducts()
        }

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    ProductAdapter.ITEM_LOADING -> {
                        2
                    }
                    else -> {
                        1
                    }
                }
            }
        }

        binding.recyclerViewProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                if (adapter.itemCount > 0 && lastPosition == adapter.itemCount - 1) {
                    viewModel.loadMore()
                }
            }
        })
    }

    // pull to refresh
    override fun onRefresh() {
        binding.swipeRefreshLayout.isRefreshing = viewModel.fetchProducts()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_products, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create -> {
                findNavController().navigate(
                    ProductsFragmentDirections.actionProductsFragmentToCreateEditProductFragment(
                        getString(R.string.create),
                        null
                    )
                )
            }
            R.id.menu_logout -> {
                viewModel.logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}











