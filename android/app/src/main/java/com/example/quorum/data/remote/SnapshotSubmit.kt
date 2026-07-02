package com.example.quorum.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request

suspend fun submitVote(address:String, sig: String, voteData: String): String = withContext(
    Dispatchers.IO) {

    val dataObj = JSONObject(voteData)
    dataObj.remove("primaryType")

    val env = JSONObject()
        .put("address",address)
        .put("sig",sig)
        .put("data",dataObj)
        .toString()

    android.util.Log.d("Quorum", "ENV: $env")

    val body = env.toRequestBody("application/json".toMediaType())
    val requ = Request.Builder()
        .url("https://seq.snapshot.org/")
        .post(body)
        .build()

    OkHttpClient().newCall(requ).execute().use{ resp ->
        val respBody= resp.body?.string() ?: ""
        android.util.Log.d("Quorum","Snapshot response (${resp.code}): $respBody")
        respBody
    }
}