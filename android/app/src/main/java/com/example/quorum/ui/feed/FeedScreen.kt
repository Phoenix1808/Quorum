package com.example.quorum.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.ui.components.ProposalCard

// TODO: FeedScreen — LazyColumn of proposals + filter chips
@Composable
fun FeedScreen(
 modifier: Modifier = Modifier,
 viewModel: FeedViewModel= viewModel()
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier= modifier.fillMaxSize()){
        when (val s = state){
            is FeedUiState.Loading->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is FeedUiState.Success->
                LazyColumn(Modifier.fillMaxSize()){
                    items(s.proposals){ proposal ->
                        ProposalCard(proposal)
                    }
                }
            is FeedUiState.Error->
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(s.message)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick ={viewModel.loadProposals()}) {Text("Retry")}
                }
        }
    }

}