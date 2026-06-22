package com.example.upb_calendario1

data class HorarioMateriaResponse(
    val aula: String,
    val created_at: String,
    val dia_semana: String,
    val hora_fin: String,
    val hora_inicio: String,
    val id_horario: Int,
    val id_materia: Int,
    val updated_at: String
)