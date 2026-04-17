package com.example.viagourmet.di

import com.example.viagourmet.data.api.CafeteriaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * ─────────────────────────────────────────────────────────────────────────
     * CONFIGURACIÓN DE LA URL BASE DE LA API
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Elige la opción que corresponde a tu entorno:
     *
     * EMULADOR ANDROID → usa 10.0.2.2 (mapea al localhost de tu PC)
     *   BASE_URL = "http://10.0.2.2:8080/"
     *
     * DISPOSITIVO FÍSICO EN LA MISMA RED Wi-Fi → usa la IP local de tu PC
     *   Encuentra tu IP: Windows → ipconfig | Mac/Linux → ip addr o ifconfig
     *   Ejemplo: BASE_URL = "http://192.168.1.100:8080/"
     *
     * La API debe estar corriendo en el puerto 8080 (Spring Boot por defecto). 10.17.0.59
     * Verifica que el servidor responda antes de probar la app.  192.168.1.67 192.168.1.67  10.45.56.252
     */
    private const val BASE_URL = "http://192.168.1.67:8080/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // Headers comunes para todas las peticiones
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCafeteriaApiService(retrofit: Retrofit): CafeteriaApiService =
        retrofit.create(CafeteriaApiService::class.java)
}