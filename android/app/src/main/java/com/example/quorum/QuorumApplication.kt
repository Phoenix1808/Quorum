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

        //Ethereum Mainnet Chain
        AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
    }
}