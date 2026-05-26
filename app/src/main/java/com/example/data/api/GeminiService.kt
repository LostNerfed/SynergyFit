package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.database.FitSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Serializable
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // structures for Moshi
    data class RequestPayload(
        val contents: List<ContentJson>,
        val generationConfig: GenerationConfigJson? = null,
        val systemInstruction: ContentJson? = null
    )

    data class ContentJson(
        val parts: List<PartJson>
    )

    data class PartJson(
        val text: String
    )

    data class GenerationConfigJson(
        val responseMimeType: String? = null,
        val temperature: Float? = null
    )

    data class ResponsePayload(
        val candidates: List<CandidateJson>?
    )

    data class CandidateJson(
        val content: ContentJson?
    )

    private fun getApiKey(settings: FitSettings?): String {
        val userKey = settings?.apiKey ?: ""
        return if (userKey.trim().isNotEmpty()) {
            userKey.trim()
        } else {
            BuildConfig.GEMINI_API_KEY
        }
    }

    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String? = null,
        isJsonMode: Boolean = false,
        settings: FitSettings? = null
    ): String = withContext(Dispatchers.IO) {
        val key = getApiKey(settings)
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return@withContext "Error: API Key de Gemini no configurada. Configure su API key en los Ajustes o en el perfil de IA."
        }

        val url = "$BASE_URL?key=$key"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val contents = listOf(ContentJson(parts = listOf(PartJson(text = prompt))))
        val systemContent = systemInstruction?.let { ContentJson(parts = listOf(PartJson(text = it))) }
        val config = if (isJsonMode) GenerationConfigJson(responseMimeType = "application/json") else null

        val requestPayload = RequestPayload(
            contents = contents,
            generationConfig = config,
            systemInstruction = systemContent
        )

        try {
            val jsonAdapter = moshi.adapter(RequestPayload::class.java)
            val requestBodyJson = jsonAdapter.toJson(requestPayload)

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toRequestBody(mediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Code ${response.code}, body: $errorBody")
                    // Clean up Google Cloud API response errors for user readability
                    val cleanError = if (errorBody.contains("API_KEY_INVALID")) {
                        "API Key inválida. Por favor, revisa tus ajustes."
                    } else if (response.code == 400) {
                        "Error en la petición de IA. Por favor, asegúrate de que el formato de clave sea correcto."
                    } else {
                        "Error de servidor (${response.code})."
                    }
                    return@withContext "Error: $cleanError"
                }

                val responseBody = response.body?.string() ?: ""
                val responseAdapter = moshi.adapter(ResponsePayload::class.java)
                val mappedResponse = responseAdapter.fromJson(responseBody)

                val replyText = mappedResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return@withContext replyText ?: "No se recibió respuesta de IA."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response from prompt", e)
            return@withContext "Error de red o procesamiento: ${e.localizedMessage}"
        }
    }

    // JSON structure for meal analysis
    data class MealAnalysisResult(
        val calories: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val foods: List<FoodAnalysisResult>
    ) : Serializable

    data class FoodAnalysisResult(
        val name: String,
        val calories: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double
    ) : Serializable

    suspend fun analyzeMeal(
        description: String,
        settings: FitSettings? = null
    ): MealAnalysisResult? = withContext(Dispatchers.IO) {
        val prompt = SystemPromptProvider.getMealAnalysisPrompt(description)

        val jsonResponse = generateResponse(
            prompt = prompt,
            isJsonMode = true,
            settings = settings
        )

        if (jsonResponse.startsWith("Error")) {
            Log.e(TAG, "Failed to analyze meal: $jsonResponse")
            return@withContext null
        }

        try {
            val analysisAdapter = moshi.adapter(MealAnalysisResult::class.java)
            var cleanJson = jsonResponse.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json")
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```")
            }
            cleanJson = cleanJson.trim()

            return@withContext analysisAdapter.fromJson(cleanJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing meal analysis JSON output", e)
            return@withContext null
        }
    }
}

object SystemPromptProvider {
    fun getMealAnalysisPrompt(description: String): String {
        return """
            Analiza la descripción de esta comida en español: "$description"
            Extrae las calorías totales (kcal), proteínas totales (g), carbohidratos totales (g), grasas totales (g).
            Además, divide la comida en sus alimentos individuales.
            Devuelve OBLIGATORIAMENTE un JSON con el siguiente formato exacto:
            {
              "calories": 350,
              "protein": 24.5,
              "carbs": 30.0,
              "fat": 12.0,
              "foods": [
                {
                  "name": "huevo frito",
                  "calories": 140,
                  "protein": 12.0,
                  "carbs": 1.0,
                  "fat": 10.0
                },
                {
                  "name": "pan tostado",
                  "calories": 210,
                  "protein": 12.5,
                  "carbs": 29.0,
                  "fat": 2.0
                }
              ]
            }
            Por favor, sé lo más nutricionalmente preciso posible basándote en la descripción provista. Si es una comida compleja, estima según proporciones saludables.
        """.trimIndent()
    }
}
