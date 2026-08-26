package com.example.quorum.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import com.example.quorum.data.remote.castVote
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import com.example.quorum.data.model.Proposal
import com.example.quorum.ui.components.formatDeadline
import com.example.quorum.data.remote.VoteManager
import com.example.quorum.data.remote.WalletSession
import com.example.quorum.data.remote.VoteStatus

// TODO: ProposalDetailScreen — full proposal (body, choices, results)

@Composable
fun ProposalDetailScreen(
    proposalId: String,
    modifier : Modifier = Modifier,
    viewModel: DetailViewModel= viewModel()
) {
    LaunchedEffect(proposalId) {
        viewModel.loadProposal(proposalId)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Wallet ya proposal badle to pehle se pada vote dobara check karo
    val walletAddress by WalletSession.address.collectAsStateWithLifecycle()
    LaunchedEffect(proposalId, walletAddress) {
        viewModel.loadMyVote(walletAddress, proposalId)
    }
    val myVote by viewModel.myVote.collectAsStateWithLifecycle()

    // Vote submit hone ke baad indicator refresh karo. Snapshot ko index karne me
    // ek pal lagta hai, isliye turant na dikhe to screen dobara kholne par dikhega.
    val voteStatusForRefresh by VoteManager.status.collectAsState()
    LaunchedEffect(voteStatusForRefresh) {
        if (voteStatusForRefresh is VoteStatus.Success) {
            viewModel.loadMyVote(walletAddress, proposalId)
        }
    }

    Box(modifier = modifier.fillMaxSize()){
        when (val s = state){
            is DetailUiState.Loading ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is DetailUiState.Error ->
                Text(s.message, Modifier.align(Alignment.Center))

            is DetailUiState.Success ->
                ProposalDetailContent(s.proposal, myVote)
        }
    }
}

@Composable
private fun ProposalDetailContent(proposal: Proposal, myVote: Int?){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ){
        Text(proposal.dao.name, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            proposal.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${proposal.state.uppercase()} · ${formatDeadline(proposal.end)}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(Modifier.height(20.dp))

        Text("Results",style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val total = proposal.scoresTotal
        proposal.choices.forEachIndexed { index, choice ->
            val score = proposal.scores.getOrNull(index) ?: 0.0
            val  pct = if  (total>0) (score/total * 100).toInt() else 0
            Text("$choice -- $pct%",style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(progress = {pct/100f }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(12.dp))

        if(proposal.state == "active"){
            Spacer(Modifier.height(20.dp))
            Text("Cast Your Vote",style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            val walletAddress by WalletSession.address.collectAsStateWithLifecycle()
            if(walletAddress == null){
                Text("Connect your wallet to vote", style = MaterialTheme.typography.bodyMedium)
            } else{
                // Status buttons se PEHLE padho — ek vote chalu hone par baaki taps
                // block karne hain. Do sign request ek saath = data overwrite = fail.
                val voteStatus by VoteManager.status.collectAsState()
                val voteInFlight = voteStatus is VoteStatus.Submitting

                // Pehle se vote kiya hua hai to bata do -- ye Snapshot se aaya sach hai.
                //
                // Buttons phir bhi enabled rehte hain, par app ye NAHI kehta ki vote badla
                // ja sakta hai. Snapshot ek address ka ek hi record rakhta hai, lekin dusra
                // vote replace hota hai ya reject -- ye humne verify nahi kiya. Bina verify
                // kiye vaada karne se accha hai chup rehna: allowed hua to user try kar lega,
                // nahi hua to asli error message dikh jayega.
                if (myVote != null) {
                    val label = proposal.choices.getOrNull(myVote - 1) ?: "choice $myVote"
                    Text("You voted: $label", style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }

                proposal.choices.forEachIndexed { index,choice->
                    Button(
                        onClick = {
                            VoteManager.resetStatus()
                            castVote(
                                space = proposal.dao.id,
                                proposalId=proposal.id,
                                choice = index + 1
                            )
                        },
                        enabled = !voteInFlight,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(choice)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                when (val vs = voteStatus){
                    is VoteStatus.Idle -> {}
                    is VoteStatus.Submitting -> Text("Submitting Vote")
                    is VoteStatus.Success -> Text("Vote submitted - id ${vs.voteId.take(10)}...")
                    is VoteStatus.Error -> Text("${vs.message}")
                }
            }
        }

        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(proposal.body, style = MaterialTheme.typography.bodyMedium)
    }
}