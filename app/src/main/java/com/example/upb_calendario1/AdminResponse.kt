package com.example.upb_calendario1

data class AdminResponse(
    val contrasena: String,
    val correo: String,
    val created_at: String,
    val id_admin: Int,
    val nombre: String,
    val updated_at: String
)