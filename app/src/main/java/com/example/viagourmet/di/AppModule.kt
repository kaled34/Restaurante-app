// ─────────────────────────────────────────────────────────────────────────────
// ARCHIVO 2: AppModule.kt  (reemplaza el existente — agrega MenuRepository)
// ─────────────────────────────────────────────────────────────────────────────

package com.example.viagourmet.di
 
import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.repository.MenuRepository
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
 
    // MenuRepository ya tiene @Singleton + @Inject constructor,
    // Hilt lo provee automáticamente — no hace falta un @Provides manual.
    // Si necesitas forzarlo explícitamente, descomenta:
    //
    // @Provides
    // @Singleton
    // fun provideMenuRepository(): MenuRepository = MenuRepository()
}
