package com.example.upb_calendario1

data class InscripcionesResponse(
    val cod_est_upb: String,
    val created_at: String,
    val estado: String,
    val fecha_inscripcion: String,
    val id_inscripcion: Int,
    val id_materia: Int,
    val nota_final: String,
    val updated_at: String
)