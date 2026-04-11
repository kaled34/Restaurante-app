package com.example.viagourmet.di

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.repository.MenuRepositoryImpl
import com.example.viagourmet.data.repository.PedidoRepository
import com.example.viagourmet.data.repository.PedidoRepositoryImpl
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
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

    @Provides
    @Singleton
    fun providePedidoRepositoryLocal(
        dao: PedidoDao,
        api: CafeteriaApiService
    ): PedidoRepositoryLocal = PedidoRepositoryLocal(dao, api)

    @Provides
    @Singleton
    fun provideMenuRepository(
        api: CafeteriaApiService
    ): MenuRepositoryImpl = MenuRepositoryImpl(api)
}