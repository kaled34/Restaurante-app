package com.example.viagourmet.domain.model

data class Categoria(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val modulo: ModuloCategoria?,
    val activo: Boolean
)

enum class ModuloCategoria {
    DESAYUNOS,
    COMIDAS,
    LIBRE;

    companion object {
        fun fromString(value: String): ModuloCategoria = when (value.lowercase()) {
            "desayunos" -> DESAYUNOS
            "comidas"   -> COMIDAS
            "libre"     -> LIBRE
            else        -> COMIDAS
        }
    }
}
