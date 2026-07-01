package com.example.quorum

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets

class QuorumApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Project ID local.properties se aata hai (BuildConfig ke through) — hardcoded nahi
        val projId = BuildConfig.WALLET_PROJECT_ID

        val appMetaData = Core.Model.AppMetaData(
            name = "Quorum",
            description = "Mobile-First DAO Governance",
            url = "https://quorum-t5uv.onrender.com",
            icons = listOf("https://cdn.stamp.fyi/space/ens.eth"),
            redirect = "quorum://request"
        )

        //relay se base connection
        CoreClient.initialize(
            projectId = projId,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
            onError = { error -> /* relay/core init error */ }
        )

        //connect modal and dapp layer
        AppKit.initialize(
            init = Modal.Params.Init(CoreClient),
            onSuccess = { /* init success */ },
            onError = { error -> /* init fail */ }
        )

        // Delegate — signature response yahan aata hai. Baaki methods khaali (crash na ho)
        AppKit.setDelegate(object : AppKit.ModalDelegate {
            override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {}
            override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {}
            override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {}
            override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {}
            override fun onSessionExtend(session: Modal.Model.Session) {}
            override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {}

            override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
                android.util.Log.d("QUORUM", "Signature Response -> $response")
            }

            override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {}
            override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {}
            override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {}
            override fun onError(error: Modal.Model.Error) {}
        })

        //Ethereum Mainnet Chain
        AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
    }
}
