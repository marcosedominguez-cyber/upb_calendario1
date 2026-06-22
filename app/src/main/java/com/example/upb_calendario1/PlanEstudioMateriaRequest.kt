package com.example.upb_calendario1

data class PlanEstudioMateriaRequest(
    val horas_asignadas: Int,
    val id_materia: Int,
    val id_plan: Int,
    val prioridad: Int
)