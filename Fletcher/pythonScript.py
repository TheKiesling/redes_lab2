import socket

def fletcher16(data):
    sum1 = 0xFF
    sum2 = 0xFF
    for byte in data:
        sum1 = (sum1 + byte) % 255
        sum2 = (sum2 + sum1) % 255
    return (sum2 << 8) | sum1

def pad_message(message, block_size):
    padding_needed = block_size - (len(message) % block_size)
    if padding_needed != block_size:
        message += '0' * padding_needed
    return message

def receiveMessage():
    host = 'localhost'
    port = 12345
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((host, port))
            receivedMessage = s.recv(1024).decode('utf-8').strip()
            print("Mensaje recibido:", receivedMessage)
            
            # Extraer checksum recibido
            receivedChecksum = int(receivedMessage[-4:], 16)
            message = receivedMessage[:-4]
            
            # Determinar el tamaño del bloque y aplicar padding si es necesario
            if len(message) % 32 == 0:
                block_size = 32
            elif len(message) % 16 == 0:
                block_size = 16
            else:
                block_size = 8
            
            message = pad_message(message, block_size)
            
            # Calcular checksum del mensaje recibido con padding
            message_bytes = message.encode('utf-8')
            calculatedChecksum = fletcher16(message_bytes)
            
            if calculatedChecksum == receivedChecksum:
                print("El mensaje es válido.")
            else:
                print("El mensaje contiene errores.")
    except ConnectionRefusedError:
        print("Conexión rechazada")

if __name__ == "__main__":
    receiveMessage()
