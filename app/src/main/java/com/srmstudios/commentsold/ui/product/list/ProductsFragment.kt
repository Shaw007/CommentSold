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
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.databinding.FragmentProductsBinding
import com.srmstudios.commentsold.ui.adapter.ProductAdapter
import com.srmstudios.commentsold.ui.adapter.ProductLoadStateAdapter
import com.srmstudios.commentsold.ui.view_model.ProductViewModel
import com.srmstudios.commentsold.util.NUM_COLUMNS_PRODUCTS
import com.srmstudios.commentsold.util.Util
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentProductsBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val viewModel: ProductViewModel by viewModels()

    @Inject
    lateinit var util: Util

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
        adapter = ProductAdapter { product ->
            product?.let { product ->
                findNavController().navigate(
                    ProductsFragmentDirections.actionProductsFragmentToProductDetailFragment(
                        product.productName ?: getString(R.string.product_details),
                        product
                    )
                )
            }
        }

        binding.apply {
            val footerAdapter = ProductLoadStateAdapter(util) { adapter.retry() }

            gridLayoutManager = GridLayoutManager(context, NUM_COLUMNS_PRODUCTS)
            recyclerViewProducts.layoutManager = gridLayoutManager
            recyclerViewProducts.setHasFixedSize(true)
            recyclerViewProducts.adapter = adapter.withLoadStateFooter(footer = footerAdapter)

            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == adapter.itemCount && footerAdapter.itemCount > 0) {
                        NUM_COLUMNS_PRODUCTS
                    } else {
                        // Loading View
                        1
                    }
                }
            }
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

        viewModel.products.observe(viewLifecycleOwner) { productsPagingData ->
            adapter.submitData(viewLifecycleOwner.lifecycle, productsPagingData)
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnRetry.setOnClickListener {
                refreshProducts()
            }

            swipeRefreshLayout.setOnRefreshListener(this@ProductsFragment)
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                // this means that initial data load call is in progress
                swipeRefreshLayout.isRefreshing = loadState.mediator?.refresh is LoadState.Loading

                // this means that loading has finished and there is no error
                // so we need to make the recyclerview visible
                recyclerViewProducts.isVisible =
                    loadState.prepend is LoadState.NotLoading || loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading

                // this means that some error has occurred in the initial data load call
                val hasErrorOccurred =
                    loadState.source.refresh is LoadState.Error || loadState.mediator?.refresh is LoadState.Error
                btnRetry.isVisible = hasErrorOccurred && adapter.itemCount == 0
                txtErrorMessage.isVisible = hasErrorOccurred && adapter.itemCount == 0
                txtErrorMessage.text =
                    util.parseApiErrorThrowable((loadState.refresh as? LoadState.Error)?.error)
                if (hasErrorOccurred && adapter.itemCount != 0) {
                    // this means some error has occurred in refreshing data from the API call
                    // but data is present in the local db
                    // so here we show only a toast message
                    Toast.makeText(
                        context,
                        util.parseApiErrorThrowable((loadState.refresh as LoadState.Error).error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // this means there are no products found in the initial data load call
                // and also no products are present in the local db
                if (loadState.refresh is LoadState.NotLoading &&
                    loadState.prepend is LoadState.NotLoading &&
                    loadState.source.refresh is LoadState.NotLoading &&
                    loadState.mediator?.refresh is LoadState.NotLoading &&
                    //loadState.append.endOfPaginationReached &&
                    adapter.itemCount == 0
                ) {
                    recyclerViewProducts.isVisible = false
                    btnRetry.isVisible = true
                    txtErrorMessage.isVisible = true
                    txtErrorMessage.text = getString(R.string.no_products_found)
                }
            }
        }
    }

    // pull to refresh
    override fun onRefresh() {
        refreshProducts()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshProducts() {
        if (!util.isNetworkAvailable()) {
            Toast.makeText(
                context,
                util.getStringByResId(R.string.please_check_internent),
                Toast.LENGTH_SHORT
            ).show()
            binding.swipeRefreshLayout.isRefreshing = false
            return
        }

        // start fetching fresh products
        adapter.refresh()
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











