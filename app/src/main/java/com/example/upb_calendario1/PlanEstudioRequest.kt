package com.example.upb_calendario1

data class PlanEstudioRequest(
    val cod_est_upb: String,
    val fecha_fin: String,
    val fecha_inicio: String,
    val horas_disponibles_semana: Int,
    val intensidad: String,
    val nombre: String,
    val rendimiento_objetivo: String
)