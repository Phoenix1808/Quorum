package com.example.quorum.ui.components

import android.R.attr.onClick
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quorum.data.model.Proposal


fun formatDeadline(end: Long): String {
    val now = System.currentTimeMillis() / 1000
    val diff = end - now
    return when {
        diff <= 0      -> "Ended"
        diff < 3600    -> "${diff / 60}m left"
        diff < 86400   -> "${diff / 3600}h left"
        else           -> "${diff / 86400}d left"
    }
}

@Composable
fun ProposalCard(proposal: Proposal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable{onClick()}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(proposal.dao.name, style = MaterialTheme.typography.labelMedium)
                Text(proposal.state.uppercase(), style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))


            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))


            val total = proposal.scoresTotal
            val topIndex = proposal.scores.indices.maxByOrNull { proposal.scores[it] } ?: 0
            val topChoice = proposal.choices.getOrNull(topIndex) ?: "-"
            val topPct = if (total > 0) (proposal.scores[topIndex] / total * 100).toInt() else 0

            if (total > 0) {
                Text("$topChoice · $topPct%", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { topPct / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("No votes yet", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))


            Text(formatDeadline(proposal.end), style = MaterialTheme.typography.labelSmall)
        }
    }
}
