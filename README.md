# Arquitectura de Microservicios para E-commerce (Patrón SAGA)

Este proyecto es una implementación de referencia de una arquitectura de microservicios robusta para un sistema de E-commerce. Implementa el **Patrón SAGA (Orquestación)** para manejar transacciones distribuidas y garantizar la consistencia eventual de los datos entre servicios desacoplados.

## Arquitectura del Sistema

El sistema utiliza un enfoque híbrido de comunicación (Síncrona y Asíncrona) para maximizar la eficiencia y la resiliencia.

### Servicios:
1.  **Discovery Server (Eureka):** Servidor de registro y descubrimiento de servicios. Elimina la necesidad de "hardcodear" puertos o IPs.
2.  **Order Service (Orquestador):**
    * Recibe las peticiones de compra.
    * Gestiona el ciclo de vida de la transacción SAGA.
    * Se comunica con `Stock-Service` vía mensajería asíncrona (**RabbitMQ**).
    * Se comunica con `Payment-Service` vía REST síncrono (**OpenFeign**).
    * Protegido con **Resilience4j** (Circuit Breaker) para fallos en la pasarela de pagos.
3.  **Stock Service:**
    * Maneja el inventario.
    * Consume eventos de reserva y compensación (rollback) desde RabbitMQ.
4.  **Payment Service:**
    * Simula una pasarela de pagos externa.
    * Persistencia transaccional en base de datos.

## Tech Stack

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.5.x
* **Cloud:** Spring Cloud Netflix Eureka, Spring Cloud OpenFeign
* **Mensajería (Event-Driven):** RabbitMQ
* **Resiliencia:** Resilience4j (Circuit Breaker)
* **Base de Datos:** MySQL 8.0 (Dockerizada)
* **Infraestructura:** Docker Compose

## Flujo SAGA (Caso de Uso: Crear Orden)

1.  **Orden Creada:** Se guarda en estado `PENDING` en MySQL.
2.  **Reserva de Stock:** Se envía un evento asíncrono a RabbitMQ (`ORDER_CREATED`).
3.  **Confirmación de Stock:** El `Stock-Service` reserva los items y responde asíncronamente (`STOCK_CONFIRMED`).
4.  **Procesamiento de Pago:** El `Order-Service` llama síncronamente al `Payment-Service`.
    * **Éxito:** La orden pasa a `APPROVED`.
    * **Fallo (Saldo/Error):** Se activa la **Compensación**. Se envía evento de rollback a Stock (`ORDER_CANCELLED`) y la orden pasa a `CANCELLED`.
    * **Caída del Servicio:** El **Circuit Breaker** detecta la caída del servicio de pagos y ejecuta el `fallback` para cancelar la orden y devolver el stock inmediatamente sin bloquear hilos.

## Cómo ejecutarlo localmente

### Prerrequisitos
* Java 21 instalado.
* Docker y Docker Compose instalados.
* Maven.

### Pasos
1.  **Levantar Infraestructura (MySQL + RabbitMQ):**
    ```bash
    docker-compose up -d
    ```
2.  **Iniciar los Servicios (en este orden recomendado):**
    * `DiscoveryServerApplication`
    * `PaymentServiceApplication`
    * `StockServiceApplication`
    * `OrderServiceApplication`

3.  **Verificar Eureka:**
    Entra a `http://localhost:8761` y verifica que los 3 servicios aparezcan en estado **UP**.

## Pruebas (Endpoints)

Puedes importar la colección de Postman o usar `curl`.

### 1. Crear Orden Exitosa
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{ \"productCode\": \"PROD-001\", \"quantity\": 1, \"price\": 100.00 }"
```
### 2. Probar Fallo de Negocio (Saldo Insuficiente)
El servicio de pagos rechaza montos mayores a 5000.
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{ \"productCode\": \"PROD-001\", \"quantity\": 1, \"price\": 6000.00 }"
```

