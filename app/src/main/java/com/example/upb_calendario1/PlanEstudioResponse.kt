package com.example.upb_calendario1

data class PlanEstudioResponse(
    val cod_est_upb: String,
    val created_at: String,
    val fecha_fin: String,
    val fecha_inicio: String,
    val horas_disponibles_semana: Int,
    val id_plan: Int,
    val intensidad: String,
    val nombre: String,
    val rendimiento_objetivo: String,
    val updated_at: String
)