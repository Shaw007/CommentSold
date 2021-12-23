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
import com.srmstudios.commentsold.ui.view_model.ProductViewModel
import com.srmstudios.commentsold.util.Resource
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products),
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentProductsBinding
    private lateinit var adapter: ProductAdapter
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
            recyclerViewProducts.adapter = adapter
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

        viewModel.navigateToLoginScreen.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.action_productsFragment_to_loginFragment)
                viewModel.doneNavigatingToLoginScreen()
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { result ->
            result.data?.let { products ->
                adapter.submitList(products.toProducts())
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
        binding.btnRetry.setOnClickListener {
            viewModel.fetchProducts()
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











