# Card Platform API

API REST para gestión de tarjetas y transacciones.

## Tabla de Contenidos
1. [Detalle de la prueba](#detalle-de-la-prueba)
2. [Consideraciones Iniciales](#consideraciones-iniciales)
3. [Tecnologías](#tecnologías)
4. [Ejecución de la Aplicación](#ejecución-de-la-Aplicación) <br>
   4.1. [Ejecución en Entorno Local](#ejecución-en-entorno-local) <br>
   4.2. [Ejecución a través del IDE](#ejecución-a-través-del-ide) <br>
   4.3. [Consumo directo de Servicios](#consumo-directo-de-servicios)
5. [Pruebas Unitarias](#pruebas-unitarias)

### Detalle de la Prueba
Se requiere crear una aplicación para administración de tarjetas de crédito y transacciones de
compra, para esto se debe crear un API Restful para la creación y actualización de objetos y una
interfaz gráfica para consultar los mismos.

### Consideraciones Iniciales
- Los mensajes de error expuestos por la API son genéricos por razones de seguridad.
  No se filtra información sensible como existencia de tarjetas o estados internos.
- El PAN nunca se persiste en texto plano. Se almacena un hash y un PAN enmascarado.
- Se utiliza una arquitectura en capas:
   - Controller (exposición REST)
   - Service (lógica de negocio)
   - Repository (acceso a datos)
- Se implementaron pruebas unitarias y de integración básicas.

### Tecnologías
* [Java 17](https://www.oracle.com/java/technologies/downloads/): Versión Java SE 17
* [Spring Boot](https://spring.io/projects/spring-boot): Versión 4.0.3
* [Spring Data JPA](https://spring.io/projects/spring-data-jpa): Persistencia
* [Hibernate](https://hibernate.org/orm/): ORM
* [Lombok](https://projectlombok.org/setup/maven): Versión 1.18.42
* [H2 Database](https://www.h2database.com/html/main.html): Versión 2.4.240
* [JUnit 5](https://junit.org/junit5/): Testing
* [Mockito](https://site.mockito.org/): Versión 5.20.0
* [Maven](https://maven.apache.org/): Gestión de dependencias

### Ejecución de la Aplicación

#### Ejecución en Entorno Local:

Para ejecutar la aplicación en entorno local se puede realizar cargando el proyecto a través del IDE de desarrollo o instalando Maven en el equipo, para posteriormente instalar la aplicación y ejecutarla.

```bash
mvn clean spring-boot:run
```

#### Ejecución a través del IDE:

Para ejecutar la aplicación a través del IDE de desarrollo se debe considerar tener el siguiente software instalado en el equipo:

* JDK 17 - https://www.oracle.com/java/technologies/downloads/
* Maven - https://maven.apache.org/download.cgi

Una vez instalado todo lo mencionado, se debe descargar el código fuente del proyecto ubicado en <URL>
Importarlo a través del IDE de desarrollo, seleccionar click derecho sobre el mismo y seleccionar Run... -> CardPlatformApplication. El aplicativo se encargará de descargar las dependencias necesarias para la ejecución y comenzará a escuchar peticiones a través de la URI http://localhost:8080/swagger-ui.html


### Endpoints principales

POST /api/cards
POST /api/transactions
GET /api/cards/{id}
GET /api/cards/{id}/transactions

### Pruebas
```bash
mvn test
```

### Pruebas Unitarias

Se realizaron pruebas unitarias a través de JUnit Test, obtenido un Code Coverage superior al 80%

![Coverage](/doc/images/Coverage.jpeg)

## Decisiones de diseño

- Base de datos H2 para facilidad de pruebas
- No se expone información sensible en errores
- Se utiliza hashing para PAN
- Se implementan pruebas unitarias y de controlador

---