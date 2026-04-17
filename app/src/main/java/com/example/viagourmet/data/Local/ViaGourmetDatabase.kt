package com.example.viagourmet.data.Local.util

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.dao.UsuarioDao
import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.entity.UsuarioEntity
import com.example.viagourmet.data.entity.PedidoLibreEntity
import com.example.viagourmet.data.entity.PedidoConDetalles
import com.example.viagourmet.data.entity.DetallePedidoEntity

@Database(
    entities = [
        PedidoEntity::class,
        DetallePedidoEntity::class,
        PedidoLibreEntity::class,
        UsuarioEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ViaGourmetDatabase : RoomDatabase() {
    abstract fun pedidoDao(): PedidoDao
    abstract fun usuarioDao(): UsuarioDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre       TEXT    NOT NULL,
                apellido     TEXT,
                telefono     TEXT,
                email        TEXT    NOT NULL,
                passwordHash TEXT    NOT NULL,
                rol          TEXT    NOT NULL,
                activo       INTEGER NOT NULL DEFAULT 1,
                creadoEn     TEXT    NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_usuarios_email ON usuarios(email)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE usuarios ADD COLUMN fotoCredencialUri TEXT")
    }
}