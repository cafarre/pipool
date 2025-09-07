# Usa la imagen oficial de GraalVM para JDK 21 desde la nueva ubicación.
#FROM ghcr.io/graalvm/native-image-community:21 AS builder

# Establece el directorio de trabajo dentro del contenedor.
#WORKDIR /app

# Copia los archivos del proyecto al contenedor.
#COPY . /app

# Compila el proyecto en una imagen nativa.
# Asegúrate de usar los mismos parámetros que funcionaron en tu máquina local.
#RUN ./mvnw native:compile -Pnative -Dnative-image.platform=linux-aarch64

# La segunda etapa crea la imagen final y es mucho más pequeña.
# `scratch` es una imagen vacía, perfecta para el binario nativo.
#FROM scratch

# Copia el binario compilado de la etapa anterior.
# El archivo ejecutable se llamará `pipool` (el nombre del artefacto).
#COPY --from=builder /app/target/pipool /pipool

# Define el punto de entrada para ejecutar la aplicación.
#ENTRYPOINT ["/pipool"]


#FROM ghcr.io/graalvm/jdk:21.0.2-ol8 AS build
FROM ghcr.io/graalvm/native-image-community:21 AS build

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copia los archivos del proyecto al contenedor.
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

# Establece los permisos de ejecución.
RUN chmod +x mvnw

# Compila el proyecto en una imagen nativa.
#RUN ./mvnw -Pnative native:compile -DskipTests
RUN ./mvnw -Pnative native:compile \
  -DskipTests \
  -Dspring.aot.enabled=true \
  -Dspring.native.verbose=true \
  -Dspring.native.build-args="--verbose --diagnostics-mode --report-unsupported-elements-at-runtime --no-fallback -Ob --native-compiler-options=-J-Xmx8g"

# Usa una imagen base minimalista para el ejecutable final,
# lo que resulta en un tamaño de imagen más pequeño.
# scratch es la imagen más pequeña posible, sin sistema operativo,
# solo el ejecutable.
FROM scratch

# Copia el ejecutable nativo generado en la fase de compilación.
COPY --from=build /app/target/pipool .

# Define el comando que se ejecutará al iniciar el contenedor.
ENTRYPOINT ["/pipool"]