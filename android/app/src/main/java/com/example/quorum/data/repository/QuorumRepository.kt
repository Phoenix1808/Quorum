package com.example.quorum.data.repository

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
}
