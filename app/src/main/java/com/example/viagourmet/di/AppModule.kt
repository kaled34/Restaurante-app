package com.example.viagourmet.di

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.repository.PedidoRepository
import com.example.viagourmet.data.repository.PedidoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePedidoRepository(
        dao: PedidoDao,
        api: CafeteriaApiService
    ): PedidoRepository = PedidoRepositoryImpl(dao, api)
}
