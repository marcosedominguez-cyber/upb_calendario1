package com.example.upb_calendario1

data class EventoCalendarioRequest(
    val descripcion: String,
    val es_recurrente: Int,
    val fecha_fin: String,
    val fecha_inicio: String,
    val id_calendario: Int,
    val id_materia: Int,
    val tipo_evento: String,
    val titulo: String
)