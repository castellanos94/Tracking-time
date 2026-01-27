# Time Tracker Application

Una aplicación de escritorio ligera construida con JavaFX para el seguimiento del tiempo dedicado a diferentes tareas y categorías. Diseñada como un widget flotante "siempre visible" para un acceso rápido y discreto.

## 🏗 Arquitectura y Diseño

El proyecto sigue el patrón de diseño **MVC (Modelo-Vista-Controlador)** con una capa de servicio robusta para la gestión de la lógica de negocio y el estado.

### Componentes Principales

*   **Model (Modelo)**:
    *   `Category`: Representa una categoría de trabajo (anteriormente "Proyecto"). Contiene nombre, color y tarifa por hora.
    *   `TimeEntry`: Registra una sesión de trabajo. Almacena la referencia a la categoría, hora de inicio/fin y, crucialmente, una copia de la `hourlyRate` en el momento de la creación para preservar la integridad histórica.
    *   `DataWrapper`: Clase auxiliar para la serialización JSON de todo el estado de la aplicación.
*   **View (Vista)**:
    *   Archivos FXML (`widget.fxml`, `configuration.fxml`) definen la estructura de la UI.
    *   Estilos CSS (`styles.css`) para una apariencia moderna y limpia.
*   **Controller (Controlador)**:
    *   `WidgetController`: Gestiona la ventana principal, el temporizador, y la visualización del historial diario.
    *   `ConfigurationController`: Gestiona la ventana de configuración de categorías (Lógica CRUD pendiente de implementación por el usuario).
*   **Service (Servicio)**:
    *   `TimerService`: Singleton que actúa como la única fuente de la verdad. Maneja la lista de categorías, el historial de tiempos, el cronómetro activo y la persistencia.

### 💾 Persistencia de Datos

*   Los datos se guardan automáticamente en un archivo JSON: `.tracking-time-data.json` ubicado en el directorio `user.home`.
*   **Estrategia de Carga**: Al iniciar, se cargan todas las categorías y el historial completo. Sin embargo, la UI solo muestra las entradas del **día actual** para mantener la interfaz limpia.
*   **Estrategia de Guardado**: Se guarda todo (categorías, historial de hoy y historial archivado) al cerrar la aplicación o modificar datos críticos.

### 📦 Empaquetado

*   Se utiliza `maven-shade-plugin` para crear un **"Fat Jar"** (JAR con dependencias incluidas).
*   Se incluye una clase `Launcher` separada para evitar conflictos de módulos de JavaFX al ejecutar el JAR directamente.

## 🚀 Situación Actual del Proyecto

### Funcionalidades Implementadas ✅
*   **Cronómetro**: Iniciar y detener el seguimiento de tiempo.
*   **Gestión de Categorías (Básica)**: Selección de categoría activa desde el widget principal.
*   **Historial Diario**: Visualización de las sesiones del día actual.
*   **Cálculo de Ganancias**: Muestra el total ganado hoy basado en las tarifas por hora.
*   **Persistencia Robusta**: Guardado automático y recuperación de datos; integridad de tarifas históricas.
*   **Widget UI**: Ventana transparente, arrastrable y siempre visible.
*   **Build Scripts**: Scripts de PowerShell (`run.ps1`, `package.ps1`) para facilitar la compilación y ejecución.

### Pendiente / En Progreso 🚧
*   **Lógica de Configuración (CRUD)**: La interfaz `configuration.fxml` y su controlador `ConfigurationController` están creados y conectados a los datos. **Falta implementar la lógica de negocio** dentro de los métodos `handleSave`, `handleDelete`, etc.
*   **Exportar a Excel**: Se ha agregado el botón en la interfaz principal (`📥`) y el método `handleExport` en el controlador. **Falta implementar la lógica** de generación del archivo Excel (posiblemente usando Apache POI).
*   **Filtrado de Historial por Fecha**: Implementar la capacidad de navegar y ver el historial de días anteriores, no solo el actual.

## 🛠 Cómo Ejecutar y Construir

**Requisitos**: JDK 21+, Maven 3.11+

### Ejecutar en Desarrollo
```powershell
.\run.ps1
```

### Empaquetar (Crear Ejecutable JAR)
```powershell
.\package.ps1
```
El archivo resultante estará en `target/tracking-time-1.0-SNAPSHOT.jar`.
Para ejecutar el jar generado:
```powershell
java -jar target/tracking-time-1.0-SNAPSHOT.jar
```
