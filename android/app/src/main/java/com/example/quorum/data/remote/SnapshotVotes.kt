package com.example.quorum.data.remote

import com.example.quorum.data.model.VoteHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

suspend fun fetchUserVotes(address:String): List<VoteHistory> = withContext(Dispatchers.IO){
    val query = """
    query{
        votes(first:50,where:{voter: "$address"},orderBy: "created",orderDirection:desc){
            choice 
            created
            proposal {title choices state space {name}}
        }
    }
    """.trimIndent()

    val payload = JSONObject().put("query",query).toString()
    val body = payload.toRequestBody("application/json".toMediaType())
    val req = Request.Builder().url("https://hub.snapshot.org/graphql").post(body).build()

    OkHttpClient().newCall(req).execute().use {resp ->
    val text = resp.body?.string() ?: return@withContext emptyList()
    
    val votesArr = JSONObject(text)
    .optJSONObject("data")?.optJSONArray("votes") ?: return@withContext emptyList()

    val list = mutableListOf<VoteHistory>()
    for(i in 0 until votesArr.length()){
        val v = votesArr.getJSONObject(i)
        val p = v.getJSONObject("proposal")
        val choices = p.getJSONArray("choices")

        val choiceLabel = when(val c = v.get("choice")){
            is Int -> choices.optString(c-1,"Choice $c")
            else -> c.toString()
        }

        list.add(
            VoteHistory(
                proposalTitle = p.getString("title"),
                choiceLabel = choiceLabel,
                spaceName = p.getJSONObject("space").getString("name"),
                state = p.getString("state"),
                created = v.getLong("created")
            )
        )
    }
    list
    }
}
