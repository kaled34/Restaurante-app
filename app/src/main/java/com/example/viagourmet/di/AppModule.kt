package com.example.viagourmet.di

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.repository.MenuRepositoryImpl
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

    /**
     * MenuRepositoryImpl — para MenuViewModel (pantalla de menú del cliente).
     * Conecta directamente con la API para obtener categorías y productos.
     */
    @Provides
    @Singleton
    fun provideMenuRepositoryImpl(
        api: CafeteriaApiService
    ): MenuRepositoryImpl = MenuRepositoryImpl(api)

    // MenuRepository (para EditarMenuViewModel y ProductoDetalleViewModel)
    // tiene @Inject constructor(api) por lo que Hilt lo inyecta automáticamente.
}