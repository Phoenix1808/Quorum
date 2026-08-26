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
    // Success ab khaali nahi — Snapshot ki di hui vote id (receipt) leke aata hai.
    // Yahi ek cheez hai jo sabit karti hai ki vote sach me land hua.
    data class Success(val voteId: String) : VoteStatus
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

    // Pending vote itni der se pada ho to maan lo wallet ne kabhi jawab diya hi nahi.
    // Iske bina UI hamesha ke liye "Submitting" pe atak jata.
    private const val PENDING_TTL_MS = 5 * 60 * 1000L

    // App start pe ek baar (QuorumApplication se) — disk storage taiyaar karta.
    fun init(context: Context) {
        prefs = context.getSharedPreferences("quorum_vote", Context.MODE_PRIVATE)

        // Process death ke baad status BHI wapas laana zaroori hai, sirf data nahi.
        // Warna naya process status Idle dikhata, user ko lagta kuch hua hi nahi, woh
        // dobara tap karta — aur dusra tap pehle wale ka data overwrite kar deta. Fir
        // pehle wala signature dusre wale data ke saath jata -> "signature validation failed".
        val hasPending = prefs.getString("data", null) != null
        val pendingAt = prefs.getLong("at", 0L)
        when {
            !hasPending -> Unit
            System.currentTimeMillis() - pendingAt > PENDING_TTL_MS -> clearPending()
            else -> _status.value = VoteStatus.Submitting
        }
    }

    // Pending vote hatane ki ek hi jagah — pehle ye do jagah alag-alag hota tha aur
    // error wale raste me hota hi nahi tha.
    private fun clearPending() {
        prefs.edit().remove("addr").remove("data").remove("at").apply()
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
            .putLong("at", System.currentTimeMillis())   // TTL ke liye
            .apply()
        _status.value = VoteStatus.Submitting   // UI ko batao: kaam chalu ho gaya
    }

    // Wallet ka signature response yahan aata (QuorumApplication delegate se forward hoke).
    fun onSignResponse(response: Modal.Model.SessionRequestResponse) {
        val result = response.result

        // Agar result "JsonRpcResult" nahi hai, matlab signature mila hi nahi —
        // aksar isliye ki user ne wallet me "Reject"/cancel kar diya.
        if (result !is Modal.Model.JsonRpcResponse.JsonRpcResult) {
            // Pehle yahan se seedha return hota tha — pending vote disk pe pada reh jata
            // aur agli baar kisi DUSRE signature ke saath jud jata. Isliye pehle saaf karo.
            clearPending()
            _status.value = VoteStatus.Error("Signature rejected ya wallet ne mana kiya")
            return
        }

        val sig = result.result                                   // asli signature (0x...)
        val address = prefs.getString("addr", null) ?: return     // disk se wapas padho
        val voteData = prefs.getString("data", null) ?: return

        // Network kaam hamesha background thread pe (IO) — UI thread block na ho.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = submitVote(address, sig as String, voteData)  // Snapshot ko POST

                // Snapshot success pe {"id":"0x...","ipfs":"..."} deta, error pe
                // {"error":...,"error_description":"..."}. Pehle yahan parse fail hone par
                // chup-chaap Success maan liya jaata tha — yaani gateway error ya HTML page
                // pe bhi UI "vote ho gaya" dikha deta. Ab sab kuch explicit hai.
                val json = try { JSONObject(result.body) } catch (e: Exception) { null }
                val voteId = json?.optString("id").orEmpty()

                _status.value = when {
                    // Snapshot ne saaf-saaf error bola
                    json != null && json.has("error") ->
                        VoteStatus.Error(json.optString("error_description", "Vote failed"))

                    // HTTP hi fail — body me chahe kuch bhi ho
                    result.code !in 200..299 ->
                        VoteStatus.Error("Snapshot HTTP ${result.code}: ${result.body.take(120)}")

                    // JSON nahi mila, ya id nahi mili → vote land hone ka koi proof nahi
                    voteId.isEmpty() ->
                        VoteStatus.Error("No vote id in response: ${result.body.take(120)}")

                    // Sirf yahan Success — receipt ke saath
                    else -> VoteStatus.Success(voteId)
                }
            } catch (e: Exception) {
                // Internet gaya / server down jaisi dikkat
                _status.value = VoteStatus.Error(e.message ?: "Submit failed")
            }
        }

        // Pending vote ab use ho chuka — disk se hata do (agli baar confusion na ho).
        clearPending()
    }
}
