package com.example.data

import kotlinx.coroutines.flow.Flow

class OreadesRepository(private val dao: OreadesDao) {

    val allPlots: Flow<List<ReforestationPlot>> = dao.getAllPlots()
    val allThreatReports: Flow<List<EnvironmentalThreatReport>> = dao.getAllThreatReports()

    suspend fun getPlotById(id: Int): ReforestationPlot? {
        return dao.getPlotById(id)
    }

    suspend fun insertPlot(plot: ReforestationPlot): Long {
        return dao.insertPlot(plot)
    }

    suspend fun deletePlot(plotId: Int) {
        dao.deleteSpeciesByPlotId(plotId)
        dao.deletePlotById(plotId)
    }

    fun getSpeciesForPlot(plotId: Int): Flow<List<PlantedSpecie>> {
        return dao.getSpeciesForPlot(plotId)
    }

    suspend fun insertPlantedSpecie(specie: PlantedSpecie) {
        dao.insertPlantedSpecie(specie)
        
        // Recalculate carbon offset for the plot
        updatePlotCarbonOffset(specie.plotId)
    }

    suspend fun deletePlantedSpecie(specieId: Int, plotId: Int) {
        dao.deletePlantedSpecie(specieId)
        updatePlotCarbonOffset(plotId)
    }

    suspend fun insertThreatReport(report: EnvironmentalThreatReport) {
        dao.insertThreatReport(report)
    }

    suspend fun deleteThreatReport(id: Int) {
        dao.deleteThreatReport(id)
    }

    // Recalculate the carbon offset for a plot based on trees planted
    private suspend fun updatePlotCarbonOffset(plotId: Int) {
        val plot = dao.getPlotById(plotId) ?: return
        val speciesList = dao.getSpeciesForPlotSync(plotId)
        
        // Let's calculate total carbon absorption
        // For our static factors:
        // Ipê Amarelo = 15kg/year, Baru = 22kg/year, Aroeira = 12kg/year, Jatobá = 25kg/year, Angico = 18kg/year
        var totalOffset = 0.0
        for (specie in speciesList) {
            val factor = when (specie.speciesName.lowercase()) {
                "ipê amarelo", "ipe amarelo" -> 15.0
                "baru" -> 22.0
                "aroeira" -> 12.0
                "jatobá", "jatoba" -> 25.0
                "angico" -> 18.0
                else -> 10.0 // Default tree
            }
            totalOffset += specie.quantity * factor
        }
        
        val updatedPlot = plot.copy(carbonOffsetKg = totalOffset)
        dao.insertPlot(updatedPlot)
    }
}
