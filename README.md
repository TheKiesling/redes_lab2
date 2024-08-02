# Proyecto de Detección y Corrección de Errores con Checksum de Fletcher y Hamming

Este proyecto implementa técnicas de detección y corrección de errores utilizando el algoritmo de Hamming y el algoritmo de checksum de Fletcher-16. Está compuesto por un servidor que calcula y envía mensajes codificados, y un cliente que recibe y verifica los mensajes.

## Estructura del Proyecto

### Servidor (Java)
- **`Server.java`**: Código del servidor que envía mensajes codificados con checksum de Fletcher-16 y Hamming.

### Cliente (Python)
- **`client.py`**: Código del cliente que recibe el mensaje y verifica si hay errores.

### Cliente de pruebas (Python)
- **`test.py`**: Código que permite realizar consultas masivas al servidor para verificar el funcionamiento de ambos algoritmos.

## Requisitos

- **Java 8 o superior**
- **Python 3.x**

## Uso del cliente

### Ejecutar el Servidor

1. **Compilar el código Java**:
   ```bash
   javac Server.java
   ```
2. **Ejecutar el servidor**:
   ```bash
    java Server
    ```
3. **Introducir el mensaje binario**:
    ```bash
     Ingrese el mensaje binario a enviar:
     1010101010101010
     ```

3. **Seleccionar el algoritmo a utilizar**:
    ```bash
     Selecciona el algoritmo a utilizar:
     1. Hamming.
     2. Fletcher.
     1
     ```

### Ejecutar el Cliente
1. **Ejecutar el cliente**:
   ```bash
   python client.py
   ```

## Uso del entorno de pruebas

### Ejecutar el Servidor

1. **Compilar el código Java**:
   ```bash
   javac Server.java
   ```
2. **Ejecutar el servidor**:
   ```bash
    java Server
    ```

### Ejecutar el Cliente de pruebas
1. **Ejecutar el cliente**:
   ```bash
   python test.py
   ```