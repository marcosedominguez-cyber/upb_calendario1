package com.example.ubp_calendario1

import com.example.upb_calendario1.AdminRequest
import com.example.upb_calendario1.AdminResponse
import com.example.upb_calendario1.CalendarioRequest
import com.example.upb_calendario1.CalendarioResponse
import com.example.upb_calendario1.CarreraRequest
import com.example.upb_calendario1.CarreraResponse
import com.example.upb_calendario1.DocumentoRequest
import com.example.upb_calendario1.DocumentoResponse
import com.example.upb_calendario1.EventoCalendarioRequest
import com.example.upb_calendario1.EventoCalendarioResponse
import com.example.upb_calendario1.HorarioMateriaRequest
import com.example.upb_calendario1.HorarioMateriaResponse
import com.example.upb_calendario1.InscripcionesRequest
import com.example.upb_calendario1.InscripcionesResponse
import com.example.upb_calendario1.MateriaRequest
import com.example.upb_calendario1.MateriaResponse
import com.example.upb_calendario1.ModuloRequest
import com.example.upb_calendario1.ModuloResponse
import com.example.upb_calendario1.PlanEstudioMateriaRequest
import com.example.upb_calendario1.PlanEstudioMateriaResponse
import com.example.upb_calendario1.PlanEstudioRequest
import com.example.upb_calendario1.PlanEstudioResponse
import com.example.upb_calendario1.UsuarioRequest
import com.example.upb_calendario1.UsuarioResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.*
import retrofit2.http.*

interface ApiService {

    @POST("carreras")
    fun crearCarrera(@Body request: CarreraRequest): Call<CarreraResponse>

    @POST("admins")
    fun crearAdmin(@Body request: AdminRequest): Call<AdminResponse>

    @POST("modulos")
    fun crearModulo(@Body request: ModuloRequest): Call<ModuloResponse>

    @POST("usuarios")
    fun crearUsuario(@Body request: UsuarioRequest): Call<UsuarioResponse>

    @POST("materias")
    fun crearMateria(@Body request: MateriaRequest): Call<MateriaResponse>

    @POST("documentos")
    fun crearDocumento(@Body request: DocumentoRequest): Call<DocumentoResponse>

    @POST("inscripciones")
    fun crearInscripcion(@Body request: InscripcionesRequest): Call<InscripcionesResponse>

    @POST("horario-materia")
    fun crearHorarioMateria(@Body request: HorarioMateriaRequest): Call<HorarioMateriaResponse>

    @POST("calendarios")
    fun crearCalendario(@Body request: CalendarioRequest): Call<CalendarioResponse>

    @POST("eventos-calendarios")
    fun crearEventoCalendario(@Body request: EventoCalendarioRequest): Call<EventoCalendarioResponse>

    @POST("plan-estudios")
    fun crearPlanEstudio(@Body request: PlanEstudioRequest): Call<PlanEstudioResponse>

    @POST("plan-estudio-materia")
    fun crearPlanEstudioMateria(@Body request: PlanEstudioMateriaRequest): Call<PlanEstudioMateriaResponse>
}