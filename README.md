# PiPool App

Aplicación Spring Boot 3 para la gestión y automatización de piscinas mediante sensores (temperatura, pH, ORP) y relés en Raspberry Pi. Compilada con GraalVM Native Image (AOT) para arquitectura ARM64.

---

## 🚀 1. Ejecución en Entorno Local (Desarrollo / Test)

Para ejecutar la aplicación localmente en tu PC (Windows, Linux o macOS) sin requerir el hardware físico I2C de la Raspberry Pi:

### Prerrequisitos
- JDK 21 (o GraalVM JDK 21) instalado.

### Ejecución con perfil `Local` (Modo Test / Hardware Mockeado)
El perfil `Local` activa la simulación de sondes (`modeTest: true`) para evitar errores al intentar acceder al bus I2C de Raspberry Pi.

- **Windows (PowerShell / CMD)**:
  ```powershell
  .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=Local
  ```

- **Linux / macOS**:
  ```bash
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=Local
  ```

La aplicación estará disponible en: `http://localhost:8045/pipool`

### Ejecución de Pruebas Unitarias
```powershell
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### Generación y ejecución de JAR tradicional
```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target/pipool-4.1.0.jar --spring.profiles.active=Local
```

---

## 🛠️ 2. Generación del Artefacto de Producción (GitHub Actions)

La compilación nativa en GraalVM para arquitectura **ARM64** (Raspberry Pi) requiere compilar sobre un entorno ARM64. Se realiza automáticamente a través del workflow configurado en GitHub Actions.

### Pasos para generar la Release nativa:

1. Ve al repositorio en **GitHub** (`https://github.com/<usuario>/pipool`).
2. Haz clic en la pestaña **Actions**.
3. En el menú de la izquierda, selecciona el workflow **Build Spring Boot Native ARM64**.
4. Haz clic en el botón **Run workflow**:
   - Selecciona la rama principal (`main` o `master`).
   - OPCIONAL: Marca la casilla `skip_tests` si deseas omitir los tests.
   - Haz clic en **Run workflow**.
5. **Resultado**:
   - Se compilará el binario nativo optimizado AOT con GraalVM.
   - Al finalizar, GitHub Actions creará automáticamente una nueva **GitHub Release** (con la versión definida en `pom.xml`, ej. `v4.1.0`) que contiene el binario ejecutable `pipool` adjunto.
   - También podrás descargar el binario desde la sección de **Artifacts** de la propia ejecución del workflow (`pipool-linux-aarch64`).

---

## 🍓 3. Despliegue en Raspberry Pi

### 1. Transferencia del Binario a la Raspberry Pi
Conectar a la maquina de producción RaspberryPi Zero 2

```bash
ssh pi@10.0.0.22

```

### 2. Actualización del entorno de ejecución y configuración de pipool
Antes de ejecutar el despliegue hay que asegurar que hemos hecho commit y push de todos los cambios de configuración en el proyecto pipool-data-prod

Luego en el directorio de ejecución de la Raspberry Pi (ej. `/home/pi/pipool/`) ejecutar **pull** para descargar los cambios:

```bash
cd /home/pi/pipool
sudo git pull
```

### 3. Ejecutar el script de despliegue de la ultima release
En el directorio de ejecución de la Raspberry Pi (ej. `/home/pi/pipool/`), debe existir un script de despliegue llamado `deploy_native.sh`:

```bash
cd /home/pi/pipool
./deploy_native.sh
```

