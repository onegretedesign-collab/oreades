package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlantedSpecie
import com.example.data.ReforestationPlot
import com.example.viewmodel.OreadesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    viewModel: OreadesViewModel,
    modifier: Modifier = Modifier
) {
    val plots by viewModel.plots.collectAsState()
    val currentPlot by viewModel.currentPlot.collectAsState()
    val plotSpecies by viewModel.currentPlotSpecies.collectAsState()

    var showAddPlotDialog by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = currentPlot,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "restore_navigation"
    ) { activePlot ->
        if (activePlot == null) {
            // Screen 1: List of all Reforestation Plots + General Native Species Guide
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("restore_plots_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Portal de Restauração",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Gerencie seus projetos de reflorestamento, registre mudas plantadas e calcule o sequestro de carbono local.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Add Plot Button
                item {
                    Button(
                        onClick = { showAddPlotDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_plot_screen"),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Nova Área para Restauro", fontWeight = FontWeight.Bold)
                    }
                }

                // Header for areas list
                item {
                    Text(
                        text = "Suas Áreas Registradas (${plots.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (plots.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Park,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Nenhuma área cadastrada.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cadastre uma propriedade ou parcela rural para começar a registrar plantios de mudas nativas do Cerrado.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(plots) { plot ->
                        PlotItemCard(
                            plot = plot,
                            onClick = { viewModel.selectPlot(plot) }
                        )
                    }
                }

                // Cerrado Native Species Guide Section
                item {
                    Text(
                        text = "Guia de Espécies Nativas do Cerrado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(viewModel.nativeSpeciesList) { info ->
                    NativeSpecieGuideCard(info = info)
                }
            }
        } else {
            // Screen 2: Detailed view for a single selected Reforestation Plot
            PlotDetailView(
                plot = activePlot,
                speciesList = plotSpecies,
                onBack = { viewModel.selectPlot(null as ReforestationPlot? ?: return@PlotDetailView) },
                viewModel = viewModel
            )
        }
    }

    // Add Plot Dialog
    if (showAddPlotDialog) {
        var plotName by remember { mutableStateOf("") }
        var ownerName by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var hctText by remember { mutableStateOf("") }
        var soilType by remember { mutableStateOf("Latossolo (Argiloso)") }
        var targetSpring by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        var soilExpanded by remember { mutableStateOf(false) }
        val soilTypesList = listOf("Latossolo (Argiloso)", "Arenoso", "Cambiolo (Pedregoso)", "Gleissolo (Úmido/Brejo)")

        AlertDialog(
            onDismissRequest = { showAddPlotDialog = false },
            title = { Text("Registrar Área de Reflorestamento", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = plotName,
                            onValueChange = { plotName = it },
                            label = { Text("Nome do Projeto / Parcela") },
                            placeholder = { Text("Ex: Reflorestamento Nascente Jacuba") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plot_input_name")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Nome do Proprietário") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plot_input_owner")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Município / Localidade") },
                            placeholder = { Text("Ex: Mineiros - GO") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plot_input_location")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = hctText,
                            onValueChange = { hctText = it },
                            label = { Text("Área total (Hectares)") },
                            placeholder = { Text("Ex: 2.5") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plot_input_area")
                        )
                    }

                    // Soil Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = soilExpanded,
                            onExpandedChange = { soilExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = soilType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Solo Predominante") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = soilExpanded,
                                onDismissRequest = { soilExpanded = false }
                            ) {
                                soilTypesList.forEach { soil ->
                                    DropdownMenuItem(
                                        text = { Text(soil) },
                                        onClick = {
                                            soilType = soil
                                            soilExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = targetSpring,
                            onValueChange = { targetSpring = it },
                            label = { Text("Córrego / Nascente Beneficiada") },
                            placeholder = { Text("Ex: Córrego Jacuba (Nascente Principal)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plot_input_spring")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Observações Adicionais") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plot_input_notes")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hct = hctText.toDoubleOrNull() ?: 1.0
                        if (plotName.isNotBlank() && ownerName.isNotBlank() && location.isNotBlank() && targetSpring.isNotBlank()) {
                            viewModel.addPlot(
                                name = plotName,
                                owner = ownerName,
                                location = location,
                                area = hct,
                                soil = soilType,
                                spring = targetSpring,
                                notes = notes
                            )
                            showAddPlotDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_submit_plot")
                ) {
                    Text("Salvar Área")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlotDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PlotItemCard(
    plot: ReforestationPlot,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Default.Forest,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Proprietário: ${plot.ownerName}  |  Local: ${plot.locationName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Beneficia:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = plot.targetSpring,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CO₂ Retido:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f kg/ano", plot.carbonOffsetKg),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00796B)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotDetailView(
    plot: ReforestationPlot,
    speciesList: List<PlantedSpecie>,
    onBack: () -> Unit,
    viewModel: OreadesViewModel
) {
    var showAddPlantingDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("plot_detail_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Plot Summary Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumo Ambiental Georreferenciado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Proprietário: ${plot.ownerName}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Local: ${plot.locationName}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Tamanho: ${plot.areaHectares} Hectares", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Tipo de Solo: ${plot.soilType}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Nascente Protegida: ${plot.targetSpring}", style = MaterialTheme.typography.bodyMedium)
                    if (plot.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Obs: ${plot.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Captura de Carbono Estimada:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Baseado em espécies de árvores ativas plantadas.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format("%.0f kg CO₂/ano", plot.carbonOffsetKg),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00796B)
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddPlantingDialog = true },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("btn_log_planting"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Nature, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Plantio", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excluir Área")
                }
            }
        }

        // Planted Species Header
        item {
            Text(
                text = "Mudas Plantadas Registradas (${speciesList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (speciesList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma muda registrada nesta área de preservação. Clique em 'Registrar Plantio' para iniciar.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(speciesList) { planted ->
                PlantedSpecieItem(
                    planted = planted,
                    onDelete = { viewModel.deletePlantedSpecie(planted.id) }
                )
            }
        }
    }

    // Add Planting Dialog
    if (showAddPlantingDialog) {
        var selectedSpecies by remember { mutableStateOf(viewModel.nativeSpeciesList.first().name) }
        var quantityText by remember { mutableStateOf("") }
        var speciesExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddPlantingDialog = false },
            title = { Text("Registrar Novo Plantio de Mudas", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Insira as mudas plantadas de acordo com as espécies do Cerrado cultivadas no viveiro de mudas do Oréades.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Species Dropdown selection
                    ExposedDropdownMenuBox(
                        expanded = speciesExpanded,
                        onExpandedChange = { speciesExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSpecies,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Espécie Nativa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = speciesExpanded,
                            onDismissRequest = { speciesExpanded = false }
                        ) {
                            viewModel.nativeSpeciesList.forEach { info ->
                                DropdownMenuItem(
                                    text = { Text("${info.name} (${info.scientificName})") },
                                    onClick = {
                                        selectedSpecies = info.name
                                        speciesExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantidade de Mudas") },
                        placeholder = { Text("Ex: 50") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planting_input_qty")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull() ?: 1
                        if (qty > 0) {
                            viewModel.addPlantedSpecie(selectedSpecies, qty)
                            showAddPlantingDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_submit_planting")
                ) {
                    Text("Salvar Plantio")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlantingDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Plot Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Projeto de Restauro?", fontWeight = FontWeight.Bold) },
            text = { Text("Essa ação é irreversível e excluirá todo o histórico de árvores plantadas nesta área do seu aplicativo.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlot(plot.id)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PlantedSpecieItem(
    planted: PlantedSpecie,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateText = sdf.format(Date(planted.plantingDate))

    // Estimate CO2 offset per tree based on species name
    val factor = when (planted.speciesName.lowercase()) {
        "ipê amarelo", "ipe amarelo" -> 15.0
        "baru" -> 22.0
        "aroeira" -> 12.0
        "jatobá", "jatoba" -> 25.0
        "angico" -> 18.0
        else -> 10.0
    }
    val totalSpecieOffset = planted.quantity * factor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Park,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${planted.quantity}x ${planted.speciesName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Plantado em: $dateText  |  Status: ${planted.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${totalSpecieOffset.toInt()} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00796B)
                    )
                    Text(
                        text = "CO₂/ano",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover Plantio",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun NativeSpecieGuideCard(info: com.example.data.NativeSpeciesInfo) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = info.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Fechar" else "Abrir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Sequestro CO₂:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(text = "~${info.carbonAbsorptionFactor.toInt()} kg/árvore/ano", style = MaterialTheme.typography.bodySmall, color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Crescimento:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(text = info.growthRate, style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Solo Ideal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(text = info.idealSoil, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Benefícios Ecológicos:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = info.benefits,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
