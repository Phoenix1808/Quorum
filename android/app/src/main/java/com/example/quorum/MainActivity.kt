package com.example.quorum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quorum.ui.detail.ProposalDetailScreen
import com.example.quorum.ui.discovery.DiscoveryScreen
import com.example.quorum.ui.feed.FeedScreen
import com.example.quorum.ui.theme.QuorumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuorumTheme {
                QuorumApp()
            }
        }
    }
}

@Composable
fun QuorumApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { QuorumBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "feed",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("feed") {
                FeedScreen(
                    onProposalClick = { id -> navController.navigate("detail/$id") }
                )
            }
            composable("detail/{proposalId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("proposalId") ?: ""
                ProposalDetailScreen(proposalId = id)
            }
            composable("discovery") {
                DiscoveryScreen()
            }
        }
    }
}

@Composable
fun QuorumBottomBar(navController: NavController) {
    // route, label, icon (emoji — material-icons dependency se bacha)
    val items = listOf(
        Triple("feed", "Feed", "🏠"),
        Triple("discovery", "Discover", "🔍")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(icon) },
                label = { Text(label) }
            )
        }
    }
}
