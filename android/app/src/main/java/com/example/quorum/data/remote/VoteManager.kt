package com.example.quorum.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.reown.appkit.client.Modal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object VoteManager {
    private lateinit var prefs: SharedPreferences

    // app start pe ek baar call hoga QuorumApplication se
    fun init(context: Context) {
        prefs = context.getSharedPreferences("quorum_vote", Context.MODE_PRIVATE)
    }

    fun setPendingVote(address: String, voteData: String) {
        prefs.edit()
            .putString("addr", address)
            .putString("data", voteData)
            .apply()            // disk pe save
    }

    fun onSignResponse(response: Modal.Model.SessionRequestResponse) {
        val result = response.result
        if (result !is Modal.Model.JsonRpcResponse.JsonRpcResult) return
        val sig = result.result
        val address = prefs.getString("addr", null) ?: return    // disk se padho
        val voteData = prefs.getString("data", null) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                submitVote(address, sig as String, voteData)
            } catch (e: Exception) {
                android.util.Log.e("Quorum", "submitVote crashed", e)
            }
        }
        prefs.edit().remove("addr").remove("data").apply()        // clear
    }
}
