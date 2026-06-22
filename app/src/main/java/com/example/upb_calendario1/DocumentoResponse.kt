package com.example.upb_calendario1

data class DocumentoResponse(
    val created_at: String,
    val descripcion: String,
    val id_admin: Int,
    val id_documento: Int,
    val id_materia: Int,
    val tipo_documento: String,
    val titulo: String,
    val updated_at: String,
    val url_archivo: String
)