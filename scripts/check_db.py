import socket
import sys

host = '127.0.0.1'
port = 30314

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(5)
    s.connect((host, port))
    print(f"Connected to {host}:{port}")
    
    # MySQL server sends a handshake packet first
    data = s.recv(1024)
    print(f"Received {len(data)} bytes")
    print(f"Hex: {data.hex()[:100]}...")
    print(f"Raw: {data[:100]}")
    
    s.close()
except Exception as e:
    print(f"Error: {e}")
