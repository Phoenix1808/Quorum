package com.example.quorum.data.remote


import com.example.quorum.data.model.DaosResponse
import com.example.quorum.data.model.FollowRequest
import com.example.quorum.data.model.FollowResponse
import com.example.quorum.data.model.ProposalDetailResponse
import com.example.quorum.data.model.ProposalsResponse
import com.example.quorum.data.model.RegisterRequest
import com.example.quorum.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// TODO: Retrofit interface — QuorumApi (getProposals, getProposal, getDaos, follow...)
interface QuorumApi{

    @GET("api/proposals")
    suspend fun getProposals(
        @Query("spaces") spaces:String? = null,
        @Query("state") state:String?= null,
        @Query("page") page:Int = 1
    ) : ProposalsResponse

    @GET("api/proposals/{id}")
    suspend fun getProposal(
        @Path("id") id:String
    ): ProposalDetailResponse

    @GET("api/daos")
    suspend fun getDaos(): DaosResponse

    @POST("api/users")
    suspend fun register(
        @Body body: RegisterRequest
    ): RegisterResponse

    @POST("api/users/{address}/follow")
    suspend fun follow(
        @Path("address") address:String,
        @Body body: FollowRequest
    ): FollowResponse

    @DELETE("api/users/{address}/follow/{daoId}")
    suspend fun unfollow(
        @Path("address") address:String,
        @Path("daoId") daoId:String
    ) : FollowResponse

    @GET("api/users/{address}/follows")
    suspend fun getFollows(
        @Path("address") address:String
    ):FollowResponse
}