import subprocess
import socket
import time
import random
import string
import pandas as pd
import matplotlib.pyplot as plt
import json
import numpy as np
from collections import Counter

def start_server():
    subprocess.run(["javac", "Server.java"])
    server_process = subprocess.Popen(["java", "Server"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(1)
    return server_process

def stop_server(server_process):
    server_process.terminate()
    server_process.wait()

def generate_random_message(length):
    return ''.join(random.choice(string.ascii_lowercase) for _ in range(length))

def binary_to_text(binary):
    message = ''.join([chr(int(binary[i:i+8], 2)) for i in range(0, len(binary), 8)])
    return message

def decode_hamming(encoded):
    r = 0
    while 2 ** r < len(encoded):
        r += 1
        
    while True:
        parityBits = [encoded[2 ** i - 1] for i in range(r)]
        errors = []
        
        for i in range(r):
            parity = get_parity(2 ** i, r, encoded)
            errors.insert(0, '1' if parity != parityBits[i] else '0')
        
        if "1" in errors:
            errPos = int("".join(errors), 2)
            if errPos >= len(encoded):
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

def decode_fletcher(encoded):
    receivedChecksum = int(encoded[-4:], 16)
    message = encoded[:-4]
    message_bytes = message.encode('utf-8')
    calculatedChecksum = fletcher16(message_bytes)
    
    if calculatedChecksum == receivedChecksum:
        return binary_to_text(message), False
    else:
        return binary_to_text(message), True

def fletcher16(data):
    sum1 = 0xFF
    sum2 = 0xFF
    for byte in data:
        sum1 = (sum1 + byte) % 255
        sum2 = (sum2 + sum1) % 255
    return (sum2 << 8) | sum1

def get_parity(p, r, message):
    counter = 0
    length = len(message)
    
    for i in range(1, length + 1):
        if (i & (i - 1)) == 0:
            continue
        if p in decompose(i, r) and message[i - 1] == '1':
            counter += 1
            
    return '0' if counter % 2 == 0 else '1'

def decompose(position, r):
    components = []
    for i in range(r - 1, -1, -1):
        power = 2 ** i
        if position >= power:
            components.append(power)
            position -= power
    return components

def send_messages_to_server(messages, probabilities):
    host = 'localhost'
    port = 12345
    results = []
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((host, port))
            for message in messages:
                
                error_probability = probabilities[random.randint(0, len(probabilities)-1)]
                
                data = {
                    "message": message,
                    "probability": error_probability
                }
                
                serialized_data = json.dumps(data).encode('utf-8')
                
                s.sendall(serialized_data+ b'\n')
                #s.sendall((message + '\n').encode('utf-8'))
                response = s.recv(1024).decode('utf-8').strip()
                
                result = {
                    'original_message': message,
                    'encoded_message': response,
                    'algorithm': '',
                    'error': False,
                    'probability': error_probability
                }
                
                if response[0] == 'H':
                    result['algorithm'] = 'Hamming'
                    decoded_binary = decode_hamming(response[1:])
                    decoded_message = binary_to_text(decoded_binary)
                    result['decoded_message'] = decoded_message
                    result['error'] = response[1:] != decoded_binary
                elif response[0] == 'F':
                    result['algorithm'] = 'Fletcher'
                    decoded_message, error = decode_fletcher(response[1:])
                    result['decoded_message'] = decoded_message
                    result['error'] = error
                else:
                    result['algorithm'] = 'Unknown'
                
                results.append(result)
                
            s.close()
    except ConnectionRefusedError:
        print("Conexión rechazada")
    except Exception as e:
        print(f"Error: {e}")
    
    return results

def main():
    num_tests = 1000
    error_probabilities = [1./100, 1./75, 1./50, 1./25]
    # message_length = 10

    messages = [generate_random_message(random.randint(10,100)) for _ in range(num_tests)]

    server_process = start_server()
    
    results = send_messages_to_server(messages, error_probabilities)
    stop_server(server_process)
    
    df = pd.DataFrame(results)
    
    df.to_csv('results.csv', index=False)
    
    error_counts = df['error'].value_counts()
    algorithms = df['algorithm'].value_counts()

    plt.figure(figsize=(12, 6))

    plt.subplot(1, 2, 1)
    error_counts.plot(kind='bar')
    plt.title('Errores detectados')
    plt.xlabel('Error')
    plt.ylabel('Cantidad')

    plt.subplot(1, 2, 2)
    algorithms.plot(kind='bar')
    plt.title('Algoritmos utilizados')
    plt.xlabel('Algoritmo')
    plt.ylabel('Cantidad')

    plt.tight_layout()
    plt.show()
    
    fig, ax = plt.subplots(figsize=(10, 6))
    width = 0.25

    x = np.arange(len(error_probabilities))
    
    frequencies = df['algorithm'].value_counts().to_dict()

    for i, (algorithm, frequencies) in enumerate(frequencies.items()):
        ax.bar(x + i*width, frequencies, width, label=algorithm)

    ax.set_xlabel('Probabilidad de Error')
    ax.set_ylabel('Frecuencia de Errores')
    ax.set_title('Frecuencia de Errores según Algoritmo y Probabilidad de Error')
    ax.set_xticks(x + width)
    ax.set_xticklabels(error_probabilities)
    ax.legend()

    plt.show()


if __name__ == "__main__":
    main()
