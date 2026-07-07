package com.example.quorum.data.model

// TODO: data classes — Proposal, Dao, DaoItem, ProposalsResponse, etc.
data class Dao(
    val id : String, val name : String,
)

data class Proposal(
    val id : String , val title: String, val body: String, val choices: List<String>,
    val scores: List<Double>, val scoresTotal: Double, val start: Long,
    val end: Long, val state:String, val dao: Dao,
    val quorum: Double? = null,
    val author: String? = null,
    val created: Long? = null,
    val snapshot: Long? = null
)

//Response Wrappers

data class ProposalsResponse(
    val page: Int, val count: Int, val proposals: List<Proposal>
)


data class ProposalDetailResponse(
  val proposal : Proposal
)
// /api/daos
data class DaoItem(
    val id : String, val name: String, val space: String,
    val about : String, val avatar: String?, val followersCount: Int,
    val proposalsCount:Int
)

data class DaosResponse(
    val count :Int, val daos:List<DaoItem>
)

//users or followers

data class User(
    val address:String,
    val fcmToken: String
)

data class RegisterRequest(
    val address: String,
    val fcmToken: String
)

data class RegisterResponse(
    val user : User
)

data class FollowRequest(
    val daoId:String
)

data class FollowResponse(
    val follows: List<String>
)

data class VoteHistory(
    val proposalTitle: String,
    val choiceLabel: String,
    val spaceName: String,
    val state : String,
    val created : Long,
)