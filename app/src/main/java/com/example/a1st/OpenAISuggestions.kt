package com.example.a1st

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object OpenAISuggestions {
    private const val API_KEY = "" // 🔐 Replace this with a secure, working key
    private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"

    fun suggestTasksFromPlan(
        weeklyPlan: String,
        onResult: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        val client = OkHttpClient()

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a helpful assistant that extracts actionable to-do tasks from a weekly plan.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", weeklyPlan)
                })
            })
            put("temperature", 0.7)
        }

        val mediaType = "application/json".toMediaType()
        val body = requestBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("❌ Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (resBody != null) {
                    try {
                        val json = JSONObject(resBody)

                        if (json.has("choices")) {
                            val reply = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            val tasks = reply.split("\n")
                                .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                                .filter { it.isNotBlank() }

                            onResult(tasks)
                        } else if (json.has("error")) {
                            val errMsg = json.getJSONObject("error").getString("message")
                            onError("❌ API Error: $errMsg")
                        } else {
                            onError("❌ Unexpected response: $resBody")
                        }

                    } catch (e: Exception) {
                        onError("❌ Parsing error: ${e.message}")
                    }
                } else {
                    onError("❌ Empty response")
                }
            }
        })
    }
}
