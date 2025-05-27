# Imagen base con Java 17
FROM eclipse-temurin:17-jdk

# Crear carpeta para la app
WORKDIR /app

# Copiar el archivo JAR (ajustá el nombre si es diferente)
COPY target/sensores-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8080
EXPOSE 8080

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
