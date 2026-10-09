package com.example.quorum.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestModelTest {

    @Test
    fun registerRequest_serializesAddressAndFcmToken() {
        val request = RegisterRequest(
            address = "0x123abc",
            fcmToken = "test-fcm-token"
        )

        val gson = Gson()
        val json = gson.toJson(request)
        val decoded = gson.fromJson(json, RegisterRequest::class.java)

        assertEquals("0x123abc", decoded.address)
        assertEquals("test-fcm-token", decoded.fcmToken)
    }
}
