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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import com.example.quorum.data.model.Proposal
import com.example.quorum.ui.components.formatDeadline

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

    Box(modifier = modifier.fillMaxSize()){
        when (val s = state){
            is DetailUiState.Loading ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is DetailUiState.Error ->
                Text(s.message, Modifier.align(Alignment.Center))

            is DetailUiState.Success ->
                ProposalDetailContent(s.proposal)
        }
    }
}

@Composable
private fun ProposalDetailContent(proposal: Proposal){
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

        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(proposal.body, style = MaterialTheme.typography.bodyMedium)
    }
}