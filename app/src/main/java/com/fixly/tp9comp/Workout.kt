package com.fixly.tp9comp


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val sport: String,
    val distance: Double,
    val calories: Double,
    val duration: Long,

    val synced: Boolean = false
)
