package com.example.hw3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShowDao {
    @Query("SELECT * FROM favourite_show ORDER BY name")
    suspend fun getFavourites(): List<FavouriteShowEntity>

    @Query("SELECT id FROM favourite_show")
    suspend fun getFavouritesIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(show: FavouriteShowEntity)

    @Query("DELETE FROM favourite_show WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_show WHERE id = :id)")
    suspend fun isFavourite(id: Int): Boolean

}