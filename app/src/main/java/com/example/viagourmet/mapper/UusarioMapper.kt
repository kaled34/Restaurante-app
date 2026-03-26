package com.example.viagourmet.data.local.mapper

import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.data.local.entity.UsuarioEntity
import com.example.viagourmet.data.local.util.hashPassword

// ── Entidad de sesión

fun UsuarioEntity.toSesion(): UsuarioSesion = UsuarioSesion(
    id = id,
    nombre = nombre,
    email = email,
    rol = RolUsuario.valueOf(rol)
)

// ── Datos de registro

fun crearUsuarioEntity(
    nombre: String,
    apellido: String?,
    telefono: String?,
    email: String,
    password: String,
    rol: RolUsuario,
    ahora: String
): UsuarioEntity = UsuarioEntity(
    nombre = nombre,
    apellido = apellido,
    telefono = telefono,
    email = email.trim().lowercase(),
    passwordHash = hashPassword(password),
    rol = rol.name,
    activo = true,
    creadoEn = ahora
)