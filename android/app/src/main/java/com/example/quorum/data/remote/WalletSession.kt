package com.example.quorum.data.remote

import com.reown.appkit.client.AppKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.web3j.crypto.Keys

// Ek hi jagah jo jaanti hai ki abhi kaun sa wallet address connected hai.
//
// Pehle har screen khud AppKit.getAccount() call karti thi. Dikkat ye thi ki woh ek
// SAADHA function call hai -- StateFlow nahi. Compose ko pata hi nahi chalta tha ki
// value badal sakti hai, isliye connect/disconnect par UI update hota hi nahi tha
// (History tab "Connect your wallet" pe atka rehta tha, wallet connected hone ke baad bhi).
//
// Aur DiscoveryViewModel to koshish bhi nahi karta tha -- usme "demo-user" hardcoded tha,
// isliye sabke follows Redis me ek hi bucket me ja rahe the.
//
// VoteManager wala hi pattern hai: andar mutable, bahar read-only StateFlow.
object WalletSession {

    private val _address = MutableStateFlow<String?>(null)

    // null = wallet connected nahi. Warna EIP-55 checksummed address.
    val address: StateFlow<String?> = _address.asStateFlow()

    // AppKit se sach uthao. Idempotent hai -- jitni baar chaho call karo.
    // Checksummed isliye rakhte hain ki Snapshot lowercase address reject karta hai
    // ("wrong envelope format"), aur voter field bhi checksummed hi store hota hai.
    fun refresh() {
        _address.value = try {
            AppKit.getAccount()?.address?.let { Keys.toChecksumAddress(it) }
        } catch (e: Exception) {
            // AppKit abhi initialize nahi hua / session load ho raha hai
            null
        }
    }

    fun clear() {
        _address.value = null
    }
}
