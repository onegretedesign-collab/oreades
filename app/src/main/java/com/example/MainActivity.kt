package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AiGuideScreen
import com.example.ui.DashboardScreen
import com.example.ui.MapGisScreen
import com.example.ui.RestoreScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OreadesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: OreadesViewModel = viewModel()
                val currentTab by viewModel.currentTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = "Oréades",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { viewModel.setTab(0) },
                                label = { Text("Início") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                        contentDescription = "Início"
                                    )
                                },
                                modifier = Modifier.testTag("nav_item_home")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { viewModel.setTab(1) },
                                label = { Text("Mapa SIG") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 1) Icons.Default.Map else Icons.Outlined.Map,
                                        contentDescription = "Mapa SIG"
                                    )
                                },
                                modifier = Modifier.testTag("nav_item_map")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { viewModel.setTab(2) },
                                label = { Text("Restaurar") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 2) Icons.Default.Park else Icons.Outlined.Park,
                                        contentDescription = "Restaurar"
                                    )
                                },
                                modifier = Modifier.testTag("nav_item_restore")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { viewModel.setTab(3) },
                                label = { Text("Eco-Guia") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == 3) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                                        contentDescription = "Eco-Guia"
                                    )
                                },
                                modifier = Modifier.testTag("nav_item_guide")
                            )
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (currentTab) {
                        0 -> DashboardScreen(viewModel = viewModel, modifier = modifier)
                        1 -> MapGisScreen(viewModel = viewModel, modifier = modifier)
                        2 -> RestoreScreen(viewModel = viewModel, modifier = modifier)
                        3 -> AiGuideScreen(viewModel = viewModel, modifier = modifier)
                    }
                }
            }
        }
    }
}

