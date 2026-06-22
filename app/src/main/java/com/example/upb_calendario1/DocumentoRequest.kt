package com.example.upb_calendario1

data class DocumentoRequest(
    val descripcion: String,
    val id_admin: Int,
    val id_materia: Int,
    val tipo_documento: String,
    val titulo: String,
    val url_archivo: String
)