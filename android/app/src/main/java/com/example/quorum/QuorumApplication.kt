package com.example.quorum

import android.app.Application
import com.example.quorum.data.remote.PushRegistration
import com.example.quorum.data.remote.VoteManager
import com.example.quorum.data.remote.WalletSession
import com.google.firebase.messaging.FirebaseMessaging
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

        VoteManager.init(this)
        PushRegistration.init(this)

        // onNewToken sirf token BADALNE par aata hai. Normal launch pe woh call hi nahi
        // hota, isliye maujooda token yahan se maangna padta hai -- warna pehli install
        // ke baad kabhi register hi nahi hote.
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                PushRegistration.onToken(task.result)
            } else {
                android.util.Log.e("Quorum", "FCM token nahi mila: ${task.exception?.message}")
            }
        }

        // Delegate — signature response yahan aata hai. Baaki methods khaali (crash na ho)
        AppKit.setDelegate(object : AppKit.ModalDelegate {
            // Ye methods pehle khaali the. Wallet connect/disconnect ka pata sabse
            // pehle yahin chalta hai, isliye WalletSession yahan se update hota hai.
            override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
                WalletSession.refresh()
            }
            override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {}
            override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {}
            override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {}
            override fun onSessionExtend(session: Modal.Model.Session) {}
            override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
                WalletSession.clear()
            }

            override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
                android.util.Log.d("QUORUM", "Signature Response -> $response")
                VoteManager.onSignResponse(response)
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
