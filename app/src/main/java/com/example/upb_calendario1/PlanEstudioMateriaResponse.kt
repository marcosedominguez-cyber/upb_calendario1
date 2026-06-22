package com.example.upb_calendario1

data class PlanEstudioMateriaResponse(
    val created_at: String,
    val horas_asignadas: Int,
    val id_materia: Int,
    val id_plan: Int,
    val id_plan_materia: Int,
    val prioridad: Int,
    val updated_at: String
)