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

// TODO: WalletScreen — connect button + connected state

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
            Text("Wallet Connected", style = MaterialTheme.typography.titleMedium)
        } else{
            Text("Vote by connecting your Wallet", style= MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onConnectClick){
                Text("Connect Wallet")
            }
        }
    }
}