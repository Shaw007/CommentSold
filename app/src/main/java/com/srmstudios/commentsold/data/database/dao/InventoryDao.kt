package com.srmstudios.commentsold.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.srmstudios.commentsold.data.database.entity.DatabaseInventory
import com.srmstudios.commentsold.data.database.entity.DatabaseInventoryJoinProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventoryList: List<DatabaseInventory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: DatabaseInventory)

    @Update
    suspend fun update(inventory: DatabaseInventory)

    @Query("delete from inventory")
    suspend fun deleteAllInventory()

    @Query("delete from inventory where id = :id")
    suspend fun delete(id: Int)

    @Query("select * from inventory order by id desc")
    fun getInventoryList(): LiveData<List<DatabaseInventory>>

    // Join of Product and Inventory Table in order to get the product name by product id
    @Query("select i.id as id,i.product_id as product_id,p.product_name as product_name,i.quantity as quantity,i.color as color,i.size as size,i.weight as weight,i.price_cents as price_cents,i.sale_price_cents as sale_price_cents,i.cost_cents as cost_cents,i.sku as sku,i.length as length,i.width as width,i.height as height,i.note as note from inventory i left join product p on i.product_id = p.id order by id desc")
    fun getInventoryListJoinProduct(): LiveData<List<DatabaseInventoryJoinProduct>>

    @Query("select i.id as id,i.product_id as product_id,p.product_name as product_name,i.quantity as quantity,i.color as color,i.size as size,i.weight as weight,i.price_cents as price_cents,i.sale_price_cents as sale_price_cents,i.cost_cents as cost_cents,i.sku as sku,i.length as length,i.width as width,i.height as height,i.note as note from inventory i left join product p on i.product_id = p.id where i.id = :id")
    fun getInventoryById(id: Int): Flow<DatabaseInventoryJoinProduct?>

}