package com.fixly.tp9comp


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insert(workout: Workout)

    @Query("SELECT * FROM workouts ORDER BY id DESC")
    suspend fun getAll(): List<Workout>

    @Query("SELECT * FROM workouts WHERE synced = 0")
    suspend fun getUnsynced(): List<Workout>

    @Query("UPDATE workouts SET synced = 1 WHERE synced = 0")
    suspend fun markAllSynced()
}
