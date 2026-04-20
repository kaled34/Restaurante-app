# 🍽️ La Cafetería - Documentación Técnica Integral

## 1. Visión del Proyecto
**La Cafetería** es una plataforma móvil multiplataforma (Android) diseñada para optimizar la cadena de valor de un establecimiento gastronómico. El sistema integra el catálogo de productos, la gestión de pedidos en tiempo real y el control administrativo, operando sobre una infraestructura en la nube para garantizar consistencia de datos global.

---

## 2. Arquitectura de Software
Se ha implementado una arquitectura basada en **Clean Architecture** y **MVVM (Model-View-ViewModel)**, asegurando la separación de responsabilidades y facilidad de mantenimiento.

### Capas del Sistema:
*   **Presentación (UI):** Desarrollada íntegramente con **Jetpack Compose**, utilizando componentes declarativos y un sistema de temas personalizado (Material 3).
*   **Dominio (Business Logic):** Define los modelos puros (`Pedido`, `Producto`, `Cliente`) y las reglas de negocio independientes de la plataforma.
*   **Datos (Data):** Implementa el patrón **Repository** como fuente única de verdad, orquestando la persistencia local (**Room**) y la comunicación remota (**Retrofit**).

---

## 3. Stack Tecnológico de Vanguardia
*   **Lenguaje:** Kotlin 1.9+.
*   **Inyección de Dependencias:** Dagger Hilt (gestión de Singletons para APIs y BD).
*   **Red:** Retrofit 2 con OkHttp 4 (incluye interceptores de log para depuración).
*   **Persistencia:** Room Database con soporte para **Flow** y corrutinas.
*   **Imágenes:** Coil (Pipeline de carga asíncrona con caché de memoria y disco).
*   **Hardware:** Integración con CameraX para captura de credenciales y GPS para geolocalización.
*   **Backend:** Java Spring Boot desplegado en **AWS EC2 (Ubuntu 22.04 LTS)**.

---

## 4. Módulo de Geoposicionamiento y Seguridad (Anti-Fraude)

### 📍 Localización en Tiempo Real
La aplicación implementa una integración avanzada con **Google Play Services Location** para obtener la ubicación del cliente con precisión de metros.

**Lógica de Obtención:**
```kotlin
// LocationService.kt: Implementación asíncrona mediante corrutinas
suspend fun getCurrentLocation(): Location? {
    return fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY, null
    ).await()
}
```

**Reverse Geocoding:**
Convierte las coordenadas `(lat, lng)` en una dirección humana legible (Calle, Número, Localidad, País) utilizando la API de **Geocoder** de Google, lo que facilita al repartidor o administrador la ubicación exacta del pedido.

### 🛡️ Detección de Fake GPS (Sistema de Integridad)
Para prevenir el fraude en pedidos "para llevar" o entregas a domicilio, se implementó un sistema de detección de ubicaciones simuladas.

**Técnicas de Verificación:**
1.  **Flag `isMock`:** Detecta si la señal GPS proviene de un satélite real o de una capa de software.
2.  **Developer Settings Check:** Verifica si el dispositivo tiene habilitada la opción de "Mock Locations".

**Código de Integridad:**
```kotlin
fun isLocationMocked(location: Location?): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        location?.isMock ?: false // Android 12+
    } else {
        location?.isFromMockProvider ?: false // Legacy Support
    }
}
```
*   **Efecto:** Si se detecta manipulación, la aplicación bloquea la función de pedido y muestra un banner de advertencia visual rojo (🚨) en el perfil del usuario.

---

## 5. Ecosistema de Endpoints (AWS API)

La aplicación interactúa con un backend RESTful en AWS. Todos los modelos de datos siguen el estándar **camelCase** para asegurar la compatibilidad con el motor JPA/Hibernate del servidor.

### 🔐 Autenticación y Perfil
| Método | Endpoint | Propósito |
| :--- | :--- | :--- |
| `POST` | `/api/v1/clientes/login` | Autenticación de clientes. |
| `POST` | `/api/v1/empleados/login` | Autenticación del staff. |
| `POST` | `/api/v1/empleados` | Registro de nuevos administradores/cocineros. |

### 🍱 Gestión de Catálogo (Menú)
*   **Sincronización:** Cada cambio (Delete/Patch) realizado por un empleado dispara un evento de "Full Refresh" en todos los clientes conectados.
*   **Endpoint de Ocultado:** `PATCH /api/v1/productos/{id}/disponibilidad?disponible=false`.

### 🛒 Ciclo de Vida del Pedido
La creación de un pedido requiere una estructura JSON compleja mapeada mediante `CrearPedidoRequest`:
```json
{
  "idEmpleado": 1,
  "idCliente": 4,
  "detalles": [
    { "idProducto": 18, "cantidad": 2, "precioUnitario": 35.0 }
  ],
  "tipo": "para_llevar"
}
```

---

## 6. Lógica de Persistencia Local (Room)

La base de datos local no solo sirve como caché, sino como garante de persistencia para datos críticos:

1.  **Foto de Perfil:** Dado que la API no almacena archivos binarios pesados, la App guarda la **URI de la foto de perfil** en la tabla `usuarios` de Room. Esto permite que la foto capturada en el registro persista en el dispositivo a través de reinicios y cierres de sesión.
2.  **Modo Offline:** Los pedidos confirmados se guardan localmente antes de sincronizarse, permitiendo al usuario consultar su historial sin conexión a internet.

---

## 7. Roles de Usuario y Flujos de Trabajo

*   **ADMIN:** Acceso al panel de control (`EditarMenuScreen`). Puede modificar precios, nombres y estados de disponibilidad que afectan a todo el sistema AWS de forma inmediata.
*   **CLIENTE:** Experiencia de usuario optimizada para la compra. Visualiza el menú real filtrado por disponibilidad.
*   **COCINERO:** Recibe notificaciones push vía FCM y gestiona la cola de producción.

---

## 8. Mejoras a Futuro (Roadmap)
Para escalar la plataforma y mejorar la retención de usuarios, se proponen las siguientes integraciones:

1.  **Pasarela de Pagos Nativa:** Integración con Stripe o PayPal para permitir pagos con tarjeta de crédito/débito directamente desde la App.
2.  **Seguimiento en Mapa:** Visualización en tiempo real del repartidor mediante un mapa interactivo para pedidos a domicilio.
3.  **Sistema de Fidelización:** Implementación de puntos por compra y cupones de descuento dinámicos gestionados desde el panel de Admin.
4.  **Modo Oscuro Completo:** Soporte para temas dinámicos que se adapten a la configuración del sistema del usuario.
5.  **Soporte Multi-idioma:** Localización completa de la App (Inglés/Español) utilizando archivos de recursos `strings.xml`.
6.  **IA Predictiva:** Sugerencias de productos basadas en el historial de pedidos y preferencias del cliente.

---

**Equipo de Desarrollo:**
*   **Kaled Pacheco:** Desarrollo de la aplicación Android, arquitectura de conexiones, despliegue de base de datos y de la API REST en infraestructura AWS.
*   **Marco Antonio Lequin Sanchez:** Diseño y desarrollo de la base de datos, desarrollo de la API REST, arquitectura de conexiones y despliegue en infraestructura AWS.
*   **KM Soft Solutions**

**Infraestructura:** AWS EC2 - Instance: `54.145.189.91`
**Lanzamiento:** Versión 2.0.1 - Abril 2026
