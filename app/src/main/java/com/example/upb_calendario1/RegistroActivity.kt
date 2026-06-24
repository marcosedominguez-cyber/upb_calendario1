package com.example.upb_calendario1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var edtCodigo: EditText
    private lateinit var edtNombre: EditText
    private lateinit var edtApellido: EditText
    private lateinit var edtEdad: EditText
    private lateinit var edtCorreo: EditText
    private lateinit var edtContrasena: EditText
    private lateinit var spCarrera: Spinner
    private lateinit var txtResultado: TextView

    private val api = ApiService.RetrofitClient.create()

    private var listaCarreras = mutableListOf<CarreraResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        inicializarVistas()
        cargarCarreras()

        findViewById<Button>(R.id.btnCrearCuenta).setOnClickListener {
            registrarUsuario()
        }

        findViewById<Button>(R.id.btnVolverLogin).setOnClickListener {
            finish()
        }
    }

    private fun inicializarVistas() {
        edtCodigo = findViewById(R.id.edtRegistroCodigo)
        edtNombre = findViewById(R.id.edtRegistroNombre)
        edtApellido = findViewById(R.id.edtRegistroApellido)
        edtEdad = findViewById(R.id.edtRegistroEdad)
        edtCorreo = findViewById(R.id.edtRegistroCorreo)
        edtContrasena = findViewById(R.id.edtRegistroContrasena)
        spCarrera = findViewById(R.id.spRegistroCarrera)
        txtResultado = findViewById(R.id.txtResultadoRegistro)
    }

    private fun cargarCarreras() {
        txtResultado.text = "Cargando carreras..."

        lifecycleScope.launch {
            try {
                val response = api.getCarreras()

                if (response.isSuccessful) {
                    listaCarreras = response.body()?.data?.toMutableList() ?: mutableListOf()

                    if (listaCarreras.isEmpty()) {
                        txtResultado.text = "No hay carreras registradas."
                        return@launch
                    }

                    val nombresCarreras = listaCarreras.map {
                        "${it.id_carrera} - ${it.nombre}"
                    }

                    val adapter = ArrayAdapter(
                        this@RegistroActivity,
                        android.R.layout.simple_spinner_item,
                        nombresCarreras
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    spCarrera.adapter = adapter

                    txtResultado.text = "Completa tus datos para registrarte."

                } else {
                    txtResultado.text = "Error al cargar carreras: ${response.code()}"
                }

            } catch (e: Exception) {
                txtResultado.text = "Error cargando carreras: ${e.message}"
            }
        }
    }

    private fun registrarUsuario() {
        val codigo = edtCodigo.text.toString().trim()
        val nombre = edtNombre.text.toString().trim()
        val apellido = edtApellido.text.toString().trim()
        val edadTexto = edtEdad.text.toString().trim()
        val correo = edtCorreo.text.toString().trim()
        val contrasena = edtContrasena.text.toString().trim()

        if (codigo.isEmpty() || nombre.isEmpty() || apellido.isEmpty() ||
            edadTexto.isEmpty() || correo.isEmpty() || contrasena.isEmpty()
        ) {
            txtResultado.text = "Completa todos los campos."
            return
        }

        if (!codigo.matches(Regex("^\\d{1,6}$"))) {
            txtResultado.text = "El código UPB debe tener máximo 6 dígitos numéricos."
            return
        }

        val edad = edadTexto.toIntOrNull()

        if (edad == null || edad <= 0) {
            txtResultado.text = "La edad debe ser un número válido."
            return
        }

        if (!correo.contains("@") || !correo.contains(".")) {
            txtResultado.text = "Ingresa un correo válido."
            return
        }

        if (listaCarreras.isEmpty()) {
            txtResultado.text = "No hay carreras disponibles para seleccionar."
            return
        }

        val posicionCarrera = spCarrera.selectedItemPosition

        if (posicionCarrera < 0 || posicionCarrera >= listaCarreras.size) {
            txtResultado.text = "Selecciona una carrera válida."
            return
        }

        val carreraSeleccionada = listaCarreras[posicionCarrera]

        txtResultado.text = "Creando cuenta..."

        lifecycleScope.launch {
            try {
                val response = api.crearUsuario(
                    cod_est_upb = codigo,
                    nombre = nombre,
                    apellido = apellido,
                    edad = edad,
                    correo = correo,
                    contrasena = contrasena,
                    id_carrera = carreraSeleccionada.id_carrera
                )

                if (response.isSuccessful) {
                    txtResultado.text = "Cuenta creada correctamente. Ahora puedes iniciar sesión."

                    Toast.makeText(
                        this@RegistroActivity,
                        "Usuario registrado",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } else {
                    val errorBackend = response.errorBody()?.string()
                    txtResultado.text =
                        "Error al crear cuenta: ${response.code()}\n$errorBackend"
                }

            } catch (e: Exception) {
                txtResultado.text = "Error: ${e.message}"
            }
        }
    }
}