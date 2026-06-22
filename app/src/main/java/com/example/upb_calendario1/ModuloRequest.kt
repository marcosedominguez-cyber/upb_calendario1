package com.example.upb_calendario1

data class ModuloRequest(
    val fecha_fin: String,
    val fecha_inicio: String,
    val nombre: String,
    val numero_modulo: Int
)