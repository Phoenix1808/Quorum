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
import org.json.JSONArray
import org.json.JSONObject


private fun voteData(from:String, space:String,proposal:String,choice:Int):String{
    val eip712 = JSONArray()
        .put(JSONObject().put("name","name").put("type","string"))
        .put(JSONObject().put("name","version").put("type","string"))

    val voteType = JSONArray()
        .put(JSONObject().put("name","from").put("type","address"))
        .put(JSONObject().put("name","space").put("type","string"))
        .put(JSONObject().put("name","timestamp").put("type","uint64"))
        .put(JSONObject().put("name", "proposal").put("type", "bytes32"))
        .put(JSONObject().put("name", "choice").put("type", "uint32"))
        .put(JSONObject().put("name", "reason").put("type", "string"))
        .put(JSONObject().put("name", "app").put("type", "string"))
        .put(JSONObject().put("name", "metadata").put("type", "string"))

    val types = JSONObject().put("EIP712Domain",eip712).put("Vote",voteType)
    val domain = JSONObject().put("name","snapshot").put("version","0.1.4")
    val msg = JSONObject()
        .put("from",from)
        .put("space",space)
        .put("timestamp",System.currentTimeMillis()/1000)
        .put("proposal",proposal)
        .put("choice",choice)
        .put("reason","")
        .put("app","quorum")
        .put("metadata","{}")

    return JSONObject()
        .put("types",types)
        .put("domain",domain)
        .put("primaryType","Vote")
        .put("message",msg)
        .toString()

}
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
                val typeData = voteData(
                    from = addr, space = "ens.eth",
                    proposal = "0xe4e1c052b2ea4f640cab27ddec326df6290d8996a9219b60cda4c4d4509f5f9a",
                    choice = 1
                )
                val paramsJson = JSONArray().put(addr).put(typeData).toString()

                val req = Request(
                    method = "eth_signTypedData_v4",
                    params = paramsJson
                )
                AppKit.request(
                    request = req,
                    onSuccess = { _ -> android.util.Log.d("Quorum", "request sent !!") },
                    onError = { error -> android.util.Log.e("Quorum", "Request Error", error) }
                )
            }) {
                Text("Sign Vote (test)")
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