package com.srmstudios.commentsold.data.database.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(products: List<DatabaseProduct>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: DatabaseProduct)

    @Update
    suspend fun update(product: DatabaseProduct)

    @Query("delete from product")
    suspend fun deleteAllProducts()

    @Query("delete from product where id = :id")
    suspend fun delete(id: Int)

    /*@Query("select * from product order by id desc")
    fun getProducts(): LiveData<List<DatabaseProduct>>*/

    @Query("select * from product order by id desc")
    fun getProducts(): PagingSource<Int, DatabaseProduct>

    @Query("select * from product where id = :id")
    fun getProductById(id: Int): Flow<DatabaseProduct?>

}