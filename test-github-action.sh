#!/bin/bash
# Script para probar la compilación localmente antes de hacer push

set -e

echo "🚀 Probando compilación nativa ARM64 localmente..."

# Verificar prerrequisitos
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado"
    exit 1
fi

# Crear contenedor temporal para compilar
echo "📦 Creando entorno de compilación..."
docker run --rm -it \
    -v "$(pwd):/workspace" \
    -w /workspace \
    --platform linux/amd64 \
    ubuntu:22.04 bash -c "
    
    # Instalar dependencias
    apt-get update && apt-get install -y \
        openjdk-21-jdk \
        wget \
        gcc-aarch64-linux-gnu \
        g++-aarch64-linux-gnu \
        libc6-dev-arm64-cross \
        curl \
        unzip
    
    # Instalar GraalVM 21
    cd /tmp
    wget -O graalvm.tar.gz https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.1/graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz
    tar -xzf graalvm.tar.gz
    mv graalvm-community-openjdk-21.0.1+12.1 /opt/graalvm
    
    export JAVA_HOME=/opt/graalvm
    export PATH=\$JAVA_HOME/bin:\$PATH
    
    # Instalar Native Image
    \$JAVA_HOME/bin/gu install native-image
    
    # Configurar cross-compilation
    export CC=aarch64-linux-gnu-gcc
    export CXX=aarch64-linux-gnu-g++
    export CROSS_COMPILE=aarch64-linux-gnu-
    
    # Compilar
    cd /workspace
    chmod +x ./mvnw
    
    echo '✅ Compilando con configuración ARM64...'
    ./mvnw -Pnative native:compile -DskipTests \
        -Dspring.aot.enabled=true \
        -Dnative-image.platform=linux-aarch64 \
        -Dnative-image.crosscompile=true \
        -Dgraalvm.native.verbose=true
    
    # Verificar resultado
    echo '🔍 Verificando ejecutable generado...'
    find target/ -type f -executable -exec file {} \;
    
    echo '✅ Compilación completada!'
"

echo "🎉 Test completado. Si no hay errores, el workflow de GitHub Actions debería funcionar."