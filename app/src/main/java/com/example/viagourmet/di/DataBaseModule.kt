package com.example.viagourmet.di

import android.content.Context
import androidx.room.Room
import com.example.viagourmet.data.Local.util.MIGRATION_1_2
import com.example.viagourmet.data.Local.util.MIGRATION_2_3
import com.example.viagourmet.data.Local.util.ViaGourmetDatabase

import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.dao.UsuarioDao


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ViaGourmetDatabase =
        Room.databaseBuilder(context, ViaGourmetDatabase::class.java, "viagourmet.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides @Singleton
    fun providePedidoDao(db: ViaGourmetDatabase): PedidoDao = db.pedidoDao()

    @Provides @Singleton
    fun provideUsuarioDao(db: ViaGourmetDatabase): UsuarioDao = db.usuarioDao()
}
