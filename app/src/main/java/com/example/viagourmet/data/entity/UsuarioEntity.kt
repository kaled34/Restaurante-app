package com.example.viagourmet.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String?,
    val telefono: String?,
    val email: String,
    val passwordHash: String,
    val rol: String,
    val activo: Boolean = true,
    val creadoEn: String,
    val fotoCredencialUri: String? = null
)
