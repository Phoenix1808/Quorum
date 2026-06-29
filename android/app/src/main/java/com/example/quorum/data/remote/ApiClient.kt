package com.example.quorum.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: Retrofit client — ApiClient (base URL, OkHttp, GsonConverter)
object ApiClient{
    private const val BASE_URL = "https://quorum-t5uv.onrender.com"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply{
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: QuorumApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(QuorumApi::class.java)
}