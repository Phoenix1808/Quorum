package com.example.quorum.ui.discovery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.ui.feed.FeedViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import com.example.quorum.data.model.DaoItem
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

// TODO: DiscoveryScreen — DAO cards + follow button

//ipfs: //converts url to http gateway
fun ipfstoHttp(url: String?): String?{
    if (url == null)
        return null
    return if (url.startsWith("ipfs://"))
        "https://ipfs.io/ipfs/" + url.removePrefix("ipfs://")
    else url
}

@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()){
        when (val s = state) {
            is DiscoveryUiState.Loading ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is DiscoveryUiState.Error ->
                Text(s.message, Modifier.align(Alignment.Center))

            is DiscoveryUiState.Success ->
                LazyColumn(Modifier.fillMaxSize()) {
                    items(s.daos) { dao ->
                        DaoCard(
                            dao = dao,
                            isFollowed = dao.id in s.followed,
                            onToggleFollow = { viewModel.toggleFollow(dao.id) })
                    }
                }
        }
    }
}

@Composable
private fun DaoCard(dao: DaoItem, isFollowed: Boolean, onToggleFollow:() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://cdn.stamp.fyi/space/${dao.space}?s=96",
                contentDescription = dao.name,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dao.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${dao.followersCount} followers - ${dao.proposalsCount} proposals",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button(onClick = onToggleFollow) {
                Text(if (isFollowed) "Following" else "Follow")
            }
        }
    }
}
