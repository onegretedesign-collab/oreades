package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OreadesDao {
    // Reforestation Plot operations
    @Query("SELECT * FROM reforestation_plots ORDER BY dateCreated DESC")
    fun getAllPlots(): Flow<List<ReforestationPlot>>

    @Query("SELECT * FROM reforestation_plots WHERE id = :id LIMIT 1")
    suspend fun getPlotById(id: Int): ReforestationPlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: ReforestationPlot): Long

    @Query("DELETE FROM reforestation_plots WHERE id = :id")
    suspend fun deletePlotById(id: Int)

    // Planted Tree operations
    @Query("SELECT * FROM planted_species WHERE plotId = :plotId ORDER BY plantingDate DESC")
    fun getSpeciesForPlot(plotId: Int): Flow<List<PlantedSpecie>>

    @Query("SELECT * FROM planted_species WHERE plotId = :plotId")
    suspend fun getSpeciesForPlotSync(plotId: Int): List<PlantedSpecie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantedSpecie(specie: PlantedSpecie)

    @Query("DELETE FROM planted_species WHERE id = :id")
    suspend fun deletePlantedSpecie(id: Int)

    @Query("DELETE FROM planted_species WHERE plotId = :plotId")
    suspend fun deleteSpeciesByPlotId(plotId: Int)

    // Threat Report operations
    @Query("SELECT * FROM threat_reports ORDER BY dateReported DESC")
    fun getAllThreatReports(): Flow<List<EnvironmentalThreatReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatReport(report: EnvironmentalThreatReport)

    @Query("DELETE FROM threat_reports WHERE id = :id")
    suspend fun deleteThreatReport(id: Int)
}
