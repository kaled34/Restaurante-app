package com.example.viagourmet.data.dao

import androidx.room.*
import com.example.viagourmet.data.entity.UsuarioEntity

@Dao
interface UsuarioDao {


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long


    @Query("SELECT * FROM usuarios WHERE email = :email AND passwordHash = :hash AND activo = 1 LIMIT 1")
    suspend fun login(email: String, hash: String): UsuarioEntity?


    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UsuarioEntity?


    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): UsuarioEntity?


    @Query("UPDATE usuarios SET nombre = :nombre, apellido = :apellido, telefono = :telefono WHERE id = :id")
    suspend fun updatePerfil(id: Int, nombre: String, apellido: String?, telefono: String?): Int


    @Query("UPDATE usuarios SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Int): Int
}