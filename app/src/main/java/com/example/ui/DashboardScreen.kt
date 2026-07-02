package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OreadesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: OreadesViewModel,
    modifier: Modifier = Modifier
) {
    val plots by viewModel.plots.collectAsState()
    val threatReports by viewModel.threatReports.collectAsState()
    
    // Calculate total trees planted in the app local database
    val localPlotsCount = plots.size
    val totalLocalCarbon = plots.sumOf { it.carbonOffsetKg }

    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Section - Artistic Flair (rounded-[32px], bg-[#D8E7CB], bottom gradient)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.secondary) // Pale Green (#D8E7CB)
                    .drawBehind {
                        // Drawing topography line rings in transparent green representing geoprocessing and Earth maps
                        val primaryColor = Color(0xFF386B1F)
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.04f),
                            radius = 400f,
                            center = Offset(size.width * 0.95f, size.height * 0.3f)
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.04f),
                            radius = 300f,
                            center = Offset(size.width * 0.95f, size.height * 0.3f)
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.04f),
                            radius = 200f,
                            center = Offset(size.width * 0.95f, size.height * 0.3f)
                        )
                    }
            ) {
                // Bottom dark overlay to make sure text is extremely legible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    // Headline badge "Destaque"
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF386B1F), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DESTAQUE",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Preservando o Coração do Brasil",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Conheça nossas frentes de preservação no bioma Cerrado e ajude-nos a restaurar a vida.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Quick Stats / Impact Row
        item {
            Text(
                text = "Impacto Ecológico Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Static + dynamic combined stats card using Artistic colors
                StatItemCard(
                    modifier = Modifier.weight(1f),
                    title = "Árvores",
                    value = "${(localPlotsCount * 120) + 150} locais",
                    icon = Icons.Default.Forest,
                    iconColor = Color(0xFF386B1F),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // #E3E3D2 (earth-beige)
                    testTag = "stat_trees"
                )
                
                StatItemCard(
                    modifier = Modifier.weight(1f),
                    title = "CO₂ Retido",
                    value = String.format("%.0f kg", totalLocalCarbon + 3800),
                    icon = Icons.Default.Co2,
                    iconColor = Color(0xFF705D00),
                    containerColor = MaterialTheme.colorScheme.tertiary, // #F2E0BD (sandy-gold)
                    testTag = "stat_carbon"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItemCard(
                    modifier = Modifier.weight(1f),
                    title = "Nascentes",
                    value = "${localPlotsCount + 12} protegidas",
                    icon = Icons.Default.WaterDrop,
                    iconColor = Color(0xFF386B1F),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // #E3E3D2
                    testTag = "stat_springs"
                )

                StatItemCard(
                    modifier = Modifier.weight(1f),
                    title = "Monitoramento",
                    value = "1.2M ha ativos",
                    icon = Icons.Default.Map,
                    iconColor = Color(0xFF705D00),
                    containerColor = MaterialTheme.colorScheme.tertiary, // #F2E0BD
                    testTag = "stat_mapping"
                )
            }
        }

        // Quick actions / Highlights - Seja um Voluntário CTA Card (Artistic Flair)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_card")
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF386B1F)) // bg-[#386B1F]
                    .clickable { showAboutDialog = true }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONHEÇA NOSSO TRABALHO",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quem é o Instituto Oréades?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Light Green selection circle with forward arrow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFB7F397), RoundedCornerShape(100.dp)), // bg-[#B7F397]
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ver mais",
                            tint = Color(0xFF121F00), // text-[#121F00]
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Active Campaigns section
        item {
            Text(
                text = "Campanhas e Ações do Instituto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            CampaignCard(
                title = "Projeto Nascentes do Araguaia",
                description = "Restauração ecológica ativa de 12 nascentes degradadas nas cabeceiras do Rio Araguaia. Mobilização de proprietários rurais para cercamento e plantio nativo.",
                progress = 0.75f,
                progressText = "75% Concluído (9 de 12 nascentes)",
                badge = "Destaque",
                badgeColor = Color(0xFF2E7D32)
            )
        }

        item {
            CampaignCard(
                title = "Zonamento Ecológico Cerrado Inteligente",
                description = "Utilização de geoprocessamento avançado para identificação de corredores de biodiversidade prioritários na região sudoeste goiana, promovendo agricultura sustentável.",
                progress = 0.40f,
                progressText = "40% Concluído (Modelagem GIS ativa)",
                badge = "Pesquisa",
                badgeColor = Color(0xFFE65100)
            )
        }

        item {
            CampaignCard(
                title = "Eco-Educação nas Escolas",
                description = "Formação de multiplicadores e plantio de hortas nativas em escolas públicas municipais de Mineiros (GO) para conscientizar sobre os recursos hídricos e a fauna local.",
                progress = 0.90f,
                progressText = "90% Concluído (18 escolas atendidas)",
                badge = "Social",
                badgeColor = Color(0xFF0288D1)
            )
        }
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sobre o Instituto Oréades",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "O Instituto Oréades de Geoprocessamento e Conservação da Natureza é uma organização não-governamental (ONG) sem fins lucrativos fundada em 2001 em Mineiros - Goiás.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        Text(
                            text = "Missão",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Promover a conservação da biodiversidade e o desenvolvimento sustentável integrando conhecimentos tradicionais, pesquisa de campo de alta qualidade e ciência geoespacial de ponta (Geoprocessamento e SIG).",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        Text(
                            text = "Atuação",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "- Elaboração e apoio no Cadastro Ambiental Rural (CAR).\n" +
                                   "- Restauração de ecossistemas degradados e nascentes.\n" +
                                   "- Sensoriamento remoto para detecção precoce de focos de queimadas.\n" +
                                   "- Planejamento de bacias hidrográficas e ecologia de paisagens.\n" +
                                   "- Educação ambiental com foco nas futuras gerações.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        Text(
                            text = "Contato Oficial",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Sede: Mineiros, Goiás, Brasil\n" +
                                   "Website: www.oreades.org.br\n" +
                                   "Email: contato@oreades.org.br",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
fun StatItemCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    testTag: String
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .height(115.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF1C1C16),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C16).copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CampaignCard(
    title: String,
    description: String,
    progress: Float,
    progressText: String,
    badge: String,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = badgeColor
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = badgeColor,
                trackColor = badgeColor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
