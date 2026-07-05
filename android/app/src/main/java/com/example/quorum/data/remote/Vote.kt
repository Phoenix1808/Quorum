package com.example.quorum.data.remote

import android.app.VoiceInteractor
import com.reown.appkit.client.AppKit
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.crypto.Keys
import com.reown.appkit.client.models.request.Request

import kotlin.concurrent.timer

private fun VoteData(from:String,space:String,proposal:String,choice:Int):String {
    val eip712 = JSONArray()
        .put(JSONObject().put("name","name").put("type","string"))
        .put(JSONObject().put("name","version").put("type","string"))

    val voteType = JSONArray()
        .put(JSONObject().put("name","from").put("type","address"))
        .put(JSONObject().put("name","space").put("type","string"))
        .put(JSONObject().put("name","timestamp").put("type","uint64"))
        .put(JSONObject().put("name","proposal").put("type","bytes32"))
        .put(JSONObject().put("name","choice").put("type","uint32"))
        .put(JSONObject().put("name","reason").put("type","string"))
        .put(JSONObject().put("name","app").put("type","string"))
        .put(JSONObject().put("name","metadata").put("type","string"))

    val types = JSONObject().put("EIP712Domain",eip712).put("Vote",voteType)
    val domain = JSONObject().put("name","snapshot").put("version","0.1.4")
    val msg = JSONObject()
        .put("from",from)
        .put("space",space)
        .put("choice",choice)
        .put("proposal",proposal)
        .put("timestamp",System.currentTimeMillis()/1000)
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

fun castVote(space: String,proposalId:String,choice:Int){
    val addr = Keys.toChecksumAddress(AppKit.getAccount()?.address ?: return)
    val typeData = VoteData(from = addr,space= space,proposal = proposalId,choice= choice)
    val paramsJson = JSONArray().put(addr).put(typeData).toString()

    VoteManager.setPendingVote(addr,typeData)

    val req = Request(method = "eth_signTypedData_v4",params = paramsJson)
    AppKit.request(
        request = req,
        onSuccess = {_ -> android.util.Log.d("Quorum","vote request sent")},
        onError = {e -> android.util.Log.e("Quorum","vote request error",e)}
    )
}