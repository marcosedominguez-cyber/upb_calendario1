package com.example.upb_calendario1

data class EventoCalendarioResponse(
    val created_at: String,
    val descripcion: String,
    val es_recurrente: Int,
    val fecha_fin: String,
    val fecha_inicio: String,
    val id_calendario: Int,
    val id_evento: Int,
    val id_materia: Int,
    val tipo_evento: String,
    val titulo: String,
    val updated_at: String
)