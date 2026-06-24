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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

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

    private lateinit var spMateriasDisponibles: Spinner
    private lateinit var txtHorarioMateriaSeleccionada: TextView
    private lateinit var txtMateriasDelDia: TextView

    private var listaMaterias = mutableListOf<MateriaResponse>()
    private var listaHorarios = mutableListOf<HorarioMateriaResponse>()
    private var listaInscripciones = mutableListOf<InscripcionesResponse>()

    private var materiaSeleccionada: MateriaResponse? = null

    private data class EventoLocal(
        val fecha: String,
        val titulo: String,
        val tipo: String,
        val descripcion: String,
        val hora: String
    )

    private val listaEventosLocales = mutableListOf<EventoLocal>()

    private lateinit var spNivelPlan: Spinner

    private data class SesionEstudioLocal(
        val fecha: String,
        val nombrePlan: String,
        val materia: String,
        val objetivo: String,
        val nivel: String,
        val horaInicio: String,
        val horaFin: String
    )

    private val listaSesionesEstudio = mutableListOf<SesionEstudioLocal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario)

        inicializarVistas()
        cargarDatosUsuario()
        configurarCalendario()
        configurarBotonesPrincipales()
        configurarBotonesDeAccion()
        configurarSpinnerNivelPlan()
        cargarDatosMateriasCalendario()

    }

    private fun inicializarVistas() {
        txtBienvenidaUsuario = findViewById(R.id.txtBienvenidaUsuario)
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada)
        txtResultadoUsuario = findViewById(R.id.txtResultadoUsuario)

        panelPerfil = findViewById(R.id.panelPerfil)
        panelMaterias = findViewById(R.id.panelMaterias)
        panelEventos = findViewById(R.id.panelEventos)
        panelPlanes = findViewById(R.id.panelPlanes)
        spMateriasDisponibles = findViewById(R.id.spMateriasDisponibles)
        txtHorarioMateriaSeleccionada = findViewById(R.id.txtHorarioMateriaSeleccionada)
        txtMateriasDelDia = findViewById(R.id.txtMateriasDelDia)
        spNivelPlan = findViewById(R.id.spNivelPlan)
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
        actualizarMateriasDelDia()

        val txtFechaEvento = findViewById<TextView?>(R.id.txtFechaEvento)
        txtFechaEvento?.text = "Evento para la fecha: $fechaSeleccionada"

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
            actualizarMateriasDelDia()

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

        findViewById<Button>(R.id.btnVerDocumentos).setOnClickListener {
            val intent = Intent(this, DocumentosActivity::class.java)
            intent.putExtra("cod_est_upb", codEstUpb)
            startActivity(intent)
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

        //findViewById<Button>(R.id.btnEditarMateriaCalendario).setOnClickListener {
            //editarMateriaCalendario()
        //}

        //findViewById<Button>(R.id.btnEliminarMateriaCalendario).setOnClickListener {
            //eliminarMateriaCalendario()
        //}

        findViewById<Button>(R.id.btnAgregarEvento).setOnClickListener {
            agregarEvento()
        }

//        findViewById<Button>(R.id.btnEditarEvento).setOnClickListener {
//            editarEvento()
//        }
//
//        findViewById<Button>(R.id.btnEliminarEvento).setOnClickListener {
//            eliminarEvento()
//        }

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

    private fun configurarSpinnerNivelPlan() {
        val niveles = listOf(
            "Aprobar",
            "Buena nota",
            "Modo tryhard"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            niveles
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spNivelPlan.adapter = adapter
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
        val materia = materiaSeleccionada

        if (materia == null) {
            txtResultadoUsuario.text = "Primero selecciona una materia."
            return
        }

        if (codEstUpb.isEmpty()) {
            txtResultadoUsuario.text = "Error: no se recibió el código UPB del estudiante."
            return
        }

        val yaExiste = listaInscripciones.any {
            it.cod_est_upb == codEstUpb && it.id_materia == materia.id_materia
        }

        if (yaExiste) {
            txtResultadoUsuario.text = "Esta materia ya está agregada a tu calendario."
            return
        }

        txtResultadoUsuario.text = "Agregando materia al calendario..."

        lifecycleScope.launch {
            try {
                val response = api.crearInscripcion(
                    fecha_inscripcion = fechaSeleccionada,
                    estado = "Activa",
                    nota_final = "0",
                    cod_est_upb = codEstUpb,
                    id_materia = materia.id_materia
                )

                if (response.isSuccessful) {
                    val fechaFin = calcularFechaFinSinFinesDeSemana(
                        fechaSeleccionada,
                        materia.duracion_semanas
                    )

                    txtResultadoUsuario.text =
                        "Materia agregada correctamente.\nInicio: $fechaSeleccionada\nFin estimado: $fechaFin"

                    Toast.makeText(
                        this@UsuarioActivity,
                        "Materia agregada",
                        Toast.LENGTH_SHORT
                    ).show()

                    cargarDatosMateriasCalendario()
                } else {
                    val errorBackend = response.errorBody()?.string()
                    txtResultadoUsuario.text =
                        "Error al agregar materia: ${response.code()}\n$errorBackend"
                }

            } catch (e: Exception) {
                txtResultadoUsuario.text = "Error: ${e.message}"
            }
        }
    }

    private fun editarMateriaCalendario() {
        txtResultadoUsuario.text = "Editar materia del calendario lo conectamos después."
        Toast.makeText(this, "Luego conectamos editar materia", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarMateriaCalendario() {
        txtResultadoUsuario.text = "Eliminar materia del calendario lo conectamos después."
        Toast.makeText(this, "Luego conectamos eliminar materia", Toast.LENGTH_SHORT).show()
    }

    private fun agregarEvento() {
        val titulo = findViewById<EditText>(R.id.edtTituloEvento).text.toString().trim()
        val tipo = findViewById<EditText>(R.id.edtTipoEvento).text.toString().trim()
        val descripcion = findViewById<EditText>(R.id.edtDescripcionEvento).text.toString().trim()
        val hora = findViewById<EditText>(R.id.edtHoraEvento).text.toString().trim()

        if (titulo.isEmpty()) {
            txtResultadoUsuario.text = "Completa el título del evento."
            return
        }

        val evento = EventoLocal(
            fecha = fechaSeleccionada,
            titulo = titulo,
            tipo = if (tipo.isEmpty()) "recordatorio" else tipo,
            descripcion = descripcion,
            hora = hora
        )

        listaEventosLocales.add(evento)

        txtResultadoUsuario.text = "Evento agregado para $fechaSeleccionada."

        Toast.makeText(
            this,
            "Evento agregado al calendario",
            Toast.LENGTH_SHORT
        ).show()

        findViewById<EditText>(R.id.edtTituloEvento).setText("")
        findViewById<EditText>(R.id.edtTipoEvento).setText("")
        findViewById<EditText>(R.id.edtDescripcionEvento).setText("")
        findViewById<EditText>(R.id.edtHoraEvento).setText("")

        actualizarMateriasDelDia()
    }

    private fun editarEvento() {
        txtResultadoUsuario.text = "Editar evento lo conectamos después."
        Toast.makeText(this, "Luego conectamos editar evento", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarEvento() {
        txtResultadoUsuario.text = "Eliminar evento lo conectamos después."
        Toast.makeText(this, "Luego conectamos eliminar evento", Toast.LENGTH_SHORT).show()
    }

    private fun agregarPlan() {
        val nombrePlan = findViewById<EditText>(R.id.edtNombrePlan).text.toString().trim()
        val materia = findViewById<EditText>(R.id.edtMateriaPlan).text.toString().trim()
        val fechaInicioTexto = findViewById<EditText>(R.id.edtFechaInicioPlan).text.toString().trim()
        val fechaFinTexto = findViewById<EditText>(R.id.edtFechaFinPlan).text.toString().trim()
        val horasTexto = findViewById<EditText>(R.id.edtHorasPlan).text.toString().trim()
        val objetivo = findViewById<EditText>(R.id.edtObjetivoPlan).text.toString().trim()
        val nivel = spNivelPlan.selectedItem.toString()

        if (nombrePlan.isEmpty() || materia.isEmpty() || fechaInicioTexto.isEmpty() || fechaFinTexto.isEmpty() || horasTexto.isEmpty()) {
            txtResultadoUsuario.text = "Completa nombre, materia, fechas y horas."
            return
        }

        val fechaInicio = normalizarFechaPlan(fechaInicioTexto)
        val fechaFin = normalizarFechaPlan(fechaFinTexto)

        if (fechaInicio == null || fechaFin == null) {
            txtResultadoUsuario.text = "Las fechas deben tener formato 20260601."
            return
        }

        val horasPorDia = horasTexto.toIntOrNull()

        if (horasPorDia == null || horasPorDia <= 0) {
            txtResultadoUsuario.text = "Las horas por día deben ser un número mayor a 0."
            return
        }

        if (fechaMayorQue(fechaInicio, fechaFin)) {
            txtResultadoUsuario.text = "La fecha de inicio no puede ser mayor que la fecha fin."
            return
        }

        val sesionesGeneradas = generarSesionesPlanEstudio(
            nombrePlan = nombrePlan,
            materia = materia,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            horasPorDia = horasPorDia,
            objetivo = objetivo,
            nivel = nivel
        )

        if (sesionesGeneradas.isEmpty()) {
            txtResultadoUsuario.text = "No se pudieron generar sesiones dentro del rango indicado."
            return
        }

        listaSesionesEstudio.addAll(sesionesGeneradas)

        txtResultadoUsuario.text =
            "Plan creado correctamente.\nNivel: $nivel\nSesiones generadas: ${sesionesGeneradas.size}"

        Toast.makeText(
            this,
            "Plan de estudio agregado",
            Toast.LENGTH_SHORT
        ).show()

        findViewById<EditText>(R.id.edtNombrePlan).setText("")
        findViewById<EditText>(R.id.edtMateriaPlan).setText("")
        findViewById<EditText>(R.id.edtFechaInicioPlan).setText("")
        findViewById<EditText>(R.id.edtFechaFinPlan).setText("")
        findViewById<EditText>(R.id.edtHorasPlan).setText("")
        findViewById<EditText>(R.id.edtObjetivoPlan).setText("")

        actualizarMateriasDelDia()
    }

    private fun cargarDatosMateriasCalendario() {
        lifecycleScope.launch {
            try {
                val responseMaterias = api.getMaterias()
                val responseHorarios = api.getHorarioMateria()
                val responseInscripciones = api.getInscripciones()

                if (responseMaterias.isSuccessful && responseHorarios.isSuccessful && responseInscripciones.isSuccessful) {
                    listaMaterias = responseMaterias.body()?.data?.toMutableList() ?: mutableListOf()
                    listaHorarios = responseHorarios.body()?.data?.toMutableList() ?: mutableListOf()

                    val todasLasInscripciones = responseInscripciones.body()?.data ?: emptyList()

                    listaInscripciones = todasLasInscripciones
                        .filter { it.cod_est_upb == codEstUpb }
                        .toMutableList()

                    cargarSpinnerMaterias()
                    actualizarMateriasDelDia()

                    txtResultadoUsuario.text = "Materias y horarios cargados correctamente."
                } else {
                    txtResultadoUsuario.text =
                        "Error cargando materias u horarios."
                }

            } catch (e: Exception) {
                txtResultadoUsuario.text = "Error cargando materias: ${e.message}"
            }
        }
    }

    private fun cargarSpinnerMaterias() {
        if (listaMaterias.isEmpty()) {
            txtHorarioMateriaSeleccionada.text = "No hay materias registradas en la base de datos."
            return
        }

        val nombresMaterias = listaMaterias.map {
            "${it.id_materia} - ${it.nombre}"
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresMaterias
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spMateriasDisponibles.adapter = adapter

        spMateriasDisponibles.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                materiaSeleccionada = listaMaterias[position]
                mostrarHorarioMateriaSeleccionada()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                materiaSeleccionada = null
            }
        }
    }

    private fun mostrarHorarioMateriaSeleccionada() {
        val materia = materiaSeleccionada

        if (materia == null) {
            txtHorarioMateriaSeleccionada.text = "Selecciona una materia."
            return
        }

        val horariosMateria = listaHorarios.filter {
            it.id_materia == materia.id_materia
        }

        val fechaFin = calcularFechaFinSinFinesDeSemana(
            fechaSeleccionada,
            materia.duracion_semanas
        )

        val texto = StringBuilder()
        texto.append("Materia: ${materia.nombre}\n")
        texto.append("Inicio: $fechaSeleccionada\n")
        texto.append("Fin estimado: $fechaFin\n")
        texto.append("Duración: ${materia.duracion_semanas} semanas\n\n")

        if (horariosMateria.isEmpty()) {
            texto.append("Esta materia no tiene horario/aula registrado.")
        } else {
            texto.append("Horario y aula:\n\n")

            horariosMateria.forEach {
                texto.append("Hora: ${it.hora_inicio} - ${it.hora_fin}\n")
                texto.append("Aula: ${it.aula}\n\n")
            }
        }

        txtHorarioMateriaSeleccionada.text = texto.toString()
    }

    private fun actualizarMateriasDelDia() {
        if (!::txtMateriasDelDia.isInitialized) {
            return
        }

        val textoDia = StringBuilder()
        textoDia.append("Resumen del día seleccionado:\n\n")

        var hayAlgo = false

        // ======================
        // MATERIAS DEL DÍA
        // ======================
        if (listaInscripciones.isNotEmpty() && !esFinDeSemana(fechaSeleccionada)) {
            listaInscripciones.forEach { inscripcion ->

                val materia = listaMaterias.firstOrNull {
                    it.id_materia == inscripcion.id_materia
                }

                if (materia != null) {
                    val estaEnRango = fechaDentroDelRangoMateria(
                        fechaSeleccionada = fechaSeleccionada,
                        fechaInicio = inscripcion.fecha_inscripcion,
                        duracionSemanas = materia.duracion_semanas
                    )

                    if (estaEnRango) {
                        val horariosMateria = listaHorarios.filter {
                            it.id_materia == materia.id_materia
                        }

                        textoDia.append("📘 Materia: ${materia.nombre}\n")

                        if (horariosMateria.isEmpty()) {
                            textoDia.append("Horario: no registrado\n")
                            textoDia.append("Aula: no registrada\n\n")
                        } else {
                            horariosMateria.forEach { horario ->
                                textoDia.append("Hora: ${horario.hora_inicio} - ${horario.hora_fin}\n")
                                textoDia.append("Aula: ${horario.aula}\n\n")
                            }
                        }

                        hayAlgo = true
                    }
                }
            }
        }

        // ======================
        // EVENTOS LOCALES DEL DÍA
        // ======================
        val eventosDelDia = listaEventosLocales.filter {
            it.fecha == fechaSeleccionada
        }

        if (eventosDelDia.isNotEmpty()) {
            textoDia.append("📝 Eventos:\n\n")

            eventosDelDia.forEach { evento ->
                textoDia.append("Tienes \"${evento.titulo}\" este día.\n")
                textoDia.append("Tipo: ${evento.tipo}\n")

                if (evento.hora.isNotEmpty()) {
                    textoDia.append("Hora: ${evento.hora}\n")
                }

                if (evento.descripcion.isNotEmpty()) {
                    textoDia.append("Detalle: ${evento.descripcion}\n")
                }

                textoDia.append("\n")
            }

            hayAlgo = true
        }

        // ======================
// PLANES DE ESTUDIO DEL DÍA
// ======================
        val sesionesDelDia = listaSesionesEstudio.filter {
            it.fecha == fechaSeleccionada
        }

        if (sesionesDelDia.isNotEmpty()) {
            textoDia.append("📚 Planes de estudio:\n\n")

            sesionesDelDia.forEach { sesion ->
                textoDia.append("Plan: ${sesion.nombrePlan}\n")
                textoDia.append("Materia/Tema: ${sesion.materia}\n")
                textoDia.append("Nivel: ${sesion.nivel}\n")
                textoDia.append("Horario: ${sesion.horaInicio} - ${sesion.horaFin}\n")

                if (sesion.objetivo.isNotEmpty()) {
                    textoDia.append("Objetivo: ${sesion.objetivo}\n")
                }

                textoDia.append("\n")
            }

            hayAlgo = true
        }

        if (!hayAlgo) {
            txtMateriasDelDia.text = "No tienes materias ni eventos para esta fecha."
            txtFechaSeleccionada.text = "Fecha seleccionada: $fechaSeleccionada"
        } else {
            txtMateriasDelDia.text = textoDia.toString()
            txtFechaSeleccionada.text =
                "Fecha seleccionada: $fechaSeleccionada\n📌 Tienes actividades este día"
        }
    }

    private fun obtenerDiaSemana(fecha: String): String {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaDate = formato.parse(fecha)

            val calendario = Calendar.getInstance()
            calendario.time = fechaDate!!

            when (calendario.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "lunes"
                Calendar.TUESDAY -> "martes"
                Calendar.WEDNESDAY -> "miércoles"
                Calendar.THURSDAY -> "jueves"
                Calendar.FRIDAY -> "viernes"
                Calendar.SATURDAY -> "sábado"
                Calendar.SUNDAY -> "domingo"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun normalizarTexto(texto: String): String {
        return texto
            .lowercase(Locale.getDefault())
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()
    }

    private fun fechaDentroDelRangoMateria(
        fechaSeleccionada: String,
        fechaInicio: String,
        duracionSemanas: Int
    ): Boolean {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val fechaSeleccionadaDate = formato.parse(fechaSeleccionada)
            val fechaInicioDate = formato.parse(fechaInicio)

            if (fechaSeleccionadaDate == null || fechaInicioDate == null) {
                return false
            }

            val fechaFinTexto = calcularFechaFinSinFinesDeSemana(fechaInicio, duracionSemanas)
            val fechaFinDate = formato.parse(fechaFinTexto)

            if (fechaFinDate == null) {
                return false
            }

            !fechaSeleccionadaDate.before(fechaInicioDate) &&
                    !fechaSeleccionadaDate.after(fechaFinDate)

        } catch (e: Exception) {
            false
        }
    }

    private fun calcularFechaFinSinFinesDeSemana(
        fechaInicio: String,
        duracionSemanas: Int
    ): String {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = formato.parse(fechaInicio)

            val calendario = Calendar.getInstance()
            calendario.time = fecha!!

            val diasHabilesNecesarios = duracionSemanas * 5
            var diasHabilesContados = 0

            while (diasHabilesContados < diasHabilesNecesarios) {
                val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)

                if (diaSemana != Calendar.SATURDAY && diaSemana != Calendar.SUNDAY) {
                    diasHabilesContados++
                }

                if (diasHabilesContados < diasHabilesNecesarios) {
                    calendario.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            formato.format(calendario.time)

        } catch (e: Exception) {
            fechaInicio
        }
    }

    private fun esFinDeSemana(fecha: String): Boolean {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaDate = formato.parse(fecha)

            val calendario = Calendar.getInstance()
            calendario.time = fechaDate!!

            val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)

            diaSemana == Calendar.SATURDAY || diaSemana == Calendar.SUNDAY

        } catch (e: Exception) {
            false
        }
    }

    private fun generarSesionesPlanEstudio(
        nombrePlan: String,
        materia: String,
        fechaInicio: String,
        fechaFin: String,
        horasPorDia: Int,
        objetivo: String,
        nivel: String
    ): List<SesionEstudioLocal> {

        val sesiones = mutableListOf<SesionEstudioLocal>()

        val diasPorSemana = when (nivel) {
            "Aprobar" -> 2
            "Buena nota" -> 4
            "Modo tryhard" -> 5
            else -> 2
        }

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val inicio = formato.parse(fechaInicio)
        val fin = formato.parse(fechaFin)

        if (inicio == null || fin == null) {
            return sesiones
        }

        val calendario = Calendar.getInstance()
        calendario.time = inicio

        var semanaActual = obtenerClaveSemana(formato.format(calendario.time))
        var diasUsadosEstaSemana = 0

        while (!calendario.time.after(fin)) {
            val fechaActual = formato.format(calendario.time)

            if (!esFinDeSemana(fechaActual)) {
                val claveSemana = obtenerClaveSemana(fechaActual)

                if (claveSemana != semanaActual) {
                    semanaActual = claveSemana
                    diasUsadosEstaSemana = 0
                }

                if (diasUsadosEstaSemana < diasPorSemana) {
                    val horaInicio = encontrarPrimerHorarioLibre(fechaActual, horasPorDia)
                    val horaFin = calcularHoraFin(horaInicio, horasPorDia)

                    sesiones.add(
                        SesionEstudioLocal(
                            fecha = fechaActual,
                            nombrePlan = nombrePlan,
                            materia = materia,
                            objetivo = objetivo,
                            nivel = nivel,
                            horaInicio = horaInicio,
                            horaFin = horaFin
                        )
                    )

                    diasUsadosEstaSemana++
                }
            }

            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }

        return sesiones
    }

    private fun obtenerClaveSemana(fecha: String): String {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaDate = formato.parse(fecha)

            val calendario = Calendar.getInstance()
            calendario.time = fechaDate!!

            val anio = calendario.get(Calendar.YEAR)
            val semana = calendario.get(Calendar.WEEK_OF_YEAR)

            "$anio-$semana"
        } catch (e: Exception) {
            ""
        }
    }

    private fun encontrarPrimerHorarioLibre(fecha: String, horasPorDia: Int): String {
        val horariosCandidatos = listOf(
            "07:00",
            "08:00",
            "09:00",
            "10:00",
            "11:00",
            "14:00",
            "15:00",
            "16:00",
            "17:00",
            "18:00",
            "19:00",
            "20:00"
        )

        for (hora in horariosCandidatos) {
            val inicio = convertirHoraAMinutos(hora)
            val fin = inicio + (horasPorDia * 60)

            if (!hayConflictoHorario(fecha, inicio, fin)) {
                return hora
            }
        }

        return "20:00"
    }

    private fun hayConflictoHorario(fecha: String, inicioNuevo: Int, finNuevo: Int): Boolean {
        // Conflicto con sesiones de estudio ya generadas
        listaSesionesEstudio
            .filter { it.fecha == fecha }
            .forEach { sesion ->
                val inicioExistente = convertirHoraAMinutos(sesion.horaInicio)
                val finExistente = convertirHoraAMinutos(sesion.horaFin)

                if (horariosSeCruzan(inicioNuevo, finNuevo, inicioExistente, finExistente)) {
                    return true
                }
            }

        // Conflicto simple con eventos locales si tienen hora
        listaEventosLocales
            .filter { it.fecha == fecha && it.hora.isNotEmpty() }
            .forEach { evento ->
                val inicioEvento = convertirHoraAMinutos(evento.hora)
                val finEvento = inicioEvento + 60

                if (horariosSeCruzan(inicioNuevo, finNuevo, inicioEvento, finEvento)) {
                    return true
                }
            }

        // Conflicto con materias del calendario si están activas ese día
        listaInscripciones.forEach { inscripcion ->
            val materia = listaMaterias.firstOrNull {
                it.id_materia == inscripcion.id_materia
            }

            if (materia != null) {
                val estaEnRango = fechaDentroDelRangoMateria(
                    fechaSeleccionada = fecha,
                    fechaInicio = inscripcion.fecha_inscripcion,
                    duracionSemanas = materia.duracion_semanas
                )

                if (estaEnRango && !esFinDeSemana(fecha)) {
                    val horariosMateria = listaHorarios.filter {
                        it.id_materia == materia.id_materia
                    }

                    horariosMateria.forEach { horario ->
                        val inicioMateria = convertirHoraAMinutos(horario.hora_inicio)
                        val finMateria = convertirHoraAMinutos(horario.hora_fin)

                        if (horariosSeCruzan(inicioNuevo, finNuevo, inicioMateria, finMateria)) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun horariosSeCruzan(
        inicioA: Int,
        finA: Int,
        inicioB: Int,
        finB: Int
    ): Boolean {
        return inicioA < finB && finA > inicioB
    }

    private fun convertirHoraAMinutos(hora: String): Int {
        return try {
            val partes = hora.split(":")
            val horas = partes[0].toInt()
            val minutos = partes.getOrNull(1)?.toInt() ?: 0

            horas * 60 + minutos
        } catch (e: Exception) {
            0
        }
    }

    private fun calcularHoraFin(horaInicio: String, horasPorDia: Int): String {
        val minutosInicio = convertirHoraAMinutos(horaInicio)
        val minutosFin = minutosInicio + (horasPorDia * 60)

        val horas = minutosFin / 60
        val minutos = minutosFin % 60

        return String.format(Locale.getDefault(), "%02d:%02d", horas, minutos)
    }

    private fun fechaValida(fecha: String): Boolean {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formato.isLenient = false
            formato.parse(fecha)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun fechaMayorQue(fechaInicio: String, fechaFin: String): Boolean {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val inicio = formato.parse(fechaInicio)
            val fin = formato.parse(fechaFin)

            if (inicio == null || fin == null) {
                return true
            }

            inicio.after(fin)
        } catch (e: Exception) {
            true
        }
    }

    private fun normalizarFechaPlan(fecha: String): String? {
        return try {
            if (fecha.length != 8) {
                return null
            }

            val anio = fecha.substring(0, 4)
            val mes = fecha.substring(4, 6)
            val dia = fecha.substring(6, 8)

            val fechaFormateada = "$anio-$mes-$dia"

            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formato.isLenient = false

            formato.parse(fechaFormateada)

            fechaFormateada

        } catch (e: Exception) {
            null
        }
    }

}
