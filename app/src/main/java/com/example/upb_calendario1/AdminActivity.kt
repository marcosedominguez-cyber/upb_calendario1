package com.example.upb_calendario1

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.appcompat.app.AlertDialog

class AdminActivity : AppCompatActivity() {

    private lateinit var toolbarAdmin: Toolbar
    private lateinit var tvSubjectCount: TextView
    private lateinit var rvSubjects: RecyclerView
    private lateinit var etSearch: TextView

    private val api = ApiService.RetrofitClient.create()

    private var idAdmin: Int = 1
    private var nombreAdmin: String = "Administrador"

    private var listaMaterias = mutableListOf<MateriaResponse>()
    private var listaMateriasFiltradas = mutableListOf<MateriaResponse>()
    private var listaHorarios = mutableListOf<HorarioMateriaResponse>()
    private var listaModulos = mutableListOf<ModuloResponse>()
    private var listaCarreras = mutableListOf<CarreraResponse>()

    private lateinit var adapter: MateriaAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        idAdmin = intent.getIntExtra("id_admin", 1)
        nombreAdmin = intent.getStringExtra("nombre") ?: "Administrador"

        inicializarVistas()
        configurarRecycler()
        configurarBotones()
        cargarDatosAdmin()
    }

    private fun inicializarVistas() {
        toolbarAdmin = findViewById(R.id.toolbar_admin)
        tvSubjectCount = findViewById(R.id.tv_subject_count)
        rvSubjects = findViewById(R.id.rv_subjects)
        etSearch = findViewById(R.id.et_search)

        setSupportActionBar(toolbarAdmin)

        supportActionBar?.title = ""
        toolbarAdmin.title = ""
        toolbarAdmin.subtitle = ""

        toolbarAdmin.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarRecycler() {
        adapter = MateriaAdminAdapter(
            materias = listaMateriasFiltradas,
            horarios = listaHorarios,
            modulos = listaModulos,

            onEditar = { materia ->
                mostrarDialogEditarMateria(materia)
            },
            onEliminar = { materia ->
                confirmarEliminarMateria(materia)
            }
        )

        rvSubjects.layoutManager = LinearLayoutManager(this)
        rvSubjects.adapter = adapter
    }

    private fun configurarBotones() {
        findViewById<View>(R.id.fab_add_subject).setOnClickListener {
            mostrarDialogNuevaMateria()
        }

        findViewById<View>(R.id.btn_filter).setOnClickListener {
            Toast.makeText(this, "Filtro avanzado lo hacemos después", Toast.LENGTH_SHORT).show()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarMaterias(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarDatosAdmin() {
        tvSubjectCount.text = "Cargando materias..."

        lifecycleScope.launch {
            try {
                val responseMaterias = api.getMaterias()
                val responseHorarios = api.getHorarioMateria()
                val responseModulos = api.getModulos()
                val responseCarreras = api.getCarreras()

                if (
                    responseMaterias.isSuccessful &&
                    responseHorarios.isSuccessful &&
                    responseModulos.isSuccessful &&
                    responseCarreras.isSuccessful
                ) {
                    listaMaterias = responseMaterias.body()?.data?.toMutableList() ?: mutableListOf()
                    listaHorarios = responseHorarios.body()?.data?.toMutableList() ?: mutableListOf()
                    listaModulos = responseModulos.body()?.data?.toMutableList() ?: mutableListOf()
                    listaCarreras = responseCarreras.body()?.data?.toMutableList() ?: mutableListOf()

                    listaMateriasFiltradas.clear()
                    listaMateriasFiltradas.addAll(listaMaterias)

                    actualizarContador()
                    adapter.actualizarDatos(listaMateriasFiltradas, listaHorarios, listaModulos)

                } else {
                    tvSubjectCount.text = "Error cargando datos del admin"
                }

            } catch (e: Exception) {
                tvSubjectCount.text = "Error: ${e.message}"
            }
        }
    }

    private fun filtrarMaterias(texto: String) {
        val busqueda = texto.lowercase(Locale.getDefault()).trim()

        listaMateriasFiltradas.clear()

        if (busqueda.isEmpty()) {
            listaMateriasFiltradas.addAll(listaMaterias)
        } else {
            listaMateriasFiltradas.addAll(
                listaMaterias.filter {
                    it.nombre.lowercase(Locale.getDefault()).contains(busqueda) ||
                            it.tipo_materia.lowercase(Locale.getDefault()).contains(busqueda)
                }
            )
        }

        actualizarContador()
        adapter.actualizarDatos(listaMateriasFiltradas, listaHorarios, listaModulos)
    }

    private fun actualizarContador() {
        tvSubjectCount.text = "${listaMateriasFiltradas.size} materias registradas"
    }

    private fun mostrarDialogNuevaMateria() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_edit_subject)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etNombre = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_subject_name)
        val etTipo = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_subject_type)
        val etModulo = dialog.findViewById<AutoCompleteTextView>(R.id.et_module)
        val etCreditos = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_credits)
        val etDuracionSemanas = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_duration_weeks)
        val etAula = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_classroom)
        val etHoraInicio = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_start_time)
        val etHoraFin = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_end_time)

        val btnCancelar = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_save_subject)

        tvDialogTitle.text = "Nueva Materia"

        cargarDropdownsDialog(etModulo)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val tipo = etTipo.text.toString().trim()
            val moduloTexto = etModulo.text.toString().trim()
            val creditosTexto = etCreditos.text.toString().trim()
            val duracionTexto = etDuracionSemanas.text.toString().trim()
            val aula = etAula.text.toString().trim()
            val horaInicio = etHoraInicio.text.toString().trim()
            val horaFin = etHoraFin.text.toString().trim()

            if (
                nombre.isEmpty() ||
                tipo.isEmpty() ||
                moduloTexto.isEmpty() ||
                creditosTexto.isEmpty() ||
                duracionTexto.isEmpty() ||
                aula.isEmpty() ||
                horaInicio.isEmpty() ||
                horaFin.isEmpty()
            ) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val creditos = creditosTexto.toIntOrNull()
            val duracionSemanas = duracionTexto.toIntOrNull()

            if (creditos == null || creditos <= 0) {
                Toast.makeText(this, "Créditos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (duracionSemanas == null || duracionSemanas <= 0) {
                Toast.makeText(this, "Duración en semanas inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!horaValida(horaInicio) || !horaValida(horaFin)) {
                Toast.makeText(this, "La hora debe tener formato HH:mm. Ej: 08:00", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!horaInicioMenorQueFin(horaInicio, horaFin)) {
                Toast.makeText(this, "La hora de inicio debe ser menor que la hora fin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val moduloSeleccionado = obtenerModuloSeleccionado(moduloTexto)

            if (moduloSeleccionado == null) {
                Toast.makeText(this, "Selecciona un módulo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarNuevaMateria(
                nombre = nombre,
                tipo = tipo,
                creditos = creditos,
                duracionSemanas = duracionSemanas,
                aula = aula,
                horaInicio = horaInicio,
                horaFin = horaFin,
                idModulo = moduloSeleccionado.id_modulo,
                dialog = dialog
            )
        }

        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun cargarDropdownsDialog(
        etModulo: AutoCompleteTextView
    ) {
        val nombresModulos = if (listaModulos.isEmpty()) {
            listOf("Módulo no registrado")
        } else {
            listaModulos.map { "${it.id_modulo} - ${it.nombre}" }
        }

        etModulo.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresModulos)
        )
    }

    private fun obtenerModuloSeleccionado(texto: String): ModuloResponse? {
        val idTexto = texto.substringBefore(" - ").trim()
        val idModulo = idTexto.toIntOrNull()

        return listaModulos.firstOrNull {
            it.id_modulo == idModulo
        }
    }

    private fun guardarNuevaMateria(
        nombre: String,
        tipo: String,
        creditos: Int,
        duracionSemanas: Int,
        aula: String,
        horaInicio: String,
        horaFin: String,
        idModulo: Int,
        dialog: Dialog
    ) {
        tvSubjectCount.text = "Guardando materia..."

        lifecycleScope.launch {
            try {
                val responseMateria = api.crearMateria(
                    nombre = nombre,
                    creditos = creditos,
                    duracion_semanas = duracionSemanas,
                    tipo_materia = tipo,
                    id_modulo = idModulo,
                    id_admin = idAdmin
                )

                if (responseMateria.isSuccessful) {
                    val materiaCreada = responseMateria.body()

                    if (materiaCreada == null) {
                        tvSubjectCount.text = "No se pudo obtener la materia creada"
                        return@launch
                    }

                    /*
                     * El campo dia_semana existe en la tabla horario_materia,
                     * pero para esta lógica lo ignoramos.
                     * Mandamos "General" solo para que el backend pueda guardar el horario.
                     */
                    val responseHorario = api.crearHorarioMateria(
                        dia_semana = "General",
                        hora_inicio = horaInicio,
                        hora_fin = horaFin,
                        aula = aula,
                        id_materia = materiaCreada.id_materia
                    )

                    if (responseHorario.isSuccessful) {
                        Toast.makeText(
                            this@AdminActivity,
                            "Materia creada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                        cargarDatosAdmin()
                    } else {
                        val errorBackend = responseHorario.errorBody()?.string()
                        tvSubjectCount.text =
                            "Materia creada, pero falló el horario: ${responseHorario.code()}\n$errorBackend"
                    }

                } else {
                    val errorBackend = responseMateria.errorBody()?.string()
                    tvSubjectCount.text =
                        "Error al crear materia: ${responseMateria.code()}\n$errorBackend"
                }

            } catch (e: Exception) {
                tvSubjectCount.text = "Error: ${e.message}"
            }
        }
    }

    private fun calcularHoraFin(horaInicio: String, horas: Int): String {
        val partes = horaInicio.split(":")
        val h = partes[0].toIntOrNull() ?: 8
        val m = partes.getOrNull(1)?.toIntOrNull() ?: 0

        val totalMinutos = (h * 60) + m + (horas * 60)

        val horaFinal = totalMinutos / 60
        val minutoFinal = totalMinutos % 60

        return String.format(Locale.getDefault(), "%02d:%02d", horaFinal, minutoFinal)
    }

    private fun actualizarMateriaYHorario(
        materia: MateriaResponse,
        horarioActual: HorarioMateriaResponse?,
        nombre: String,
        tipo: String,
        creditos: Int,
        duracionSemanas: Int,
        aula: String,
        horaInicio: String,
        horaFin: String,
        idModulo: Int,
        docTitulo: String,
        docTipo: String,
        docDescripcion: String,
        docUrl: String,
        dialog: Dialog
    ) {
        tvSubjectCount.text = "Actualizando materia..."

        val materiaRequest = MateriaRequest(
            nombre = nombre,
            creditos = creditos,
            duracion_semanas = duracionSemanas,
            tipo_materia = tipo,
            id_modulo = idModulo,
            id_admin = idAdmin
        )

        lifecycleScope.launch {
            try {
                val responseMateria = api.actualizarMateria(
                    materia.id_materia,
                    materiaRequest
                )

                if (responseMateria.isSuccessful) {

                    if (horarioActual != null) {
                        val horarioRequest = HorarioMateriaRequest(
                            dia_semana = "General",
                            hora_inicio = horaInicio,
                            hora_fin = horaFin,
                            aula = aula,
                            id_materia = materia.id_materia
                        )

                        val responseHorario = api.actualizarHorarioMateria(
                            horarioActual.id_horario,
                            horarioRequest
                        )

                        if (!responseHorario.isSuccessful) {
                            val errorBackend = responseHorario.errorBody()?.string()
                            tvSubjectCount.text =
                                "Materia actualizada, pero falló el horario: ${responseHorario.code()}\n$errorBackend"
                            return@launch
                        }

                    } else {
                        val responseHorarioNuevo = api.crearHorarioMateria(
                            dia_semana = "General",
                            hora_inicio = horaInicio,
                            hora_fin = horaFin,
                            aula = aula,
                            id_materia = materia.id_materia
                        )

                        if (!responseHorarioNuevo.isSuccessful) {
                            val errorBackend = responseHorarioNuevo.errorBody()?.string()
                            tvSubjectCount.text =
                                "Materia actualizada, pero no se pudo crear horario: ${responseHorarioNuevo.code()}\n$errorBackend"
                            return@launch
                        }
                    }

                    val documentoOk = crearDocumentoOpcional(
                        titulo = docTitulo,
                        tipo = docTipo,
                        descripcion = docDescripcion,
                        url = docUrl,
                        idMateria = materia.id_materia
                    )

                    if (!documentoOk) {
                        return@launch
                    }

                    Toast.makeText(
                        this@AdminActivity,
                        "Materia actualizada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                    cargarDatosAdmin()

                } else {
                    val errorBackend = responseMateria.errorBody()?.string()
                    tvSubjectCount.text =
                        "Error al actualizar materia: ${responseMateria.code()}\n$errorBackend"
                }

            } catch (e: Exception) {
                tvSubjectCount.text = "Error: ${e.message}"
            }
        }
    }

    private fun mostrarDialogEditarMateria(materia: MateriaResponse) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_edit_subject)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etNombre = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_subject_name)
        val etTipo = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_subject_type)
        val etModulo = dialog.findViewById<AutoCompleteTextView>(R.id.et_module)
        val etCreditos = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_credits)
        val etDuracionSemanas = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_duration_weeks)
        val etAula = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_classroom)
        val etHoraInicio = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_start_time)
        val etHoraFin = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_end_time)

        val etDocTitulo = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_doc_title)
        val etDocTipo = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_doc_type)
        val etDocUrl = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_doc_url)
        val etDocDescripcion = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_doc_description)

        val btnCancelar = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnGuardar = dialog.findViewById<Button>(R.id.btn_save_subject)

        val horarioActual = listaHorarios.firstOrNull {
            it.id_materia == materia.id_materia
        }

        val moduloActual = listaModulos.firstOrNull {
            it.id_modulo == materia.id_modulo
        }

        tvDialogTitle.text = "Editar Materia"

        etNombre.setText(materia.nombre)
        etTipo.setText(materia.tipo_materia)
        etCreditos.setText(materia.creditos.toString())
        etDuracionSemanas.setText(materia.duracion_semanas.toString())

        if (moduloActual != null) {
            etModulo.setText("${moduloActual.id_modulo} - ${moduloActual.nombre}", false)
        }

        if (horarioActual != null) {
            etAula.setText(horarioActual.aula)
            etHoraInicio.setText(horarioActual.hora_inicio)
            etHoraFin.setText(horarioActual.hora_fin)
        }

        cargarDropdownsDialog(etModulo)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val tipo = etTipo.text.toString().trim()
            val moduloTexto = etModulo.text.toString().trim()
            val creditosTexto = etCreditos.text.toString().trim()
            val duracionTexto = etDuracionSemanas.text.toString().trim()
            val aula = etAula.text.toString().trim()
            val horaInicio = etHoraInicio.text.toString().trim()
            val horaFin = etHoraFin.text.toString().trim()
            val docTitulo = etDocTitulo.text.toString().trim()
            val docTipo = etDocTipo.text.toString().trim()
            val docUrl = etDocUrl.text.toString().trim()
            val docDescripcion = etDocDescripcion.text.toString().trim()

            if (
                nombre.isEmpty() ||
                tipo.isEmpty() ||
                moduloTexto.isEmpty() ||
                creditosTexto.isEmpty() ||
                duracionTexto.isEmpty() ||
                aula.isEmpty() ||
                horaInicio.isEmpty() ||
                horaFin.isEmpty()
            ) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val creditos = creditosTexto.toIntOrNull()
            val duracionSemanas = duracionTexto.toIntOrNull()

            if (creditos == null || creditos <= 0) {
                Toast.makeText(this, "Créditos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (duracionSemanas == null || duracionSemanas <= 0) {
                Toast.makeText(this, "Duración en semanas inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!horaValida(horaInicio) || !horaValida(horaFin)) {
                Toast.makeText(this, "La hora debe tener formato HH:mm. Ej: 08:00", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!horaInicioMenorQueFin(horaInicio, horaFin)) {
                Toast.makeText(this, "La hora de inicio debe ser menor que la hora fin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val moduloSeleccionado = obtenerModuloSeleccionado(moduloTexto)

            if (moduloSeleccionado == null) {
                Toast.makeText(this, "Selecciona un módulo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actualizarMateriaYHorario(
                materia = materia,
                horarioActual = horarioActual,
                nombre = nombre,
                tipo = tipo,
                creditos = creditos,
                duracionSemanas = duracionSemanas,
                aula = aula,
                horaInicio = horaInicio,
                horaFin = horaFin,
                idModulo = moduloSeleccionado.id_modulo,
                docTitulo = docTitulo,
                docTipo = docTipo,
                docDescripcion = docDescripcion,
                docUrl = docUrl,
                dialog = dialog
            )
        }

        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun confirmarEliminarMateria(materia: MateriaResponse) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar materia")
            .setMessage("¿Seguro que deseas eliminar la materia ${materia.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMateriaCompleta(materia)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMateriaCompleta(materia: MateriaResponse) {
        tvSubjectCount.text = "Eliminando materia..."

        lifecycleScope.launch {
            try {
                // 1. Buscar documentos relacionados con la materia
                val responseDocumentos = api.getDocumentos()

                if (!responseDocumentos.isSuccessful) {
                    tvSubjectCount.text = "No se pudieron consultar los documentos."
                    return@launch
                }

                val documentosMateria = responseDocumentos.body()?.data
                    ?.filter { it.id_materia == materia.id_materia }
                    ?: emptyList()

                // 2. Eliminar documentos primero
                for (documento in documentosMateria) {
                    val responseDocumento = api.eliminarDocumento(documento.id_documento)

                    if (!responseDocumento.isSuccessful) {
                        val errorBackend = responseDocumento.errorBody()?.string()
                        tvSubjectCount.text =
                            "No se pudo eliminar un documento: ${responseDocumento.code()}\n$errorBackend"
                        return@launch
                    }
                }

                // 3. Eliminar horarios de esa materia
                val horariosMateria = listaHorarios.filter {
                    it.id_materia == materia.id_materia
                }

                for (horario in horariosMateria) {
                    val responseHorario = api.eliminarHorarioMateria(horario.id_horario)

                    if (!responseHorario.isSuccessful) {
                        val errorBackend = responseHorario.errorBody()?.string()
                        tvSubjectCount.text =
                            "No se pudo eliminar un horario: ${responseHorario.code()}\n$errorBackend"
                        return@launch
                    }
                }

                // 4. Eliminar la materia
                val responseMateria = api.eliminarMateria(materia.id_materia)

                if (responseMateria.isSuccessful) {
                    Toast.makeText(
                        this@AdminActivity,
                        "Materia eliminada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    cargarDatosAdmin()
                } else {
                    val errorBackend = responseMateria.errorBody()?.string()
                    tvSubjectCount.text =
                        "Error al eliminar materia: ${responseMateria.code()}\n$errorBackend"
                }

            } catch (e: Exception) {
                tvSubjectCount.text = "Error: ${e.message}"
            }
        }
    }

    class MateriaAdminAdapter(
        private var materias: MutableList<MateriaResponse>,
        private var horarios: MutableList<HorarioMateriaResponse>,
        private var modulos: MutableList<ModuloResponse>,
        private val onEditar: (MateriaResponse) -> Unit,
        private val onEliminar: (MateriaResponse) -> Unit
    ) : RecyclerView.Adapter<MateriaAdminAdapter.MateriaViewHolder>() {

        fun actualizarDatos(
            nuevasMaterias: MutableList<MateriaResponse>,
            nuevosHorarios: MutableList<HorarioMateriaResponse>,
            nuevosModulos: MutableList<ModuloResponse>
        ) {
            materias = nuevasMaterias
            horarios = nuevosHorarios
            modulos = nuevosModulos
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_subject_admin, parent, false)

            return MateriaViewHolder(view)
        }

        override fun getItemCount(): Int {
            return materias.size
        }

        override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
            val materia = materias[position]

            val horariosMateria = horarios.filter {
                it.id_materia == materia.id_materia
            }

            val modulo = modulos.firstOrNull {
                it.id_modulo == materia.id_modulo
            }

            holder.tvSubjectCode.text = materia.tipo_materia
            holder.tvSubjectName.text = materia.nombre
            holder.tvSubjectCareer.text = "Tipo: ${materia.tipo_materia}"
            holder.tvSubjectCredits.text = "${materia.creditos} Cred."

            holder.tvSubjectSemester.text = if (modulo != null) {
                "Módulo ${modulo.numero_modulo}"
            } else {
                "Módulo ${materia.id_modulo}"
            }

            if (horariosMateria.isNotEmpty()) {
                val primerHorario = horariosMateria.first()

                holder.tvSubjectClassroom.text = "Aula ${primerHorario.aula}"

                val dias = horariosMateria.joinToString(", ") {
                    it.dia_semana
                }

                holder.tvSubjectHours.text =
                    "Horario: ${primerHorario.hora_inicio} - ${primerHorario.hora_fin}"
            } else {
                holder.tvSubjectClassroom.text = "Sin aula"
                holder.tvSubjectHours.text = "Sin horario registrado"
            }

            holder.btnEditSubject.setOnClickListener {
                onEditar(materia)
            }

            holder.btnDeleteSubject.setOnClickListener {
                onEliminar(materia)
            }
        }

        class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvSubjectCode: TextView = itemView.findViewById(R.id.tv_subject_code)
            val tvSubjectName: TextView = itemView.findViewById(R.id.tv_subject_name)
            val tvSubjectCareer: TextView = itemView.findViewById(R.id.tv_subject_career)
            val tvSubjectCredits: TextView = itemView.findViewById(R.id.tv_subject_credits)
            val tvSubjectClassroom: TextView = itemView.findViewById(R.id.tv_subject_classroom)
            val tvSubjectSemester: TextView = itemView.findViewById(R.id.tv_subject_semester)
            val tvSubjectHours: TextView = itemView.findViewById(R.id.tv_subject_hours)
            val btnEditSubject: Button = itemView.findViewById(R.id.btn_edit_subject)
            val btnDeleteSubject: Button = itemView.findViewById(R.id.btn_delete_subject)
        }
    }
    private fun horaValida(hora: String): Boolean {
        return try {
            val partes = hora.split(":")

            if (partes.size != 2) {
                return false
            }

            val h = partes[0].toInt()
            val m = partes[1].toInt()

            h in 0..23 && m in 0..59

        } catch (e: Exception) {
            false
        }
    }

    private fun horaInicioMenorQueFin(horaInicio: String, horaFin: String): Boolean {
        val inicio = convertirHoraAMinutos(horaInicio)
        val fin = convertirHoraAMinutos(horaFin)

        return inicio < fin
    }

    private fun convertirHoraAMinutos(hora: String): Int {
        return try {
            val partes = hora.split(":")
            val h = partes[0].toInt()
            val m = partes[1].toInt()

            h * 60 + m

        } catch (e: Exception) {
            0
        }
    }

    private suspend fun crearDocumentoOpcional(
        titulo: String,
        tipo: String,
        descripcion: String,
        url: String,
        idMateria: Int
    ): Boolean {
        val hayAlgo = titulo.isNotEmpty() || tipo.isNotEmpty() || descripcion.isNotEmpty() || url.isNotEmpty()

        if (!hayAlgo) {
            return true
        }

        if (titulo.isEmpty() || tipo.isEmpty() || url.isEmpty()) {
            tvSubjectCount.text = "Para agregar documento completa título, tipo y link."
            return false
        }

        val descripcionFinal = if (descripcion.isEmpty()) {
            "Material de apoyo para la materia"
        } else {
            descripcion
        }

        val response = api.crearDocumento(
            titulo = titulo,
            tipo_documento = tipo,
            descripcion = descripcionFinal,
            url_archivo = url,
            id_materia = idMateria,
            id_admin = idAdmin
        )

        return if (response.isSuccessful) {
            true
        } else {
            val errorBackend = response.errorBody()?.string()
            tvSubjectCount.text =
                "La materia se guardó, pero falló el documento: ${response.code()}\n$errorBackend"
            false
        }
    }
}