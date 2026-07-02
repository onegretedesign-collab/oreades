package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reforestation_plots")
data class ReforestationPlot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ownerName: String,
    val locationName: String,
    val areaHectares: Double,
    val soilType: String,
    val targetSpring: String,
    val carbonOffsetKg: Double = 0.0,
    val notes: String = "",
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "planted_species")
data class PlantedSpecie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plotId: Int,
    val speciesName: String,
    val quantity: Int,
    val plantingDate: Long = System.currentTimeMillis(),
    val status: String = "Muda" // "Muda", "Desenvolvimento", "Adulto"
)

@Entity(tableName = "threat_reports")
data class EnvironmentalThreatReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threatType: String, // "Fogo", "Desmatamento", "Contaminação de Água", "Extração Ilegal"
    val reporterName: String,
    val latitude: Double,
    val longitude: Double,
    val locationDescription: String,
    val severity: String, // "Baixo", "Médio", "Alto", "Crítico"
    val description: String,
    val dateReported: Long = System.currentTimeMillis()
)

// Data class to represent native species guidelines (non-persisted static list, but useful)
data class NativeSpeciesInfo(
    val name: String,
    val scientificName: String,
    val description: String,
    val carbonAbsorptionFactor: Double, // kg of CO2 absorbed per tree per year
    val idealSoil: String,
    val growthRate: String, // "Rápido", "Médio", "Lento"
    val benefits: String
)
