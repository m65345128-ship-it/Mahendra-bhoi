package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// Moshi data mapping for Gemini API REST payload
@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(val contents: List<GeminiContent>)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Check if the API Key is a valid user-provided key, in contrast to the placeholder
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Executes content generation query to Gemini.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext generateSimulatedResponse(prompt)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val fullPrompt = if (systemInstruction != null) "$systemInstruction\n\nUser Question:\n$prompt" else prompt
        
        val requestBodyData = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))
            )
        )
        
        val adapter = moshi.adapter(GeminiRequest::class.java)
        val jsonPayload = adapter.toJson(requestBodyData)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonPayload.toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error: ${response.code} - $responseBodyStr")
                    return@withContext "API Request returned error code ${response.code}. Please ensure your API Key is valid."
                }

                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val geminiResponse = responseAdapter.fromJson(responseBodyStr)
                val reply = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return@withContext reply ?: "I analyzed your request, but couldn't produce content. Please try again."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception: ${e.message}", e)
            return@withContext "Network error: ${e.localizedMessage}. Please verify your device's connectivity."
        }
    }

    /**
     * Simulation fallback engine to provide a premium user experience when offline or key is unconfigured.
     */
    private fun generateSimulatedResponse(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("workout") || query.contains("exercise") || query.contains("routine") -> {
                """
                🏋️ **GymGuru Custom Core & Strength Routine**
                
                This personalized muscle-building circuit activates multiple muscle groups to increase density and metabolism.
                
                **1. Dumbbell Incline Press**
                - **Sets**: 4 | **Reps**: 10-12 | **Rest**: 90 sec
                - *Trainer Tip*: Focus on full expansion at the bottom and squeezing core.
                
                **2. Pull-ups or Lat Pulldowns**
                - **Sets**: 3 | **Reps**: 8-10 | **Rest**: 60 sec
                - *Trainer Tip*: Retract scapula before starting the pulling motion.
                
                **3. Squat to Push Press**
                - **Sets**: 3 | **Reps**: 12 | **Rest**: 75 sec
                - *Trainer Tip*: Multi-joint movement. Drives explosive power from hips.
                
                Would you like me to automatically add these exercises to your custom Workout Screen log? Simply press the "Add Suggested Exercise to Routine" button below!
                """.trimIndent()
            }
            query.contains("diet") || query.contains("recipe") || query.contains("meal") || query.contains("eat") -> {
                """
                🥗 **GymGuru AI Clean Carb & Protein Power Bowl**
                
                A balanced fuel recipe optimized for recovery and lean muscle tissue repair.
                
                - **Calories**: 580 kcal
                - **Protein**: 45g | **Carbs**: 65g | **Fats**: 12g
                - **Ingredients**: Grilled skinless chicken breast (150g), steamed brown jasmine rice (1 cup), steamed broccoli (100g), diced ripe avocado (40g), low-sodium tamari glaze.
                - **Instructions**: Toss healthy ingredients into a single bowl. Top with sesame seeds. Perfect for pre or post-exercise nutrition.
                
                Press "Add Meal to Today's Diet log" below to save this instantly!
                """.trimIndent()
            }
            else -> {
                """
                💪 **GymGuru AI Health Assistant**
                
                I am your high-performance wellness and training partner. 
                
                Whether you want to build muscle, fuel recovery with calculated macronutrients, or get visual form advice, I'm ready to advise!
                
                *You can ask me questions like:*
                1. "Design a chest and shoulder workout routine"
                2. "Give me a high-protein dinner recipe under 600 calories"
                3. "Explain the benefits of compound movements"
                
                *Note: GymGuru AI is running in offline simulation mode. Configure a custom Gemini API Key in AI Studio to unlock live real-time answers.*
                """.trimIndent()
            }
        }
    }
}
