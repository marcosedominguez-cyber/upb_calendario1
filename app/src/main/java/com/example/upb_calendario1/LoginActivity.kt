package com.example.upb_calendario1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {

    private lateinit var edtCorreo: EditText
    private lateinit var edtContrasena: EditText
    private lateinit var rbEstudiante: RadioButton
    private lateinit var rbAdmin: RadioButton
    private lateinit var txtMensajeLogin: TextView

    private val api = ApiService.RetrofitClient.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtCorreo = findViewById(R.id.edtCorreo)
        edtContrasena = findViewById(R.id.edtContrasena)
        rbEstudiante = findViewById(R.id.rbEstudiante)
        rbAdmin = findViewById(R.id.rbAdmin)
        txtMensajeLogin = findViewById(R.id.txtMensajeLogin)

        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)

        btnIngresar.setOnClickListener {
            iniciarSesion()
        }

        btnRegistro.setOnClickListener {
            Toast.makeText(this, "Registro lo hacemos después", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarSesion() {
        val correo = edtCorreo.text.toString().trim()
        val contrasena = edtContrasena.text.toString().trim()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            txtMensajeLogin.text = "Debes ingresar correo y contraseña."
            return
        }

        txtMensajeLogin.text = "Validando datos..."

        if (rbAdmin.isChecked) {
            loginAdmin(correo, contrasena)
        } else {
            loginEstudiante(correo, contrasena)
        }
    }

    private fun loginAdmin(correo: String, contrasena: String) {
        lifecycleScope.launch {
            try {
                val response = api.getAdmins()

                if (response.isSuccessful) {
                    val admins = response.body()?.data

                    val adminEncontrado = admins?.firstOrNull {
                        it.correo.equals(correo, ignoreCase = true) &&
                                it.contrasena == contrasena
                    }

                    if (adminEncontrado != null) {
                        val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                        intent.putExtra("id_admin", adminEncontrado.id_admin)
                        intent.putExtra("nombre", adminEncontrado.nombre)
                        startActivity(intent)
                        finish()
                    } else {
                        txtMensajeLogin.text = "Admin no encontrado o contraseña incorrecta."
                    }

                } else {
                    txtMensajeLogin.text = "Error al consultar admins: ${response.code()}"
                }

            } catch (e: Exception) {
                txtMensajeLogin.text = "Error: ${e.message}"
            }
        }
    }

    private fun loginEstudiante(correo: String, contrasena: String) {
        lifecycleScope.launch {
            try {
                val response = api.getUsuarios()

                if (response.isSuccessful) {
                    val usuarios = response.body()?.data

                    val usuarioEncontrado = usuarios?.firstOrNull {
                        it.correo.equals(correo, ignoreCase = true) &&
                                it.contrasena == contrasena
                    }

                    if (usuarioEncontrado != null) {
                        val intent = Intent(this@LoginActivity, UsuarioActivity::class.java)

                        intent.putExtra("cod_est_upb", usuarioEncontrado.cod_est_upb)
                        intent.putExtra("nombre", usuarioEncontrado.nombre)
                        intent.putExtra("apellido", usuarioEncontrado.apellido)
                        intent.putExtra("edad", usuarioEncontrado.edad)
                        intent.putExtra("correo", usuarioEncontrado.correo)
                        intent.putExtra("contrasena", usuarioEncontrado.contrasena)
                        intent.putExtra("id_carrera", usuarioEncontrado.id_carrera)

                        startActivity(intent)
                        finish()
                    } else {
                        txtMensajeLogin.text = "Estudiante no encontrado o contraseña incorrecta."
                    }

                } else {
                    txtMensajeLogin.text = "Error al consultar usuarios: ${response.code()}"
                }

            } catch (e: Exception) {
                txtMensajeLogin.text = "Error: ${e.message}"
            }
        }
    }
}