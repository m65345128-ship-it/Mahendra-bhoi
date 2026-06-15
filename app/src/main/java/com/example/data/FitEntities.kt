package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Rivera",
    val age: Int = 26,
    val gender: String = "Male",
    val heightCm: Float = 178f,
    val weightKg: Float = 76.5f,
    val goal: String = "Build Muscle", // "Build Muscle", "Lose Weight", "Lean Bulk", "Maintain"
    val activityLevel: String = "Active", // "Sedentary", "Active", "Very Active"
    val isPremium: Boolean = false,
    val isDarkMode: Boolean = true,
    val stepCountSimulated: Int = 4250,
    val lastStepCounterReset: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val dateStr: String // "yyyy-MM-dd"
)

@Entity(tableName = "meals")
data class MealItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val isVegetarian: Boolean,
    val isEatenToday: Boolean = false,
    val customCreated: Boolean = false
)

@Entity(tableName = "exercises")
data class ExerciseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Full Body"
    val reps: Int,
    val sets: Int,
    val restSec: Int,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val isCompletedToday: Boolean = false,
    val animationType: String = "press" // press, pull, lift, squat, cardio, core
)

@Entity(tableName = "notif_logs")
data class NotifLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val category: String, // "Workout", "Diet", "Motivation"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
