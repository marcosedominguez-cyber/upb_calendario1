package com.example.upb_calendario1

data class HorarioMateriaRequest(
    val aula: String,
    val dia_semana: String,
    val hora_fin: String,
    val hora_inicio: String,
    val id_materia: Int
)