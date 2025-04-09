# TrackerHealth

TrackerHealth es una aplicación Android para el seguimiento de actividades físicas, alimentación y métricas de salud. Permite a los usuarios registrar sus actividades diarias, monitorear su progreso y visualizar informes de su estado de salud.

## Características

- **Autenticación de usuarios**: Sistema de login y registro con persistencia de sesión
- **Dashboard personalizado**: Resumen de actividades y métricas de salud
- **Seguimiento de actividad física**: Registro de ejercicios, duración, distancia y calorías quemadas
- **Seguimiento de alimentación**: Registro de comidas, calorías y macronutrientes
- **Informes y estadísticas**: Visualización de progreso y tendencias
- **Persistencia de datos**: Almacenamiento local con SQLite

## Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/trackerhealth/
│   │   │   ├── database/           # Capa de base de datos
│   │   │   ├── dao/                # Objetos de acceso a datos
│   │   │   ├── model/              # Clases de modelo
│   │   │   └── [Activities]        # Actividades de la aplicación
│   │   │
│   │   ├── res/                    # Recursos de la aplicación
│   │   │   ├── layout/             # Layouts XML
│   │   │   ├── values/             # Valores (strings, colores, etc.)
│   │   │   ├── drawable/           # Recursos gráficos
│   │   │   └── menu/               # Menús de navegación
│   │   │
│   │   └── AndroidManifest.xml     # Configuración de la aplicación
```

## Arquitectura

La aplicación sigue el patrón de arquitectura MVC (Modelo-Vista-Controlador) con una capa de persistencia SQLite:

- **Modelo**: Clases en el paquete `model` que representan las entidades de datos
- **Vista**: Archivos XML en la carpeta `res/layout`
- **Controlador**: Actividades en el paquete raíz que manejan la lógica de la interfaz de usuario
- **Persistencia**: Clases en los paquetes `database` y `dao` que manejan el acceso a datos

## Requisitos

- Android 6.0 (API nivel 23) o superior
- Gradle 7.0 o superior
- Android Studio Arctic Fox o superior

## Instalación

1. Clona el repositorio:
   ```
   git clone https://github.com/yourusername/TrackerHealth.git
   ```

2. Abre el proyecto en Android Studio

3. Sincroniza el proyecto con los archivos Gradle

4. Ejecuta la aplicación en un emulador o dispositivo físico

## Uso

### Inicio de Sesión

Para probar la aplicación, puedes usar las siguientes credenciales:
- Email: test@example.com
- Contraseña: password

O crear un nuevo usuario desde la pantalla de registro.

### Navegación

La aplicación utiliza una barra de navegación inferior con cuatro secciones principales:
- **Dashboard**: Resumen general de actividades y métricas
- **Actividad Física**: Registro y seguimiento de ejercicios
- **Alimentación**: Registro y seguimiento de comidas
- **Informes**: Estadísticas y visualización de datos

## Desarrollo

### Capa de Persistencia

La aplicación utiliza SQLite para el almacenamiento local de datos. La estructura de la base de datos incluye:

- Tabla `users`: Información de usuarios
- Tabla `physical_activities`: Registro de actividades físicas
- Tabla `food_entries`: Registro de alimentación

### Extensión

Para añadir nuevas funcionalidades:

1. Crea nuevas clases de modelo en el paquete `model`
2. Implementa los DAOs correspondientes en el paquete `dao`
3. Actualiza `DatabaseHelper` para incluir las nuevas tablas
4. Crea las actividades y layouts necesarios

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

## Contacto

Tu Nombre - [jaiderandres901@hotmail.com](mailto:jaiderandres901@hotmail.com)

Enlace del proyecto: [https://github.com/jaider012/TrackerHealth](https://github.com/jaider012/TrackerHealth) 
