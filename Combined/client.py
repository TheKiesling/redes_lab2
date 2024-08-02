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

def fletcherDecoding(encoded):
    receivedChecksum = int(encoded[-4:], 16)
    message = encoded[:-4]
    
    if len(message) % 32 == 0:
        block_size = 32
    elif len(message) % 16 == 0:
        block_size = 16
    else:
        block_size = 8
    
    message = pad_message(message, block_size)
    message_bytes = message.encode('utf-8')
    calculatedChecksum = fletcher16(message_bytes)
    
    if calculatedChecksum == receivedChecksum:
        return "El mensaje es válido."
    else:
        return "El mensaje contiene errores."

def hammingDecoding(encoded):
    r = 0
    while 2 ** r < len(encoded):
        r += 1
        
    while True:
        parityBits = [encoded[2 ** i - 1] for i in range(r)]
        errors = []
        
        for i in range(r):
            parity = getParity(2 ** i, r, encoded)
            errors.insert(0, '1' if parity != parityBits[i] else '0')
        
        if "1" in errors:
            errPos = int("".join(errors), 2)
            if(errPos >= len(encoded)):
                errPos = len(encoded)
            prevState = encoded[errPos - 1]
            correctedBit = '0' if prevState == '1' else '1'
            encoded = encoded[:errPos - 1] + correctedBit + encoded[errPos:]
        else:
            break
            
    decoded = []
    for i in range(len(encoded)):
        if (i + 1 & (i)) == 0:
            continue
        decoded.append(encoded[i])
    
    return "".join(decoded)

def binaryToText(binary):
    message = ''.join([chr(int(binary[i:i+8], 2)) for i in range(0, len(binary), 8)])
    return message

def detectAlgorithm(encodedMessage):
    algorithm = encodedMessage[0]
    
    if (algorithm == 'H'):
        return [0, hammingDecoding(encodedMessage[1:])]
    elif (algorithm == 'F'):
        return [1, fletcherDecoding(encodedMessage[1:])]
    else:
        return None

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
                    print("Mensaje recibido (codificado):", receivedMessage)
                    
                    decodedMessage = detectAlgorithm(receivedMessage)
                    if decodedMessage is None:
                        print("Error al intentar decodificar mensaje: Algoritmo no reconocido.")
                    elif decodedMessage[0] == 0:
                        decodedText = binaryToText(decodedMessage[1])
                        print("Mensaje decodificado (texto):", decodedText)
                        print("SE USA Hamming")
                    else:
                        print("Mensaje decodificado:", decodedMessage[1])
                        print("SE USA Fletcher")
                except (ConnectionResetError, ConnectionAbortedError):
                    print("Error de conexión con el servidor.")
                    break
    except ConnectionRefusedError:
        print("Conexión rechazada")

if __name__ == "__main__":
    receiveMessage()
