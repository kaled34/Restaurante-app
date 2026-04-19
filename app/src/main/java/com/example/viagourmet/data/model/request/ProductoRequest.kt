package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ProductoRequest(
    // Cambiado para coincidir EXACTAMENTE con lo que pide tu servidor en AWS según el error 400
    @SerializedName("idCategoria")
    val idCategoria: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("precio")
    val precio: BigDecimal,

    @SerializedName("disponible")
    val disponible: Boolean = true,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null
)
