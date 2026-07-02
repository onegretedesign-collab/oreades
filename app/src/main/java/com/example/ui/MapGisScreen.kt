package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EnvironmentalThreatReport
import com.example.viewmodel.OreadesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapGisScreen(
    viewModel: OreadesViewModel,
    modifier: Modifier = Modifier
) {
    val threatReports by viewModel.threatReports.collectAsState()
    val plots by viewModel.plots.collectAsState()

    // Map filters state
    var showFires by remember { mutableStateOf(true) }
    var showWater by remember { mutableStateOf(true) }
    var showReforestation by remember { mutableStateOf(true) }

    // Map interaction states
    var selectedLocationName by remember { mutableStateOf("Mineiros - Goiás (Sede)") }
    var selectedLocLat by remember { mutableStateOf(-17.5683) }
    var selectedLocLng by remember { mutableStateOf(-52.5511) }
    var selectedLocDetails by remember { mutableStateOf("Sede administrativa e viveiro de mudas nativas do Instituto Oréades.") }
    var selectedLocEcoIndex by remember { mutableStateOf("92% (Excelente)") }

    // Threat dialog state
    var showReportDialog by remember { mutableStateOf(false) }

    // Static natural sites on the map
    val mapPoints = remember {
        listOf(
            MapPoint("Parque Nacional das Emas", 200f, 150f, -18.15, -52.91, "Reserva de biosfera crucial do Cerrado. Alta biodiversidade.", "98% (Preservado)"),
            MapPoint("Cabeceira Rio Araguaia", 100f, 250f, -17.82, -52.79, "Nascente principal do majestoso Rio Araguaia, foco de restauração do Oréades.", "68% (Alerta - Erosão)"),
            MapPoint("Serra de Jataí", 400f, 120f, -17.88, -51.72, "Região de transição com relevo ondulado e mata de galeria.", "81% (Estável)"),
            MapPoint("Rio Claro", 300f, 280f, -17.65, -52.12, "Importante corpo hídrico de abastecimento municipal.", "84% (Bom)")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_gis_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Screen Header
        item {
            Column {
                Text(
                    text = "Mapeamento e Geoprocessamento",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Monitore o Cerrado e as nascentes do Rio Araguaia com nossa central SIG interativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Layer Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showFires,
                    onClick = { showFires = !showFires },
                    label = { Text("Queimadas") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = showWater,
                    onClick = { showWater = !showWater },
                    label = { Text("Hidrografia") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Water,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = showReforestation,
                    onClick = { showReforestation = !showReforestation },
                    label = { Text("Restauros") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Park,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // The Interactive Map Canvas - styled with secondary pale green background & 28.dp rounded corners
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    // Hit test for points on the map
                                    var clickedOnPoint = false
                                    for (point in mapPoints) {
                                        val distance = sqrt((offset.x - point.x) * (offset.x - point.x) + (offset.y - point.y) * (offset.y - point.y))
                                        if (distance < 30f) {
                                            selectedLocationName = point.name
                                            selectedLocLat = point.lat
                                            selectedLocLng = point.lng
                                            selectedLocDetails = point.description
                                            selectedLocEcoIndex = point.ecoIndex
                                            clickedOnPoint = true
                                            break
                                        }
                                    }
                                    if (!clickedOnPoint) {
                                        // Tap on arbitrary location
                                        val simulatedLat = -17.50 + (offset.y / 1000f)
                                        val simulatedLng = -52.70 + (offset.x / 1000f)
                                        selectedLocationName = "Ponto Coordenado (GIS)"
                                        selectedLocLat = Math.round(simulatedLat * 10000.0) / 10000.0
                                        selectedLocLng = Math.round(simulatedLng * 10000.0) / 10000.0
                                        selectedLocDetails = "Localização capturada via cursor georreferenciado por sensoriamento remoto."
                                        selectedLocEcoIndex = "85% (Bom)"
                                    }
                                }
                            }
                    ) {
                        // 1. Draw River lines (Hydrography)
                        if (showWater) {
                            val riverPath = Path().apply {
                                moveTo(0f, 120f)
                                cubicTo(150f, 130f, 120f, 260f, 280f, 290f)
                                cubicTo(350f, 305f, 400f, 150f, size.width, 180f)
                            }
                            drawPath(
                                path = riverPath,
                                color = Color(0xFF0288D1),
                                style = Stroke(width = 6f)
                            )
                            
                            // Auxiliary stream
                            val streamPath = Path().apply {
                                moveTo(280f, 290f)
                                lineTo(200f, size.height)
                            }
                            drawPath(
                                path = streamPath,
                                color = Color(0xFF29B6F6),
                                style = Stroke(width = 3f)
                            )
                        }

                        // 2. Draw forest reserves (Green circles with fuzzy boundaries)
                        drawCircle(
                            color = Color(0xFF2E7D32).copy(alpha = 0.25f),
                            radius = 70f,
                            center = Offset(180f, 130f)
                        )
                        drawCircle(
                            color = Color(0xFF2E7D32).copy(alpha = 0.25f),
                            radius = 50f,
                            center = Offset(420f, 100f)
                        )

                        // 3. Draw static map points
                        for (point in mapPoints) {
                            // Point glow outline
                            drawCircle(
                                color = Color.White,
                                radius = 10f,
                                center = Offset(point.x, point.y)
                            )
                            drawCircle(
                                color = Color(0xFF1B5E20),
                                radius = 6f,
                                center = Offset(point.x, point.y)
                            )
                        }

                        // 4. Draw reforestation projects from local database (Green checkmarks/dots)
                        if (showReforestation) {
                            plots.forEachIndexed { index, _ ->
                                // Generate a simulated map coordinate based on plot index
                                val px = 150f + (index * 85f) % (size.width - 100f)
                                val py = 120f + (index * 60f) % (size.height - 100f)
                                drawCircle(
                                    color = Color(0xFF4CAF50),
                                    radius = 12f,
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = Offset(px, py)
                                )
                            }
                        }

                        // 5. Draw reported threats from local database (Red indicators)
                        if (showFires) {
                            threatReports.forEachIndexed { index, report ->
                                val px = 120f + (index * 110f) % (size.width - 100f)
                                val py = 80f + (index * 75f) % (size.height - 100f)
                                
                                val color = when (report.threatType) {
                                    "Fogo" -> Color(0xFFE65100)
                                    "Desmatamento" -> Color(0xFFD84315)
                                    else -> Color(0xFFC62828)
                                }
                                
                                // Glowing outer pulse ring
                                drawCircle(
                                    color = color.copy(alpha = 0.35f),
                                    radius = 16f,
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = color,
                                    radius = 8f,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }

                    // Map overlay labels
                    Text(
                        text = "BACIA DO ARAGUAIA (SIMULAÇÃO SIG)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33691E).copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Rosa dos ventos",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(36.dp)
                    )
                }
            }
        }

        // Details of selected map coordinate - styled with 28.dp rounded corners
        item {
            Card(
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
                        Text(
                            text = selectedLocationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lat: $selectedLocLat  |  Lng: $selectedLocLng",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedLocDetails,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Índice de Conservação:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedLocEcoIndex,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Action: Report Threat - styled with 28.dp rounded corners
        item {
            Button(
                onClick = { showReportDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_open_report_dialog"),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reportar Alerta Ambiental (Cidadão)",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Subtitle section for local reports
        item {
            Text(
                text = "Alertas de Crimes Ambientais Ativos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List of Active Threat Reports
        if (threatReports.isEmpty()) {
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum alerta pendente na sua região.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(threatReports) { report ->
                ThreatReportItem(
                    report = report,
                    onDelete = { viewModel.deleteThreatReport(report.id) }
                )
            }
        }
    }

    // Report Threat Dialog
    if (showReportDialog) {
        var threatType by remember { mutableStateOf("Fogo") }
        var reporterName by remember { mutableStateOf("") }
        var localName by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var severity by remember { mutableStateOf("Médio") }

        var typeExpanded by remember { mutableStateOf(false) }
        var severityExpanded by remember { mutableStateOf(false) }

        val typesList = listOf("Fogo", "Desmatamento", "Contaminação de Água", "Extração Ilegal")
        val severitiesList = listOf("Baixo", "Médio", "Alto", "Crítico")

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Reportar Alerta Ambiental",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Ajude o Instituto Oréades a georreferenciar crimes e focos de incêndio no Cerrado para encaminhamento aos órgãos competentes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Threat Type Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = threatType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Ocorrência") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                typesList.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            threatType = type
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Severity Dropdown
                    item {
                        ExposedDropdownMenuBox(
                            expanded = severityExpanded,
                            onExpandedChange = { severityExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = severity,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gravidade") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = severityExpanded,
                                onDismissRequest = { severityExpanded = false }
                            ) {
                                severitiesList.forEach { sev ->
                                    DropdownMenuItem(
                                        text = { Text(sev) },
                                        onClick = {
                                            severity = sev
                                            severityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = reporterName,
                            onValueChange = { reporterName = it },
                            label = { Text("Seu Nome / Denunciante") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_input_reporter")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = localName,
                            onValueChange = { localName = it },
                            label = { Text("Nome do Local (ex: Fazenda Recanto)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_input_location")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("Descrição dos Fatos") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_input_description")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reporterName.isNotBlank() && localName.isNotBlank() && desc.isNotBlank()) {
                            // Generate random coordinate around Mineiros headquarters
                            val rLat = -17.5683 + (Random().nextDouble() - 0.5) * 0.4
                            val rLng = -52.5511 + (Random().nextDouble() - 0.5) * 0.4
                            val formattedLat = Math.round(rLat * 10000.0) / 10000.0
                            val formattedLng = Math.round(rLng * 10000.0) / 10000.0

                            viewModel.addThreatReport(
                                type = threatType,
                                reporter = reporterName,
                                lat = formattedLat,
                                lng = formattedLng,
                                locDesc = localName,
                                severity = severity,
                                desc = desc
                            )
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_submit_threat_report")
                ) {
                    Text("Enviar Denúncia")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ThreatReportItem(
    report: EnvironmentalThreatReport,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = sdf.format(Date(report.dateReported))

    val severityColor = when (report.severity) {
        "Crítico" -> Color(0xFFC62828)
        "Alto" -> Color(0xFFE65100)
        "Médio" -> Color(0xFFFBC02D)
        else -> Color(0xFF558B2F)
    }

    val icon = when (report.threatType) {
        "Fogo" -> Icons.Default.LocalFireDepartment
        "Desmatamento" -> Icons.Default.Terrain
        "Contaminação de Água" -> Icons.Default.Opacity
        else -> Icons.Default.Warning
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.threatType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover Alerta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gravidade: ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = report.severity,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
                Text(
                    text = "  |  Local: ${report.locationDescription}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Registrado por: ${report.reporterName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Coordenadas: ${report.latitude}, ${report.longitude}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Data class holder for static map highlights
data class MapPoint(
    val name: String,
    val x: Float,
    val y: Float,
    val lat: Double,
    val lng: Double,
    val description: String,
    val ecoIndex: String
)
