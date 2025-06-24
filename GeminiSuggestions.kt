package com.example.a1st

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

import java.io.IOException

object GeminiSuggestions {
    private const val API_KEY = "" // Replace with yours
    private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=$API_KEY"

    fun suggestTasksFromPlan(
        weeklyPlan: String,
        onResult: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        val client = OkHttpClient()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Extract simple todo tasks from this weekly plan:\n$weeklyPlan")
                        })
                    })
                })
            })
        }

        val body = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("❌ Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val resBody = response.body?.string()
                    if (!response.isSuccessful || resBody == null) {
                        onError("❌ HTTP ${response.code} - ${resBody ?: "No response body"}")
                        return
                    }

                    val json = JSONObject(resBody)
                    val candidates = json.optJSONArray("candidates")
                    val content = candidates?.getJSONObject(0)?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.getJSONObject(0)?.optString("text", null)

                    if (text == null) {
                        onError("❌ No text found in response.")
                        return
                    }

                    val tasks = text.split("\n")
                        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                        .filter { it.isNotBlank() }

                    onResult(tasks)
                } catch (e: Exception) {
                    onError("❌ Parsing error: ${e.message}")
                }
            }
        })
    }
}
