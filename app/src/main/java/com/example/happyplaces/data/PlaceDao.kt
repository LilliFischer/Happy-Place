package com.example.happyplaces.data

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PlaceDao {
    @Query("SELECT * FROM places")
    fun getAll(): LiveData<List<Place>>

    @Insert
    suspend fun insert(place: Place)

    @Update
    suspend fun update(place: Place)

    @Delete
    suspend fun delete(place: Place)
}
