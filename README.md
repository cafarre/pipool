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
Descarga el binario ejecutable `pipool` generado en la Release de GitHub directamente en la Raspberry Pi o transfiriéndolo vía `scp`:

```bash
# Opción A: Transferir desde tu equipo local vía SCP
scp target/pipool pi@<IP_RASPBERRY>:/home/pi/pipool/pipool

# Opción B: Descargar directamente en la Raspberry Pi desde GitHub Releases
curl -L -O https://github.com/<usuario>/pipool/releases/download/v4.1.0/pipool
```

### 2. Estructura de Directorios Requerida en la Raspberry Pi
En el directorio de ejecución de la Raspberry Pi (ej. `/home/pi/pipool/`), deben existir las carpetas de configuración y registros:

```text
/home/pi/pipool/
├── pipool                  # Binario ejecutable nativo
├── conf/
│   ├── sondes.json         # Configuración de sondas y umbrales
│   └── reles.json          # Configuración de relés
└── logs/                   # Directorio de logs (se crea automáticamente)
```

### 3. Asignación de Permisos y Ejecución Manual
```bash
cd /home/pi/pipool
chmod +x pipool
./pipool
```

### 4. Configurar como Servicio del Sistema (systemd)
Para que PiPool se ejecute automáticamente como demonio al arrancar la Raspberry Pi:

1. Crear el archivo de servicio `/etc/systemd/system/pipool.service`:
   ```bash
   sudo nano /etc/systemd/system/pipool.service
   ```

2. Añadir el siguiente contenido:
   ```ini
   [Unit]
   Description=PiPool App Service
   After=network.target

   [Service]
   Type=simple
   User=pi
   WorkingDirectory=/home/pi/pipool
   ExecStart=/home/pi/pipool/pipool
   Restart=always
   RestartSec=10
   Environment=SEC_USER=tu_usuario SEC_PASSWORD=tu_password JWT_SECRET=tu_jwt_secret

   [Install]
   WantedBy=multi-user.target
   ```

3. Recargar y activar el servicio:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable pipool
   sudo systemctl start pipool
   ```

4. Ver el estado y los logs:
   ```bash
   sudo systemctl status pipool
   journalctl -u pipool -f
   ```
