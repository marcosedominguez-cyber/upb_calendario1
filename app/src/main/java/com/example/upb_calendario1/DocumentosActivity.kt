package com.example.upb_calendario1

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DocumentosActivity : AppCompatActivity() {

    private lateinit var txtEstadoDocumentos: TextView
    private lateinit var contenedorDocumentos: LinearLayout

    private val api = ApiService.RetrofitClient.create()

    private var codEstUpb: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documentos)

        txtEstadoDocumentos = findViewById(R.id.txtEstadoDocumentos)
        contenedorDocumentos = findViewById(R.id.contenedorDocumentos)

        codEstUpb = intent.getStringExtra("cod_est_upb") ?: ""

        findViewById<Button>(R.id.btnVolverDocumentos).setOnClickListener {
            finish()
        }

        cargarDocumentosDisponibles()
    }

    private fun cargarDocumentosDisponibles() {
        if (codEstUpb.isEmpty()) {
            txtEstadoDocumentos.text = "Error: no se recibió el código del estudiante."
            return
        }

        txtEstadoDocumentos.text = "Buscando tus materias inscritas..."

        lifecycleScope.launch {
            try {
                val responseInscripciones = api.getInscripciones()
                val responseDocumentos = api.getDocumentos()
                val responseMaterias = api.getMaterias()

                if (
                    responseInscripciones.isSuccessful &&
                    responseDocumentos.isSuccessful &&
                    responseMaterias.isSuccessful
                ) {
                    val inscripciones = responseInscripciones.body()?.data ?: emptyList()
                    val documentos = responseDocumentos.body()?.data ?: emptyList()
                    val materias = responseMaterias.body()?.data ?: emptyList()

                    val idsMateriasDelUsuario = inscripciones
                        .filter { it.cod_est_upb == codEstUpb }
                        .map { it.id_materia }
                        .toSet()

                    val documentosPermitidos = documentos.filter {
                        idsMateriasDelUsuario.contains(it.id_materia)
                    }

                    contenedorDocumentos.removeAllViews()

                    if (idsMateriasDelUsuario.isEmpty()) {
                        txtEstadoDocumentos.text =
                            "Todavía no tienes materias inscritas en tu calendario."
                        return@launch
                    }

                    if (documentosPermitidos.isEmpty()) {
                        txtEstadoDocumentos.text =
                            "No hay documentos disponibles para tus materias inscritas."
                        return@launch
                    }

                    txtEstadoDocumentos.text =
                        "Documentos encontrados: ${documentosPermitidos.size}"

                    documentosPermitidos.forEach { documento ->
                        val nombreMateria = materias.firstOrNull {
                            it.id_materia == documento.id_materia
                        }?.nombre ?: "Materia desconocida"

                        agregarTarjetaDocumento(documento, nombreMateria)
                    }

                } else {
                    txtEstadoDocumentos.text =
                        "Error cargando documentos. Revisa el API."
                }

            } catch (e: Exception) {
                txtEstadoDocumentos.text = "Error: ${e.message}"
            }
        }
    }

    private fun agregarTarjetaDocumento(
        documento: DocumentoResponse,
        nombreMateria: String
    ) {
        val tarjeta = LinearLayout(this)
        tarjeta.orientation = LinearLayout.VERTICAL
        tarjeta.setPadding(24, 22, 24, 22)
        tarjeta.setBackgroundColor(android.graphics.Color.WHITE)

        val paramsTarjeta = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        paramsTarjeta.setMargins(0, 0, 0, 18)
        tarjeta.layoutParams = paramsTarjeta

        val txtTitulo = TextView(this)
        txtTitulo.text = documento.titulo
        txtTitulo.textSize = 20f
        txtTitulo.setTypeface(null, Typeface.BOLD)
        txtTitulo.setTextColor(android.graphics.Color.parseColor("#003366"))

        val txtMateria = TextView(this)
        txtMateria.text = "Materia: $nombreMateria"
        txtMateria.textSize = 15f
        txtMateria.setTextColor(android.graphics.Color.parseColor("#003366"))
        txtMateria.setPadding(0, 8, 0, 0)

        val txtTipo = TextView(this)
        txtTipo.text = "Tipo: ${documento.tipo_documento}"
        txtTipo.textSize = 14f
        txtTipo.setTextColor(android.graphics.Color.parseColor("#555555"))
        txtTipo.setPadding(0, 6, 0, 0)

        val txtDescripcion = TextView(this)
        txtDescripcion.text = documento.descripcion
        txtDescripcion.textSize = 15f
        txtDescripcion.setTextColor(android.graphics.Color.parseColor("#333333"))
        txtDescripcion.setPadding(0, 10, 0, 10)

        val btnAbrir = Button(this)
        btnAbrir.text = "Abrir documento"
        btnAbrir.setTextColor(android.graphics.Color.WHITE)
        btnAbrir.setBackgroundColor(android.graphics.Color.parseColor("#003366"))

        btnAbrir.setOnClickListener {
            abrirDocumento(documento.url_archivo)
        }

        tarjeta.addView(txtTitulo)
        tarjeta.addView(txtMateria)
        tarjeta.addView(txtTipo)
        tarjeta.addView(txtDescripcion)
        tarjeta.addView(btnAbrir)

        contenedorDocumentos.addView(tarjeta)
    }

    private fun abrirDocumento(url: String) {
        if (url.isEmpty()) {
            Toast.makeText(this, "Este documento no tiene link.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir el documento.", Toast.LENGTH_SHORT).show()
        }
    }
}