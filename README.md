# Time Tracker

Una aplicación de escritorio ligera construida con JavaFX para el seguimiento del tiempo dedicado a diferentes tareas y categorías. Diseñada para freelancers y profesionales, permite gestionar proyectos, calcular ganancias en tiempo real y exportar informes detallados.

## 🏗 Arquitectura y Diseño

El proyecto sigue el patrón de diseño **MVC (Modelo-Vista-Controlador)** con una capa de servicio robusta y persistencia basada en base de datos.

### Componentes Principales

*   **Model (Modelo)**:
    *   `Category`: Representa una categoría de trabajo. Contiene nombre, color y tarifa por hora.
    *   `TimeEntry`: Registra una sesión de trabajo. Almacena la referencia a la categoría, hora de inicio/fin, descripción y tarifa histórica.
    *   `TimeReport`: Clase DTO para la generación de reportes y exportación.
*   **View (Vista)**:
    *   Archivos FXML (`widget.fxml`, `configuration.fxml`, `export_wizard.fxml`).
    *   Estilos CSS para una apariencia moderna.
*   **Controller (Controlador)**:
    *   `WidgetController`: Gestiona la ventana principal y el temporizador.
    *   `ConfigurationController`: Gestiona la configuración de categorías y tarifas.
    *   `ExportWizardController`: Asistente para la exportación de datos (JSON, CSV, XLSX).
*   **Service & Persistence**:
    *   `TimerService`: Lógica de negocio principal.
    *   `DatabaseManager`: Gestión de conexión a base de datos embebida **Apache Derby**.
    *   **DAOs**: `CategoryDAO`, `TimeEntryDAO`, `TimeReportDAO` para acceso a datos.
    *   `TimeReportExportService`: Servicio de generación de archivos de exportación.

### 💾 Persistencia de Datos

*   **Base de Datos**: Los datos se almacenan de forma segura en una base de datos embebida Apache Derby ubicada en `~/.tracking-time-db`.
*   **Migración**: El sistema migra automáticamente datos de versiones anteriores (JSON) si se detectan.
*   **Integridad**: Uso de transacciones y claves foráneas para integridad referencial.

### 📦 Empaquetado

*   Se utiliza `maven-shade-plugin` para crear un **"Fat Jar"**.
*   Clase `Launcher` para compatibilidad con JavaFX.

## 🚀 Situación Actual del Proyecto

### Funcionalidades Implementadas ✅

*   **Cronómetro**: Seguimiento de tiempo en tiempo real con descripciones.
*   **Gestión de Categorías**: Crear, editar y eliminar categorías con tarifas personalizadas.
*   **Historial y Reportes**: Visualización de historial y cálculo de ganancias.
*   **Persistencia Robusta**: Base de datos SQL embebida (Derby).
*   **Asistente de Exportación**: Exportación de datos a **Excel (XLSX)**, **CSV** y **JSON** con filtrado por rango de fechas.
*   **Widget UI**: Ventana "siempre visible" para acceso rápido.
*   **Build Scripts**: Scripts de PowerShell optimizados.

### Pendiente / En Progreso 🚧

*   **Filtrado Avanzado**: Mejorar las capacidades de filtrado en la vista de historial de la UI principal.
*   **Edición de Entradas**: Permitir editar entradas de tiempo pasadas.

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
