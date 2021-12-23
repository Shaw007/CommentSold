package com.srmstudios.commentsold.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.srmstudios.commentsold.data.database.dao.InventoryDao
import com.srmstudios.commentsold.data.database.dao.ProductDao
import com.srmstudios.commentsold.data.database.entity.DatabaseInventory
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import com.srmstudios.commentsold.util.DATABASE_VERSION

@Database(entities = [DatabaseProduct::class, DatabaseInventory::class], version = DATABASE_VERSION, exportSchema = false)
abstract class CommentSoldDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun inventoryDao(): InventoryDao

}