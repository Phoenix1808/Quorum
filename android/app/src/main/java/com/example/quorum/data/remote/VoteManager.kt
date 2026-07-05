package com.example.quorum.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.reown.appkit.client.Modal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// Ek vote 4 haal (state) se guzarta hai. UI in me se jo bhi current hai wahi dikhata hai.
// "sealed" ka matlab: bas yehi 4 possibilities hain, iske bahar koi state ho hi nahi sakti.
sealed interface VoteStatus {
    data object Idle : VoteStatus            // kuch nahi ho raha (default)
    data object Submitting : VoteStatus      // sign ho gaya, ab Snapshot ko bhej rahe
    data object Success : VoteStatus         // Snapshot ne vote accept kar liya
    data class Error(val message: String) : VoteStatus   // kuch galat hua + reason
}

// Ye "bridge" hai. Vote 2 alag jagah se guzarta hai:
//   1) hum sign request bhejte hain (Vote.kt se)
//   2) signature BAAD me async wapas aata hai (QuorumApplication ke delegate se)
// Beech me app ka process mar bhi sakta hai (wallet foreground me hota), isliye pending
// vote ko disk (SharedPreferences) pe rakhte hain — sirf RAM me rakhte to gum ho jaata.
object VoteManager {
    private lateinit var prefs: SharedPreferences

    // _status = andar wala (badalne wala) box. status = bahar wala (sirf padhne wala) box.
    // UI sirf "status" dekh sakta, badal nahi sakta — isse control ek hi jagah rehta.
    private val _status = MutableStateFlow<VoteStatus>(VoteStatus.Idle)
    val status: StateFlow<VoteStatus> = _status.asStateFlow()

    // App start pe ek baar (QuorumApplication se) — disk storage taiyaar karta.
    fun init(context: Context) {
        prefs = context.getSharedPreferences("quorum_vote", Context.MODE_PRIVATE)
    }

    // Naya vote start karne se pehle UI purana result (✅/❌) saaf kar sake.
    fun resetStatus() {
        _status.value = VoteStatus.Idle
    }

    // Sign request bhejne se PEHLE call hota (Vote.kt me). Vote ki detail disk pe save,
    // taaki signature wapas aane pe humein pata ho kis vote ka signature tha.
    fun setPendingVote(address: String, voteData: String) {
        prefs.edit()
            .putString("addr", address)
            .putString("data", voteData)
            .apply()
        _status.value = VoteStatus.Submitting   // UI ko batao: kaam chalu ho gaya
    }

    // Wallet ka signature response yahan aata (QuorumApplication delegate se forward hoke).
    fun onSignResponse(response: Modal.Model.SessionRequestResponse) {
        val result = response.result

        // Agar result "JsonRpcResult" nahi hai, matlab signature mila hi nahi —
        // aksar isliye ki user ne wallet me "Reject"/cancel kar diya.
        if (result !is Modal.Model.JsonRpcResponse.JsonRpcResult) {
            _status.value = VoteStatus.Error("Signature rejected")
            return
        }

        val sig = result.result                                   // asli signature (0x...)
        val address = prefs.getString("addr", null) ?: return     // disk se wapas padho
        val voteData = prefs.getString("data", null) ?: return

        // Network kaam hamesha background thread pe (IO) — UI thread block na ho.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = submitVote(address, sig as String, voteData)  // Snapshot ko POST

                // Snapshot ka jawab JSON hota. Error pe {"error":..., "error_description":"..."},
                // success pe {"id":"0x..."}. To "error" key hai ya nahi — usse decide karte.
                val json = try { JSONObject(resp) } catch (e: Exception) { null }
                if (json != null && json.has("error")) {
                    _status.value = VoteStatus.Error(json.optString("error_description", "Vote failed"))
                } else {
                    _status.value = VoteStatus.Success
                }
            } catch (e: Exception) {
                // Internet gaya / server down jaisi dikkat
                _status.value = VoteStatus.Error(e.message ?: "Submit failed")
            }
        }

        // Pending vote ab use ho chuka — disk se hata do (agli baar confusion na ho).
        prefs.edit().remove("addr").remove("data").apply()
    }
}
