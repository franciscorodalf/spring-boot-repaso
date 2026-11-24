# Primera API de tareas con Spring Boot

Instala JDK 21 y Maven. Comprueba con `java -version` y `mvn -version` que Maven usa JDK 21.
El proyecto fija Spring Boot 4.1.2 en el POM.

## 1. Arrancar

Desde la carpeta del proyecto ejecuta `mvn spring-boot:run`. Deja esa terminal abierta.
Desde otra terminal, ejecuta los ejemplos de abajo. En PowerShell usa `curl.exe`.

## 2. Rutas de la API

Hay dos almacenamientos con las mismas operaciones. Cambia `csv` por `json` en
la ruta para usar el otro:

| Método | Ruta | Respuesta |
|--------|------|-----------|
| `GET` | `/api/csv/tareas` | **200** con todas las tareas |
| `GET` | `/api/csv/tareas?titulo=texto` | **200** con las tareas cuyo título contiene `texto` (sin distinguir mayúsculas) |
| `GET` | `/api/csv/tareas/{id}` | **200** con la tarea, **404** si no existe |
| `POST` | `/api/csv/tareas` con `{"titulo":"..."}` | **201** + cabecera `Location`, **400** si el título está en blanco |
| `PUT` | `/api/csv/tareas/{id}` con `{"titulo":"..."}` | **200** con la tarea cambiada, **400** si el título está en blanco, **404** si no existe |
| `DELETE` | `/api/csv/tareas/{id}` | **204** si se borra, **404** si no existe |

Las tareas se guardan en `data/tarea.csv` y `data/tarea.json`. Las rutas de los
ficheros se configuran en `src/main/resources/application.properties`
(`tareas.csv.ruta` y `tareas.json.ruta`), así que los datos se conservan entre
reinicios.

## 3. Probar con curl

```sh
curl -i http://localhost:8080/api/json/tareas
curl -i -H 'Content-Type: application/json' --data '{"titulo":"Preparar entrega"}' http://localhost:8080/api/json/tareas
curl -i http://localhost:8080/api/json/tareas/1
curl -i -X PUT -H 'Content-Type: application/json' --data '{"titulo":"Entrega revisada"}' http://localhost:8080/api/json/tareas/1
curl -i 'http://localhost:8080/api/json/tareas?titulo=revisada'
curl -i -H 'Content-Type: application/json' --data '{"titulo":" "}' http://localhost:8080/api/json/tareas
curl -i http://localhost:8080/api/json/tareas/999
curl -i -X DELETE http://localhost:8080/api/json/tareas/1
```

Con el fichero JSON vacío los resultados esperados son, en orden, **200** con `[]`,
**201** con cabecera `Location: /api/json/tareas/1`, **200** con la tarea creada,
**200** con el título cambiado, **200** con la tarea encontrada, **400** por título
en blanco, **404** para el identificador desconocido y **204** al borrar.
Usa la ruta devuelta en `Location` si ya creaste otras tareas.

## 4. Seguir el código

```
TareaControllerCsv  -> TareaServiceCsv  -> TareaRepositoryCsv  (data/tarea.csv)
TareaControllerJson -> TareaServiceJson -> TareaRepositoryJson (data/tarea.json)
```

- Los **controladores** reciben HTTP y validan el cuerpo (`PeticionTarea` con
  `@NotBlank`). Devuelven 404 cuando el servicio responde `null` o `false`.
- Los **servicios** (`ITareaService`) comprueban los datos y delegan en el
  repositorio. Spring elige la implementación con `@Qualifier`.
- Los **repositorios** (`ITareaRepository`) leen el fichero completo en cada
  operación y lo reescriben al crear, actualizar o borrar. El id nuevo es el
  mayor existente + 1. `TareaRepository` es una versión en memoria que no usa
  ningún controlador.

La persistencia en PostgreSQL se trabaja más adelante en la ruta.

## 5. Tests

`mvn test` ejecuta los tests de `src/test`: los repositorios CSV, JSON y en memoria
y el ciclo completo de la API para los dos almacenamientos. Usan ficheros
temporales, así que no modifican `data/`.

## Entrega breve

Crea una segunda tarea con otro título. Entrega el código de estado, la
cabecera `Location` y el resultado de consultar esa URL.

Para empaquetar, ejecuta `mvn package`. Después puedes iniciar el JAR con
`java -jar target/tareas-0.0.1.jar`.

## Probar la API con Swagger UI

1. Arranca el ejemplo con `mvn spring-boot:run`.
2. Abre [Swagger UI](http://localhost:8080/swagger-ui/index.html). La descripción OpenAPI está en [`/v3/api-docs`](http://localhost:8080/v3/api-docs).
3. Despliega `POST /api/json/tareas` (o `/api/csv/tareas`), pulsa **Try it out**, escribe `{"titulo":"Preparar entrega"}` y pulsa **Execute**. Comprueba el **201** y copia el identificador que aparece en el cuerpo o en la cabecera `Location`.
4. Abre `GET /api/json/tareas/{id}`, escribe ese identificador y verifica el **200** y el título. Repite el POST con `{"titulo":" "}` y verifica el **400**.

Observa el campo **Request URL**. Es la URL HTTP que recibe la petición. Cuando cambies el almacenamiento a fichero, SQLite o PostgreSQL, podrás repetir la práctica desde la misma interfaz y comprobar el dato en el almacenamiento de ese perfil.

Swagger UI muestra también `PUT` (actualizar), `DELETE` (borrar) y la búsqueda `GET ?titulo=` de cada almacenamiento.
