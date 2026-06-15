package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitDao {
    // Profile
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    // Weight logs
    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeightLogsFlow(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLog(id: Long)

    // Water logs for date
    @Query("SELECT * FROM water_logs WHERE dateStr = :dateStr")
    fun getWaterLogsForDateFlow(dateStr: String): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE dateStr = :dateStr")
    suspend fun getWaterLogsForDate(dateStr: String): List<WaterLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE dateStr = :dateStr")
    suspend fun clearWaterLogsForDate(dateStr: String)

    // Meals
    @Query("SELECT * FROM meals ORDER BY id DESC")
    fun getAllMealsFlow(): Flow<List<MealItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealItem)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: Long)

    @Query("UPDATE meals SET isEatenToday = :isEaten")
    suspend fun updateAllMealsEatenStatus(isEaten: Boolean)

    @Query("UPDATE meals SET isEatenToday = :isEaten WHERE id = :mealId")
    suspend fun setMealEatenToday(mealId: Long, isEaten: Boolean)

    // Exercises
    @Query("SELECT * FROM exercises ORDER BY id ASC")
    fun getAllExercisesFlow(): Flow<List<ExerciseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseItem)

    @Query("UPDATE exercises SET isCompletedToday = :isCompleted WHERE id = :exerciseId")
    suspend fun setExerciseCompletedToday(exerciseId: Long, isCompleted: Boolean)

    @Query("UPDATE exercises SET isCompletedToday = 0")
    suspend fun resetAllExercisesCompleted()

    // Notification Alerts
    @Query("SELECT * FROM notif_logs ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotifLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notif: NotifLog)

    @Query("DELETE FROM notif_logs WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("UPDATE notif_logs SET isRead = 1")
    suspend fun markAllNotificationsRead()
}
