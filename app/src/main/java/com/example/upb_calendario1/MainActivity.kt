package com.example.upb_calendario1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var edtId: EditText
    private lateinit var edtNombre: EditText
    private lateinit var edtFacultad: EditText
    private lateinit var txtResultado: TextView

    private val api = ApiService.RetrofitClient.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edtId = findViewById(R.id.edtId)
        edtNombre = findViewById(R.id.edtNombre)
        edtFacultad = findViewById(R.id.edtFacultad)
        txtResultado = findViewById(R.id.txtResultado)

        val btnListar = findViewById<Button>(R.id.btnListar)
        val btnCrear = findViewById<Button>(R.id.btnCrear)
        val btnModificar = findViewById<Button>(R.id.btnModificar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        btnListar.setOnClickListener {
            listarCarreras()
        }

        btnCrear.setOnClickListener {
            crearCarrera()
        }

        btnModificar.setOnClickListener {
            modificarCarrera()
        }

        btnEliminar.setOnClickListener {
            eliminarCarrera()
        }
    }

    private fun listarCarreras() {
        lifecycleScope.launch {
            try {
                val response = api.getCarreras()

                if (response.isSuccessful) {
                    val carreras = response.body()?.data

                    if (carreras != null) {
                        val texto = carreras.joinToString("\n\n") {
                            "ID: ${it.id_carrera}\nNombre: ${it.nombre}\nFacultad: ${it.facultad}"
                        }

                        txtResultado.text = texto
                    } else {
                        txtResultado.text = "No hay datos."
                    }
                } else {
                    txtResultado.text = "Error al listar: ${response.code()}"
                }

            } catch (e: Exception) {
                txtResultado.text = "Error: ${e.message}"
            }
        }
    }

    private fun crearCarrera() {
        val nombre = edtNombre.text.toString()
        val facultad = edtFacultad.text.toString()

        lifecycleScope.launch {
            try {
                val response = api.crearCarrera(nombre, facultad)

                if (response.isSuccessful) {
                    val carrera = response.body()
                    txtResultado.text = "Carrera creada:\n$carrera"
                } else {
                    txtResultado.text = "Error al crear: ${response.code()}"
                }

            } catch (e: Exception) {
                txtResultado.text = "Error: ${e.message}"
            }
        }
    }

    private fun modificarCarrera() {
        val id = edtId.text.toString().toIntOrNull()
        val nombre = edtNombre.text.toString()
        val facultad = edtFacultad.text.toString()

        if (id == null) {
            txtResultado.text = "Debes ingresar un ID válido."
            return
        }

        lifecycleScope.launch {
            try {
                val request = CarreraUpdateRequest(
                    nombre = nombre,
                    facultad = facultad
                )

                val response = api.actualizarCarrera(id, request)

                if (response.isSuccessful) {
                    val carrera = response.body()
                    txtResultado.text = "Carrera modificada:\n$carrera"
                } else {
                    txtResultado.text = "Error al modificar: ${response.code()}"
                }

            } catch (e: Exception) {
                txtResultado.text = "Error: ${e.message}"
            }
        }
    }

    private fun eliminarCarrera() {
        val id = edtId.text.toString().toIntOrNull()

        if (id == null) {
            txtResultado.text = "Debes ingresar un ID válido."
            return
        }

        lifecycleScope.launch {
            try {
                val response = api.eliminarCarrera(id)

                if (response.isSuccessful) {
                    txtResultado.text = "Carrera eliminada correctamente."
                } else {
                    txtResultado.text = "Error al eliminar: ${response.code()}"
                }

            } catch (e: Exception) {
                txtResultado.text = "Error: ${e.message}"
            }
        }
    }
}