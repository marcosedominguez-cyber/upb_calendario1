package com.example.upb_calendario1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UsuarioActivity : AppCompatActivity() {

    private lateinit var txtBienvenidaUsuario: TextView
    private lateinit var txtFechaSeleccionada: TextView
    private lateinit var txtResultadoUsuario: TextView

    private lateinit var panelPerfil: LinearLayout
    private lateinit var panelMaterias: LinearLayout
    private lateinit var panelEventos: LinearLayout
    private lateinit var panelPlanes: LinearLayout

    private var fechaSeleccionada: String = ""

    private val api = ApiService.RetrofitClient.create()

    private var codEstUpb: String = ""
    private var contrasenaActual: String = ""
    private var idCarreraActual: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario)

        inicializarVistas()
        cargarDatosUsuario()
        configurarCalendario()
        configurarBotonesPrincipales()
        configurarBotonesDeAccion()
    }

    private fun inicializarVistas() {
        txtBienvenidaUsuario = findViewById(R.id.txtBienvenidaUsuario)
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada)
        txtResultadoUsuario = findViewById(R.id.txtResultadoUsuario)

        panelPerfil = findViewById(R.id.panelPerfil)
        panelMaterias = findViewById(R.id.panelMaterias)
        panelEventos = findViewById(R.id.panelEventos)
        panelPlanes = findViewById(R.id.panelPlanes)
    }

    private fun cargarDatosUsuario() {
        codEstUpb = intent.getStringExtra("cod_est_upb") ?: ""
        contrasenaActual = intent.getStringExtra("contrasena") ?: ""

        val nombre = intent.getStringExtra("nombre") ?: "Estudiante"
        val apellido = intent.getStringExtra("apellido") ?: ""
        val edad = intent.getIntExtra("edad", 0)
        val correo = intent.getStringExtra("correo") ?: ""

        idCarreraActual = intent.getIntExtra("id_carrera", 0)

        txtBienvenidaUsuario.text = "Bienvenido\n$nombre $apellido"

        findViewById<EditText>(R.id.edtNombreUsuario).setText(nombre)
        findViewById<EditText>(R.id.edtApellidoUsuario).setText(apellido)
        findViewById<EditText>(R.id.edtEdadUsuario).setText(edad.toString())
        findViewById<EditText>(R.id.edtCorreoUsuario).setText(correo)

        // No mostramos la contraseña actual por seguridad.
        // Si se deja vacío, se conserva la contraseña anterior.
        findViewById<EditText>(R.id.edtContrasenaUsuario).setText("")
    }

    private fun configurarCalendario() {
        val calendarView = findViewById<CalendarView>(R.id.calendarUsuario)

        val calendario = Calendar.getInstance()
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaSeleccionada = formato.format(calendario.time)

        txtFechaSeleccionada.text = "Fecha seleccionada: $fechaSeleccionada"

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val mesCorrecto = month + 1

            fechaSeleccionada = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                year,
                mesCorrecto,
                dayOfMonth
            )

            txtFechaSeleccionada.text = "Fecha seleccionada: $fechaSeleccionada"
        }
    }

    private fun configurarBotonesPrincipales() {
        findViewById<Button>(R.id.btnMostrarPerfil).setOnClickListener {
            mostrarSoloPanel(panelPerfil)
            txtResultadoUsuario.text = "Editando datos del usuario."
        }

        findViewById<Button>(R.id.btnMostrarMaterias).setOnClickListener {
            mostrarSoloPanel(panelMaterias)
            txtResultadoUsuario.text = "Gestionando materias del calendario."
        }

        findViewById<Button>(R.id.btnMostrarEventos).setOnClickListener {
            mostrarSoloPanel(panelEventos)
            txtResultadoUsuario.text = "Gestionando eventos para la fecha $fechaSeleccionada."
        }

        findViewById<Button>(R.id.btnMostrarPlanes).setOnClickListener {
            mostrarSoloPanel(panelPlanes)
            txtResultadoUsuario.text = "Gestionando planes de estudio."
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun configurarBotonesDeAccion() {
        findViewById<Button>(R.id.btnGuardarUsuario).setOnClickListener {
            guardarUsuario()
        }

        findViewById<Button>(R.id.btnAgregarMateriaCalendario).setOnClickListener {
            agregarMateriaCalendario()
        }

        findViewById<Button>(R.id.btnEditarMateriaCalendario).setOnClickListener {
            editarMateriaCalendario()
        }

        findViewById<Button>(R.id.btnEliminarMateriaCalendario).setOnClickListener {
            eliminarMateriaCalendario()
        }

        findViewById<Button>(R.id.btnAgregarEvento).setOnClickListener {
            agregarEvento()
        }

        findViewById<Button>(R.id.btnEditarEvento).setOnClickListener {
            editarEvento()
        }

        findViewById<Button>(R.id.btnEliminarEvento).setOnClickListener {
            eliminarEvento()
        }

        findViewById<Button>(R.id.btnAgregarPlan).setOnClickListener {
            agregarPlan()
        }
    }

    private fun mostrarSoloPanel(panelSeleccionado: LinearLayout) {
        panelPerfil.visibility = View.GONE
        panelMaterias.visibility = View.GONE
        panelEventos.visibility = View.GONE
        panelPlanes.visibility = View.GONE

        panelSeleccionado.visibility = View.VISIBLE
    }

    private fun guardarUsuario() {
        val nombre = findViewById<EditText>(R.id.edtNombreUsuario).text.toString().trim()
        val apellido = findViewById<EditText>(R.id.edtApellidoUsuario).text.toString().trim()
        val edadTexto = findViewById<EditText>(R.id.edtEdadUsuario).text.toString().trim()
        val correo = findViewById<EditText>(R.id.edtCorreoUsuario).text.toString().trim()
        val nuevaContrasena = findViewById<EditText>(R.id.edtContrasenaUsuario).text.toString().trim()

        if (codEstUpb.isEmpty()) {
            txtResultadoUsuario.text = "Error: no se recibió el código UPB del usuario."
            return
        }

        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || edadTexto.isEmpty()) {
            txtResultadoUsuario.text = "Completa nombre, apellido, edad y correo."
            return
        }

        val edad = edadTexto.toIntOrNull()

        if (edad == null) {
            txtResultadoUsuario.text = "La edad debe ser un número."
            return
        }

        if (idCarreraActual == 0) {
            txtResultadoUsuario.text = "Error: no se recibió la carrera del usuario."
            return
        }

        val contrasenaFinal = if (nuevaContrasena.isEmpty()) {
            contrasenaActual
        } else {
            nuevaContrasena
        }

        if (contrasenaFinal.isEmpty()) {
            txtResultadoUsuario.text = "Debes ingresar una contraseña."
            return
        }

        val request = UsuarioRequest(
            apellido = apellido,
            cod_est_upb = codEstUpb,
            contrasena = contrasenaFinal,
            correo = correo,
            edad = edad,
            id_carrera = idCarreraActual,
            nombre = nombre
        )

        txtResultadoUsuario.text = "Guardando cambios..."

        lifecycleScope.launch {
            try {
                val response = api.actualizarUsuario(codEstUpb, request)

                if (response.isSuccessful) {
                    txtResultadoUsuario.text = "Usuario actualizado correctamente."

                    txtBienvenidaUsuario.text = "Bienvenido\n$nombre $apellido"

                    contrasenaActual = contrasenaFinal

                    Toast.makeText(
                        this@UsuarioActivity,
                        "Datos guardados",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorBackend = response.errorBody()?.string()

                    txtResultadoUsuario.text =
                        "Error al actualizar usuario: ${response.code()}\n$errorBackend"
                }

            } catch (e: Exception) {
                txtResultadoUsuario.text = "Error: ${e.message}"
            }
        }
    }

    private fun agregarMateriaCalendario() {
        val materia = findViewById<EditText>(R.id.edtNombreMateriaCalendario).text.toString().trim()
        val hora = findViewById<EditText>(R.id.edtHoraMateriaCalendario).text.toString().trim()
        val aula = findViewById<EditText>(R.id.edtAulaMateriaCalendario).text.toString().trim()

        if (materia.isEmpty() || hora.isEmpty()) {
            txtResultadoUsuario.text = "Completa la materia y la hora."
            return
        }

        txtResultadoUsuario.text =
            "Materia agregada visualmente:\nMateria: $materia\nFecha: $fechaSeleccionada\nHora: $hora\nAula: $aula"

        Toast.makeText(this, "Luego conectamos agregar materia al API", Toast.LENGTH_SHORT).show()
    }

    private fun editarMateriaCalendario() {
        val id = findViewById<EditText>(R.id.edtIdMateriaCalendario).text.toString().trim()

        if (id.isEmpty()) {
            txtResultadoUsuario.text = "Ingresa el ID de la materia calendario para editar."
            return
        }

        txtResultadoUsuario.text = "Materia calendario con ID $id lista para editar."
        Toast.makeText(this, "Luego conectamos editar materia al API", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarMateriaCalendario() {
        val id = findViewById<EditText>(R.id.edtIdMateriaCalendario).text.toString().trim()

        if (id.isEmpty()) {
            txtResultadoUsuario.text = "Ingresa el ID de la materia calendario para eliminar."
            return
        }

        txtResultadoUsuario.text = "Materia calendario con ID $id lista para eliminar."
        Toast.makeText(this, "Luego conectamos eliminar materia al API", Toast.LENGTH_SHORT).show()
    }

    private fun agregarEvento() {
        val titulo = findViewById<EditText>(R.id.edtTituloEvento).text.toString().trim()
        val descripcion = findViewById<EditText>(R.id.edtDescripcionEvento).text.toString().trim()
        val hora = findViewById<EditText>(R.id.edtHoraEvento).text.toString().trim()

        if (titulo.isEmpty()) {
            txtResultadoUsuario.text = "Completa el título del evento."
            return
        }

        txtResultadoUsuario.text =
            "Evento agregado visualmente:\n$titulo\nFecha: $fechaSeleccionada\nHora: $hora\nDescripción: $descripcion"

        Toast.makeText(this, "Luego conectamos agregar evento al API", Toast.LENGTH_SHORT).show()
    }

    private fun editarEvento() {
        val id = findViewById<EditText>(R.id.edtIdEvento).text.toString().trim()

        if (id.isEmpty()) {
            txtResultadoUsuario.text = "Ingresa el ID del evento para editar."
            return
        }

        txtResultadoUsuario.text = "Evento con ID $id listo para editar."
        Toast.makeText(this, "Luego conectamos editar evento al API", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarEvento() {
        val id = findViewById<EditText>(R.id.edtIdEvento).text.toString().trim()

        if (id.isEmpty()) {
            txtResultadoUsuario.text = "Ingresa el ID del evento para eliminar."
            return
        }

        txtResultadoUsuario.text = "Evento con ID $id listo para eliminar."
        Toast.makeText(this, "Luego conectamos eliminar evento al API", Toast.LENGTH_SHORT).show()
    }

    private fun agregarPlan() {
        val nombrePlan = findViewById<EditText>(R.id.edtNombrePlan).text.toString().trim()
        val materia = findViewById<EditText>(R.id.edtMateriaPlan).text.toString().trim()
        val horas = findViewById<EditText>(R.id.edtHorasPlan).text.toString().trim()
        val objetivo = findViewById<EditText>(R.id.edtObjetivoPlan).text.toString().trim()

        if (nombrePlan.isEmpty() || materia.isEmpty() || horas.isEmpty()) {
            txtResultadoUsuario.text = "Completa nombre del plan, materia y horas."
            return
        }

        txtResultadoUsuario.text =
            "Plan de estudio creado visualmente:\nPlan: $nombrePlan\nMateria: $materia\nHoras por día: $horas\nObjetivo: $objetivo"

        Toast.makeText(this, "Luego conectamos plan de estudio al API", Toast.LENGTH_SHORT).show()
    }
}