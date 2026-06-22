package com.example.upb_calendario1

data class MateriaResponse(
    val created_at: String,
    val creditos: Int,
    val duracion_semanas: Int,
    val id_admin: Int,
    val id_materia: Int,
    val id_modulo: Int,
    val nombre: String,
    val tipo_materia: String,
    val updated_at: String
)