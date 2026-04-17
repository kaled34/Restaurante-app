package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ProductoRequest(
    @SerializedName("id_categoria")
    val idCategoria: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("precio")
    val precio: BigDecimal,

    @SerializedName("disponible")
    val disponible: Boolean = true,

    @SerializedName("imagen_url")
    val imagenUrl: String? = null
)