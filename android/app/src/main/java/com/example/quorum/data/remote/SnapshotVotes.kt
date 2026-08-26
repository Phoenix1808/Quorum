package com.example.quorum.data.remote

import com.example.quorum.data.model.VoteHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// Ek address ka ek PROPOSAL par vote. null = abhi tak vote nahi kiya.
//
// Snapshot har address ka ek hi vote record rakhta hai -- dobara vote karo to purana
// replace ho jata hai, naya record nahi banta. (120 votes wale proposal par 120 unique
// voters, ek bhi duplicate nahi.) Isliye first:1 kaafi hai.
suspend fun fetchUserVoteOn(address: String, proposalId: String): Int? = withContext(Dispatchers.IO){
    val query = """
    query{
        votes(first:1, where:{voter: "$address", proposal: "$proposalId"}){
            choice
        }
    }
    """.trimIndent()

    val payload = JSONObject().put("query",query).toString()
    val body = payload.toRequestBody("application/json".toMediaType())
    val req = Request.Builder().url("https://hub.snapshot.org/graphql").post(body).build()

    OkHttpClient().newCall(req).execute().use { resp ->
        val text = resp.body?.string() ?: return@withContext null
        val arr = JSONObject(text).optJSONObject("data")?.optJSONArray("votes")
        if (arr == null || arr.length() == 0) return@withContext null

        // basic / single-choice me choice ek Int hota hai (1-based index).
        // approval/ranked/weighted me array ya object aata hai -- unhe hum abhi
        // support nahi karte (castVote bhi uint32 hi bhejta hai), to null.
        arr.getJSONObject(0).get("choice") as? Int
    }
}

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
