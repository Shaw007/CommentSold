package com.srmstudios.commentsold.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.srmstudios.commentsold.databinding.ItemLoadingBinding
import com.srmstudios.commentsold.databinding.ItemProductBinding
import com.srmstudios.commentsold.ui.model.Product
import com.srmstudios.commentsold.util.Util
import com.srmstudios.commentsold.util.convertCentsToDollars

class ProductAdapter(
    private val itemClickListener: (Product?) -> Unit
) :
    PagingDataAdapter<Product, RecyclerView.ViewHolder>(ProductDillUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = getItem(position)

        when (holder) {
            is ProductViewHolder -> {
                holder.bind(product)
            }
        }
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val product = getItem(adapterPosition)
                itemClickListener(product)
            }
        }

        fun bind(product: Product?) {
            product?.let { product ->
                binding.apply {
                    txtTitle.text = product.productName ?: ""
                    txtStyle.text = product.style ?: ""
                    txtBrand.text = product.brand ?: ""
                    txtPrice.text = convertCentsToDollars(product.shippingPrice)
                }
            }
        }
    }

    class ProductDillUtil : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Product, newItem: Product) =
            oldItem == newItem
    }
}

class ProductLoadStateAdapter(private val util: Util, private val retry: () -> Unit) :
    LoadStateAdapter<ProductLoadStateAdapter.ProductLoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ProductLoadStateViewHolder {
        return ProductLoadStateViewHolder(
            ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProductLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    inner class ProductLoadStateViewHolder(
        private val binding: ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.txtErrorMessage.text = util.parseApiErrorThrowable(loadState.error)
            }
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.btnRetry.isVisible = loadState is LoadState.Error
            binding.txtErrorMessage.isVisible = loadState is LoadState.Error
        }
    }
}