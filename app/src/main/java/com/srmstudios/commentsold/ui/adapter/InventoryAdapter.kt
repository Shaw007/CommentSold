package com.srmstudios.commentsold.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.srmstudios.commentsold.databinding.ItemInventoryBinding
import com.srmstudios.commentsold.ui.model.Inventory
import com.srmstudios.commentsold.util.convertCentsToDollars

class InventoryAdapter(private val itemClickListener: (Inventory) -> Unit) :
    ListAdapter<Inventory, RecyclerView.ViewHolder>(InventoryDillUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InventoryViewHolder(
            ItemInventoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val inventory = getItem(position)

        when (holder) {
            is InventoryViewHolder -> {
                holder.bind(inventory)
            }
        }
    }

    inner class InventoryViewHolder(private val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val inventory = getItem(adapterPosition)
                itemClickListener(inventory)
            }
        }

        fun bind(inventory: Inventory) {
            binding.apply {
                txtProductName.text = inventory.productName ?: ""
                txtColor.text = inventory.color ?: ""
                txtSize.text = inventory.size ?: ""
                txtQuantity.text = inventory.quantity?.toString() ?: ""
                txtWeight.text = "${inventory.weight ?: "0"} lb"
                txtPrice.text = convertCentsToDollars(inventory.priceCents)
                txtPrice.paintFlags = txtPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                txtSalePrice.text = convertCentsToDollars(inventory.salePriceCents)
            }
        }
    }

    class InventoryDillUtil : DiffUtil.ItemCallback<Inventory>() {
        override fun areItemsTheSame(oldItem: Inventory, newItem: Inventory) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Inventory, newItem: Inventory) = oldItem == newItem
    }
}