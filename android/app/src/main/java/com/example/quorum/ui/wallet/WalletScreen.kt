package com.example.quorum.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.reown.appkit.ui.components.button.AppKitState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.quorum.data.remote.VoteManager
import com.example.quorum.data.remote.VoteStatus
import com.example.quorum.data.remote.castVote
import com.reown.appkit.client.AppKit

@Composable
fun WalletScreen(
    appKitState: AppKitState,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val isConnect by appKitState.isConnected.collectAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =  Arrangement.Center
    ){
        Text("Wallet",style= MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if(isConnect){
            val acc = AppKit.getAccount()
            val address = acc?.address ?: "--"
            Text("Wallet Connected", style = MaterialTheme.typography.titleMedium)

            val shortAdd = if (address.length>10) "${address.take(6)}..${address.takeLast(4)}" //like take 6 chars from beginning and 4 from ending
            else address
            Text(shortAdd,style=MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
              castVote(
                    space = "mxlcolm.eth",
                    proposalId = "0xbb0946295da31956587571aade81bf560370aeb407bc3c43ab1251e613273ef5",
                    choice = 1 //"Wine" (choices: Wine/Beer/Spirits)
                )

            }) {
                Text("Sign Vote (test)")
            }

            // vote ka live status — VoteManager se observe hota, tap ke baad badalta dikhega
            val voteStatus by VoteManager.status.collectAsState()
            Spacer(Modifier.height(8.dp))
            when (val vs = voteStatus) {
                is VoteStatus.Idle -> {}                                          // kuch mat dikhao
                is VoteStatus.Submitting -> Text("Submitting vote…")
                is VoteStatus.Success -> Text("✅ Vote submitted!")
                is VoteStatus.Error -> Text("❌ ${vs.message}")
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = {AppKit.disconnect(onSuccess = {}, onError = {})
            }) {
                Text("Disconnect")
            }
        } else{
            Text("Vote by connecting your Wallet", style= MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onConnectClick){
                Text("Connect Wallet")
            }
        }
    }
}