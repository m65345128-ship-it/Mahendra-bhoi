package com.example.data

import kotlinx.coroutines.flow.Flow

class FitRepository(private val fitDao: FitDao) {
    val userProfile: Flow<UserProfile?> = fitDao.getUserProfileFlow()

    suspend fun getProfileDirect(): UserProfile? = fitDao.getUserProfile()
    suspend fun saveProfile(profile: UserProfile) = fitDao.insertProfile(profile)

    val weightLogs: Flow<List<WeightLog>> = fitDao.getAllWeightLogsFlow()
    suspend fun addWeightLog(weight: Float, timestamp: Long) = fitDao.insertWeightLog(WeightLog(weightKg = weight, timestamp = timestamp))
    suspend fun deleteWeightLog(id: Long) = fitDao.deleteWeightLog(id)

    fun getWaterLogsFlow(dateStr: String): Flow<List<WaterLog>> = fitDao.getWaterLogsForDateFlow(dateStr)
    suspend fun addWaterLog(amountMl: Int, dateStr: String) = fitDao.insertWaterLog(WaterLog(amountMl = amountMl, dateStr = dateStr))
    suspend fun clearWaterLogs(dateStr: String) = fitDao.clearWaterLogsForDate(dateStr)

    val meals: Flow<List<MealItem>> = fitDao.getAllMealsFlow()
    suspend fun addMeal(meal: MealItem) = fitDao.insertMeal(meal)
    suspend fun deleteMealId(id: Long) = fitDao.deleteMeal(id)
    suspend fun setMealEaten(mealId: Long, isEaten: Boolean) = fitDao.setMealEatenToday(mealId, isEaten)
    suspend fun resetAllMealsEaten() = fitDao.updateAllMealsEatenStatus(false)

    val exercises: Flow<List<ExerciseItem>> = fitDao.getAllExercisesFlow()
    suspend fun addExercise(exercise: ExerciseItem) = fitDao.insertExercise(exercise)
    suspend fun setExerciseCompleted(id: Long, isCompleted: Boolean) = fitDao.setExerciseCompletedToday(id, isCompleted)
    suspend fun resetAllWorkouts() = fitDao.resetAllExercisesCompleted()

    val notifications: Flow<List<NotifLog>> = fitDao.getAllNotificationsFlow()
    suspend fun addNotification(title: String, message: String, category: String) = 
        fitDao.insertNotification(NotifLog(title = title, message = message, category = category))
    suspend fun insertNotification(log: NotifLog) = fitDao.insertNotification(log)
    suspend fun deleteNotification(id: Long) = fitDao.deleteNotification(id)
    suspend fun markAllNotificationsAsRead() = fitDao.markAllNotificationsRead()
}
