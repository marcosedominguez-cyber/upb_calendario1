package com.example.upb_calendario1

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import kotlin.jvm.java


interface ApiService {
    @GET("api/carreras")
    suspend fun getCarreras(): Response<RespuestaCarreras>

    @FormUrlEncoded
    @POST("api/carreras")
    suspend fun crearCarrera(
        @Field("nombre") nombre: String,
        @Field("facultad") facultad: String
    ): Response<CarreraResponse>

    @PATCH("api/carreras/{id}")
    suspend fun actualizarCarrera(
        @Path("id") id: Int,
        @Body request: CarreraUpdateRequest
    ): Response<CarreraResponse>

    @DELETE("api/carreras/{id}")
    suspend fun eliminarCarrera(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/admins")
    suspend fun getAdmins(): Response<RespuestaAdmins>

    @FormUrlEncoded
    @POST("api/admins")
    suspend fun crearAdmin(
        @Field("nombre") nombre: String,
        @Field("correo") correo: String,
        @Field("contrasena") contrasena: String
    ): Response<AdminResponse>

    @GET("api/modulos")
    suspend fun getModulos(): Response<RespuestaModulos>

    @FormUrlEncoded
    @POST("api/modulos")
    suspend fun crearModulo(
        @Field("nombre") nombre: String,
        @Field("numero_modulo") numero_modulo: Int,
        @Field("fecha_inicio") fecha_inicio: String,
        @Field("fecha_fin") fecha_fin: String
    ): Response<ModuloResponse>

    @GET("api/usuarios")
    suspend fun getUsuarios(): Response<RespuestaUsuarios>

    @FormUrlEncoded
    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Field("cod_est_upb") cod_est_upb: String,
        @Field("nombre") nombre: String,
        @Field("apellido") apellido: String,
        @Field("edad") edad: Int,
        @Field("correo") correo: String,
        @Field("contrasena") contrasena: String,
        @Field("id_carrera") id_carrera: Int
    ): Response<UsuarioResponse>

    @PATCH("api/usuarios/{cod_est_upb}")
    suspend fun actualizarUsuario(
        @Path("cod_est_upb") codEstUpb: String,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    @GET("api/materias")
    suspend fun getMaterias(): Response<RespuestaMaterias>

    @FormUrlEncoded
    @POST("api/materias")
    suspend fun crearMateria(
        @Field("nombre") nombre: String,
        @Field("creditos") creditos: Int,
        @Field("duracion_semanas") duracion_semanas: Int,
        @Field("tipo_materia") tipo_materia: String,
        @Field("id_modulo") id_modulo: Int,
        @Field("id_admin") id_admin: Int
    ): Response<MateriaResponse>

    @PATCH("api/materias/{id}")
    suspend fun actualizarMateria(
        @Path("id") id: Int,
        @Body request: MateriaRequest
    ): Response<MateriaResponse>

    @DELETE("api/materias/{id}")
    suspend fun eliminarMateria(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/documentos")
    suspend fun getDocumentos(): Response<RespuestaDocumentos>

    @FormUrlEncoded
    @POST("api/documentos")
    suspend fun crearDocumento(
        @Field("titulo") titulo: String,
        @Field("tipo_documento") tipo_documento: String,
        @Field("descripcion") descripcion: String,
        @Field("url_archivo") url_archivo: String,
        @Field("id_materia") id_materia: Int,
        @Field("id_admin") id_admin: Int
    ): Response<DocumentoResponse>

    @GET("api/inscripciones")
    suspend fun getInscripciones(): Response<RespuestaInscripciones>

    @FormUrlEncoded
    @POST("api/inscripciones")
    suspend fun crearInscripcion(
        @Field("fecha_inscripcion") fecha_inscripcion: String,
        @Field("estado") estado: String,
        @Field("nota_final") nota_final: String,
        @Field("cod_est_upb") cod_est_upb: String,
        @Field("id_materia") id_materia: Int
    ): Response<InscripcionesResponse>

    @PATCH("api/inscripciones/{id}")
    suspend fun actualizarInscripcion(
        @Path("id") id: Int,
        @Body request: InscripcionesRequest
    ): Response<InscripcionesResponse>

    @DELETE("api/inscripciones/{id}")
    suspend fun eliminarInscripcion(
        @Path("id") id: Int
    ): Response<Unit>


    @GET("api/horario-materia")
    suspend fun getHorarioMateria(): Response<RespuestaHorarioMateria>

    @FormUrlEncoded
    @POST("api/horario-materia")
    suspend fun crearHorarioMateria(
        @Field("dia_semana") dia_semana: String,
        @Field("hora_inicio") hora_inicio: String,
        @Field("hora_fin") hora_fin: String,
        @Field("aula") aula: String,
        @Field("id_materia") id_materia: Int
    ): Response<HorarioMateriaResponse>

    @PATCH("api/horario-materia/{id}")
    suspend fun actualizarHorarioMateria(
        @Path("id") id: Int,
        @Body request: HorarioMateriaRequest
    ): Response<HorarioMateriaResponse>

    @DELETE("api/horario-materia/{id}")
    suspend fun eliminarHorarioMateria(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/calendarios")
    suspend fun getCalendarios(): Response<RespuestaCalendarios>

    @FormUrlEncoded
    @POST("api/calendarios")
    suspend fun crearCalendario(
        @Field("cod_est_upb") cod_est_upb: String
    ): Response<CalendarioResponse>

    @GET("api/eventos-calendarios")
    suspend fun getEventosCalendarios(): Response<RespuestaEventosCalendarios>

    @FormUrlEncoded
    @POST("api/eventos-calendarios")
    suspend fun crearEventoCalendario(
        @Field("titulo") titulo: String,
        @Field("descripcion") descripcion: String,
        @Field("tipo_evento") tipo_evento: String,
        @Field("fecha_inicio") fecha_inicio: String,
        @Field("fecha_fin") fecha_fin: String,
        @Field("es_recurrente") es_recurrente: Int,
        @Field("id_calendario") id_calendario: Int,
        @Field("id_materia") id_materia: Int
    ): Response<EventoCalendarioResponse>

    @PATCH("api/eventos-calendarios/{id}")
    suspend fun actualizarEventoCalendario(
        @Path("id") id: Int,
        @Body request: EventoCalendarioRequest
    ): Response<EventoCalendarioResponse>

    @DELETE("api/eventos-calendarios/{id}")
    suspend fun eliminarEventoCalendario(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/plan-estudios")
    suspend fun getPlanesEstudio(): Response<RespuestaPlanesEstudio>

    @FormUrlEncoded
    @POST("api/plan-estudios")
    suspend fun crearPlanEstudio(
        @Field("nombre") nombre: String,
        @Field("intensidad") intensidad: String,
        @Field("horas_disponibles_semana") horas_disponibles_semana: Int,
        @Field("rendimiento_objetivo") rendimiento_objetivo: String,
        @Field("fecha_inicio") fecha_inicio: String,
        @Field("fecha_fin") fecha_fin: String,
        @Field("cod_est_upb") cod_est_upb: String
    ): Response<PlanEstudioResponse>

    @PATCH("api/plan-estudios/{id}")
    suspend fun actualizarPlanEstudio(
        @Path("id") id: Int,
        @Body request: PlanEstudioRequest
    ): Response<PlanEstudioResponse>

    @DELETE("api/plan-estudios/{id}")
    suspend fun eliminarPlanEstudio(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/plan-estudio-materia")
    suspend fun getPlanEstudioMateria(): Response<RespuestaPlanEstudioMateria>

    @FormUrlEncoded
    @POST("api/plan-estudio-materia")
    suspend fun crearPlanEstudioMateria(
        @Field("horas_asignadas") horas_asignadas: Int,
        @Field("prioridad") prioridad: Int,
        @Field("id_plan") id_plan: Int,
        @Field("id_materia") id_materia: Int
    ): Response<PlanEstudioMateriaResponse>

    @PATCH("api/plan-estudio-materia/{id}")
    suspend fun actualizarPlanEstudioMateria(
        @Path("id") id: Int,
        @Body request: PlanEstudioMateriaRequest
    ): Response<PlanEstudioMateriaResponse>

    @DELETE("api/plan-estudio-materia/{id}")
    suspend fun eliminarPlanEstudioMateria(
        @Path("id") id: Int
    ): Response<Unit>

object RetrofitClient {
    fun create(): com.example.upb_calendario1.ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.19.133.73/upb_calendario/public/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(com.example.upb_calendario1.ApiService::class.java)
    }
}
}
