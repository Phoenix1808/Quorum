package com.example.quorum.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.data.model.VoteHistory
import com.reown.appkit.client.AppKit
import org.web3j.crypto.Keys
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    // connected wallet ka address (checksummed — Snapshot voter checksummed store karta)
    val address = AppKit.getAccount()?.address?.let { Keys.toChecksumAddress(it) }

    // address milte hi (ya badalte hi) votes load karo
    LaunchedEffect(address) {
        if (address != null) viewModel.loadVotes(address)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // wallet connect hi nahi → votes ho hi nahi sakte
            address == null -> Text(
                "Connect your wallet to see your votes",
                Modifier.align(Alignment.Center)
            )
            else -> when (val s = state) {
                is HistoryUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is HistoryUiState.Empty -> Text("No votes yet", Modifier.align(Alignment.Center))
                is HistoryUiState.Error -> Text(s.message, Modifier.align(Alignment.Center))
                is HistoryUiState.Success -> VotesList(s.votes)
            }
        }
    }
}

// LazyColumn = list jo scroll pe hi items banata (memory-efficient, jaise feed)
@Composable
private fun VotesList(votes: List<VoteHistory>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // header + stat (ek baar, list ke upar)
        item {
            Text("Your Votes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${votes.size} votes cast", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(16.dp))
        }
        // har vote ke liye ek row
        items(votes) { vote ->
            VoteRow(vote)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VoteRow(vote: VoteHistory) {
    Column {
        Text(vote.proposalTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text("${vote.spaceName} · ${vote.state} · ${formatDate(vote.created)}",
            style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text("Voted: ${vote.choiceLabel}", style = MaterialTheme.typography.bodyMedium)
    }
}

// unix seconds → "07 Jul 2026"
private fun formatDate(unixSeconds: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(unixSeconds * 1000))
