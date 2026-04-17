package com.example.viagourmet.data.Local.mapper

import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.data.entity.UsuarioEntity
import com.example.viagourmet.data.Local.util.hashPassword

fun UsuarioEntity.toSesion(): UsuarioSesion = UsuarioSesion(
    id = id,
    nombre = nombre,
    email = email,
    rol = runCatching { RolUsuario.valueOf(rol) }.getOrDefault(RolUsuario.CLIENTE)
)

fun crearUsuarioEntity(
    nombre: String,
    apellido: String?,
    telefono: String?,
    email: String,
    password: String,
    rol: RolUsuario,
    ahora: String,
    fotoCredencialUri: String? = null
): UsuarioEntity = UsuarioEntity(
    nombre = nombre,
    apellido = apellido,
    telefono = telefono,
    email = email.trim().lowercase(),
    passwordHash = hashPassword(password),
    rol = rol.name,
    activo = true,
    creadoEn = ahora,
    fotoCredencialUri = fotoCredencialUri
)