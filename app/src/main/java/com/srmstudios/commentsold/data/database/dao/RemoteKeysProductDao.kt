package com.srmstudios.commentsold.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.srmstudios.commentsold.data.database.entity.RemoteKeysProduct


@Dao
interface RemoteKeysProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<RemoteKeysProduct>)

    @Query("SELECT * FROM remote_keys_product WHERE productId = :productId")
    suspend fun getRemoteKeyByProductId(productId: Int): RemoteKeysProduct?

    @Query("DELETE FROM remote_keys_product")
    suspend fun clearRemoteKeys()

}