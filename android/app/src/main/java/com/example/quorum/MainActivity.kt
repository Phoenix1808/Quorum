package com.example.quorum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quorum.ui.detail.ProposalDetailScreen
import com.example.quorum.ui.feed.FeedScreen
import com.example.quorum.ui.theme.QuorumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuorumTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "feed",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Route 1: feed
                        composable("feed") {
                            FeedScreen(
                                onProposalClick = { id ->
                                    navController.navigate("detail/$id")
                                }
                            )
                        }
                        // Route 2: detail (id ke saath)
                        composable("detail/{proposalId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("proposalId") ?: ""
                            ProposalDetailScreen(proposalId = id)
                        }
                    }
                }
            }
        }
    }
}
