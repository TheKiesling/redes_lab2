# Proyecto de Detección y Corrección de Errores con Checksum de Fletcher y Hamming

Este proyecto implementa técnicas de detección y corrección de errores utilizando el algoritmo de Hamming y el algoritmo de checksum de Fletcher-16. Está compuesto por un servidor que calcula y envía mensajes codificados, y un cliente que recibe y verifica los mensajes.

## Estructura del Proyecto

### Servidor (Java)
- **`JavaServer.java`**: Código del servidor que envía mensajes codificados con checksum de Fletcher-16 y Hamming.

### Cliente (Python)
- **`client.py`**: Código del cliente que recibe el mensaje y verifica si hay errores.

## Requisitos

- **Java 8 o superior**
- **Python 3.x**

## Uso

### Ir al algoritmo que se desea utilizar

1. **Fletcher**:
   ```bash
   cd Fletcher
   ```
2. **Hamming**:
   ```bash
    cd Hamming
    ```

### Ejecutar el Servidor

1. **Compilar el código Java**:
   ```bash
   javac JavaServer.java
   ```
2. **Ejecutar el servidor**:
   ```bash
    java JavaServer
    ```
3. **Introducir el mensaje binario**:
    ```bash
     Ingrese el mensaje binario a enviar:
     1010101010101010
     ```

### Ejecutar el Cliente
1. **Ejecutar el cliente**:
   ```bash
   python pythonScript.py
   ```