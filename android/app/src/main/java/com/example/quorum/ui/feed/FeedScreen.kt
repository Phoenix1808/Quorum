package com.example.quorum.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.ui.components.ProposalCard

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onProposalClick: (String) -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    // local UI state kaunsa filter selected hai (null = All)
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf(
        "All" to null,
        "Active" to "active",
        "Closed" to "closed"
    )

    Column(modifier = modifier.fillMaxSize()) {

       //filter chips
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (label, value) ->
                FilterChip(
                    selected = selectedFilter == value,
                    onClick = {
                        selectedFilter = value
                        viewModel.loadProposals(state = value)
                    },
                    label = { Text(label) }
                )
            }
        }

       //content is within the column
        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                is FeedUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is FeedUiState.Success ->
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(s.proposals) { proposal ->
                            ProposalCard(
                                proposal = proposal,
                                onClick = { onProposalClick(proposal.id) }
                            )
                        }
                    }

                is FeedUiState.Error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(s.message)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadProposals(selectedFilter) }) {
                            Text("Retry")
                        }
                    }
            }
        }
    }
}
