package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        WeightLog::class,
        WaterLog::class,
        MealItem::class,
        ExerciseItem::class,
        NotifLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fitDao(): FitDao
}
