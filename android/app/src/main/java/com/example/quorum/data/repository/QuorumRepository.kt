package com.example.quorum.data.repository

import com.example.quorum.data.model.DaoItem
import com.example.quorum.data.model.FollowRequest
import com.example.quorum.data.model.RegisterRequest
import com.example.quorum.data.model.Proposal
import com.example.quorum.data.remote.ApiClient

class QuorumRepository {

    suspend fun getProposals(
        spaces: String? = null,
        state: String? = null,
        page: Int = 1
    ): List<Proposal> {
        return ApiClient.api.getProposals(spaces, state, page).proposals
    }

    suspend fun getProposal(id: String): Proposal {
        return ApiClient.api.getProposal(id).proposal
    }

    // Backend address+fcmToken store karta hai aur address ko global "users" set me
    // daalta hai -- deadline cron usi set par chalta hai.
    suspend fun register(address: String, fcmToken: String){
        ApiClient.api.register(RegisterRequest(address, fcmToken))
    }

    suspend fun getDaos(): List<DaoItem>{
        return ApiClient.api.getDaos().daos
    }

    suspend fun getFollows(address:String): List<String>{
        return ApiClient.api.getFollows(address).follows
    }

    suspend fun follow(address:String, daoId: String): List<String>{
        return ApiClient.api.follow(address, FollowRequest(daoId)).follows
    }

    suspend fun unfollow(address:String, daoId:String): List<String>{
        return ApiClient.api.unfollow(address,daoId).follows
    }
}
