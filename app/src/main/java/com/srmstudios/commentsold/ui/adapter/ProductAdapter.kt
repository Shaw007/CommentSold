package com.srmstudios.commentsold.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.srmstudios.commentsold.databinding.ItemLoadingBinding
import com.srmstudios.commentsold.databinding.ItemProductBinding
import com.srmstudios.commentsold.ui.model.Product
import com.srmstudios.commentsold.util.convertCentsToDollars


class ProductAdapter(private val itemClickListener: (Product) -> Unit) :
    ListAdapter<Product, RecyclerView.ViewHolder>(ProductDillUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_PRODUCT) {
            ProductViewHolder(
                ItemProductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            LoadingViewHolder(
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = getItem(position)

        when (holder) {
            is ProductViewHolder -> {
                holder.bind(product)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).id == -1) {
            ITEM_LOADING
        } else {
            ITEM_PRODUCT
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

        fun bind(product: Product) {
            binding.apply {
                txtTitle.text = product.productName ?: ""
                txtStyle.text = product.style ?: ""
                txtBrand.text = product.brand ?: ""
                txtPrice.text = convertCentsToDollars(product.shippingPrice)
            }
        }
    }

    inner class LoadingViewHolder(private val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ProductDillUtil : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }

    companion object {
        const val ITEM_PRODUCT = 100
        const val ITEM_LOADING = 101
    }
}










