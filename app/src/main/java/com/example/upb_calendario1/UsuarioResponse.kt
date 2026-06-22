package com.example.upb_calendario1

data class UsuarioResponse(
    val apellido: String,
    val cod_est_upb: String,
    val contrasena: String,
    val correo: String,
    val created_at: String,
    val edad: Int,
    val id_carrera: Int,
    val id_usuario: Int,
    val nombre: String,
    val updated_at: String
)