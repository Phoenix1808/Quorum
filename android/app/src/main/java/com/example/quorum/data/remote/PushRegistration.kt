package com.example.quorum.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.quorum.data.repository.QuorumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Backend ko push bhejne ke liye DO cheezein chahiye: wallet address AUR fcm token.
//   POST /api/users  ->  { address, fcmToken }
//
// Dikkat ye hai ki dono kabhi bhi, kisi bhi order me aa sakte hain -- token app start
// pe aa sakta hai jab wallet connected na ho, ya wallet pehle connect ho jaye aur token
// baad me refresh ho. Isliye yahan dono ko yaad rakhte hain aur jab bhi dono maujood
// hon, register kar dete hain.
object PushRegistration {

    private lateinit var prefs: SharedPreferences
    private val repo = QuorumRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        prefs = context.getSharedPreferences("quorum_push", Context.MODE_PRIVATE)

        // Wallet connect/disconnect/switch par dobara koshish karo.
        // collect current value bhi turant deta hai, to app start pe ek try ho jata hai.
        scope.launch {
            WalletSession.address.collect { tryRegister() }
        }
    }

    // Token disk pe rakhte hain kyunki wallet baad me connect ho sakta hai --
    // us waqt token dobara nahi milega (onNewToken sirf refresh par aata hai).
    fun onToken(token: String) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString("token", token).apply()
        tryRegister()
    }

    private fun tryRegister() {
        if (!::prefs.isInitialized) return
        val token = prefs.getString("token", null) ?: return
        val address = WalletSession.address.value ?: return

        // Wahi address+token dobara bhejne ka koi matlab nahi (backend hset hai, idempotent
        // hai, par network call bekaar hai). Jo aakhri baar bheja tha wahi hai to skip.
        val signature = "$address|$token"
        if (prefs.getString("registered", null) == signature) return

        scope.launch {
            try {
                repo.register(address, token)
                prefs.edit().putString("registered", signature).apply()
                android.util.Log.d("Quorum", "FCM registered for $address")
            } catch (e: Exception) {
                // Network fail -> agli baar (wallet event ya token refresh) phir try hoga
                android.util.Log.e("Quorum", "FCM register failed: ${e.message}")
            }
        }
    }
}
