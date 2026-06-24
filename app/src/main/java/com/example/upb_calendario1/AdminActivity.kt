package com.example.upb_calendario1

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val nombre = intent.getStringExtra("nombre") ?: "Administrador"
        val txtBienvenidaAdmin = findViewById<TextView>(R.id.txtBienvenidaAdmin)

        txtBienvenidaAdmin.text = "Bienvenido Admin\n$nombre"
    }
}