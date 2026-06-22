package com.example.upb_calendario1

data class UsuarioRequest(
    val apellido: String,
    val cod_est_upb: String,
    val contrasena: String,
    val correo: String,
    val edad: Int,
    val id_carrera: Int,
    val nombre: String
)