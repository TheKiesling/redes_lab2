import socket

def decompose(position, r):
    components = []
    for i in range(r - 1, -1, -1):
        power = 2 ** i
        if position >= power:
            components.append(power)
            position -= power
    return components

def getParity(p, r, message):
    counter = 0
    length = len(message)
    
    for i in range(1, length + 1):
        if (i & (i - 1)) == 0:
            continue
        if p in decompose(i, r) and message[i - 1] == '1':
            counter += 1
            
    return '0' if counter % 2 == 0 else '1'

def hammingDecoding(encoded):
    r = 0
    while 2 ** r < len(encoded):
        r += 1
        
    while True: # Intentará evaluar y corregir todos los errores posibles
        parityBits = [encoded[2 ** i - 1] for i in range(r)]
        errors = []
        
        for i in range(r):
            parity = getParity(2 ** i, r, encoded)
            errors.insert(0, '1' if parity != parityBits[i] else '0')
        
        if "1" in errors:
            errPos = int("".join(errors), 2)
            prevState = encoded[errPos - 1]
            correctedBit = '0' if prevState == '1' else '1'
            encoded = encoded[:errPos - 1] + correctedBit + encoded[errPos:]
            print(f"Error en posición {errPos}. Se corrigió de '{prevState}' a '{correctedBit}'.")
            print(f"Mensaje corregido: {encoded}")
        else:
            break
            
    decoded = []
    for i in range(len(encoded)):
        if (i + 1 & (i)) == 0:
            continue
        decoded.append(encoded[i])
    
    return "".join(decoded)

def receiveMessage():
    host = 'localhost'
    port = 12345
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((host, port))
            while True:
                try:
                    receivedMessage = s.recv(1024).decode('utf-8').strip()
                    if not receivedMessage:
                        break
                    print("Mensaje recibido:", receivedMessage)
                    decodedMessage = hammingDecoding(receivedMessage)
                    print("Mensaje decodificado:", decodedMessage)
                except (ConnectionResetError, ConnectionAbortedError):
                    print("Error de conexión con el servidor.")
                    break
    except ConnectionRefusedError:
        print("Conexión rechazada")

if __name__ == "__main__":
    receiveMessage()
