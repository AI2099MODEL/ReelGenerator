package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lightweight Gemini client for the Chat assistant ("MyLyfe Desk").
 *
 * Uses the generativelanguage REST API directly via OkHttp (already a project
 * dependency) so no Firebase project setup is required. The API key is read
 * from BuildConfig.GEMINI_API_KEY, which the secrets-gradle-plugin injects
 * from the local .env file at build time. If the key is missing or the network
 * call fails, a graceful fallback message is returned so the Chat screen never
 * silently drops a user's turn.
 */
object ChatAi {
    private const val TAG = "ChatAi"
    private const val MODEL = "gemini-2.5-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
    private val client = OkHttpClient()

    private const val FALLBACK =
        "I'm here, but I can't reach the assistant right now. Your message is saved in this thread."

    private const val SYSTEM_PROMPT =
        "You are MyLyfe Desk, a warm, concise personal assistant inside the 'MyLyfe' " +
            "organizer app. Help the user with diary reflections, event planning, tasks, " +
            "and document-vault organization. Keep replies friendly and under 120 words."

    data class Message(val isUser: Boolean, val text: String)

    suspend fun generateReply(history: List<Message>, userText: String): String =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "GEMINI_API_KEY_DEFAULT" || apiKey.contains("PLACEHOLDER")) {
                Log.w(TAG, "GEMINI_API_KEY not set; using offline fallback reply.")
                return@withContext FALLBACK
            }
            try {
                val contents = JSONArray()
                (history + Message(isUser = true, text = userText)).forEach { m ->
                    contents.put(
                        JSONObject().apply {
                            put("role", if (m.isUser) "user" else "model")
                            put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", m.text))
                            )
                        }
                    )
                }
                val body = JSONObject().apply {
                    put(
                        "systemInstruction",
                        JSONObject().put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
                    )
                    put("contents", contents)
                    put(
                        "generationConfig",
                        JSONObject()
                            .put("temperature", 0.7)
                            .put("maxOutputTokens", 1024)
                    )
                }

                val request = Request.Builder()
                    .url("$ENDPOINT?key=$apiKey")
                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Gemini error ${response.code}: ${response.body?.string()}")
                        return@withContext FALLBACK
                    }
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val text = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    text.trim().ifBlank { FALLBACK }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini call failed", e)
                FALLBACK
            }
        }
}
