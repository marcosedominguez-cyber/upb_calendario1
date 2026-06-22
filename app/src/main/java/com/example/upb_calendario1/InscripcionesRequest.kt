package com.example.upb_calendario1

data class InscripcionesRequest(
    val cod_est_upb: String,
    val estado: String,
    val fecha_inscripcion: String,
    val id_materia: Int,
    val nota_final: String
)