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
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.models.request.Request


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
                val addr = AppKit.getAccount()?.address ?: return@Button
                val msg = "0x48656c6c6f2051756f72756d"
                val req = Request(
                    method = "personal_sign",
                    params = "[\"$msg\", \"$addr\"]"
                )
                AppKit.request(
                    request = req,
                    onSuccess = { _ -> android.util.Log.d("Quorum", "request sent !!") },
                    onError = { error -> android.util.Log.e("Quorum", "Request Error", error) }
                )
            }) {
                Text("Sign Text Message")
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