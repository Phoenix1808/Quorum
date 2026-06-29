package com.example.quorum.data.repository

import com.example.quorum.data.model.Proposal
import com.example.quorum.data.remote.ApiClient

// TODO: Repository — ApiClient ko wrap karke ViewModels ko clean data deta hai
class QuorumRepository{
    suspend fun getProposals(
        spaces: String? = null,
        state: String? = null,
        page:Int = 1
    ) : List<Proposal>{
        return ApiClient.api.getProposals(spaces,state,page).proposals
    }
}