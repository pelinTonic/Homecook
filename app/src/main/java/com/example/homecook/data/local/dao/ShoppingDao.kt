package com.example.homecook.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homecook.data.local.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY name ASC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean)

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()

    suspend fun replaceAll(items: List<ShoppingItemEntity>) {
        clearAll()
        upsertAll(items)
    }
}
