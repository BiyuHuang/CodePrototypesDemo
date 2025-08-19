import socket
import threading
import json

HOST = '0.0.0.0'
PORT = 5000

# 记录已注册客户端
clients = {}

def handle_client(conn, addr):
    print(f"[INFO] New connection from {addr}")
    with conn:
        while True:
            try:
                data = conn.recv(1024)
                if not data:
                    break
                try:
                    message = json.loads(data.decode('utf-8'))
                except Exception as e:
                    response = {'status': 'error', 'msg': 'Invalid JSON'}
                    conn.sendall(json.dumps(response).encode('utf-8'))
                    continue
                cmd = message.get('cmd')
                if cmd == 'REGISTER':
                    client_id = message.get('client_id', str(addr))
                    clients[client_id] = {'addr': addr, 'status': 'online'}
                    response = {'status': 'ok', 'msg': f'Registered {client_id}'}
                elif cmd == 'HEARTBEAT':
                    client_id = message.get('client_id', str(addr))
                    if client_id in clients:
                        clients[client_id]['status'] = 'online'
                        response = {'status': 'ok', 'msg': 'Heartbeat received'}
                    else:
                        response = {'status': 'error', 'msg': 'Client not registered'}
                elif cmd == 'STATUS':
                    client_id = message.get('client_id', str(addr))
                    status = clients.get(client_id, {'status': 'unknown'})['status']
                    response = {'status': 'ok', 'client_status': status}
                elif cmd == 'COMMAND':
                    # 这里只做回显，实际可扩展
                    response = {'status': 'ok', 'msg': 'Command received', 'command': message.get('command')}
                else:
                    response = {'status': 'error', 'msg': 'Unknown command'}
                conn.sendall(json.dumps(response).encode('utf-8'))
            except Exception as e:
                print(f"[ERROR] {e}")
                break
    print(f"[INFO] Connection from {addr} closed.")

def start_server():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen()
        print(f"[INFO] MCP Server listening on {HOST}:{PORT}")
        while True:
            conn, addr = s.accept()
            t = threading.Thread(target=handle_client, args=(conn, addr), daemon=True)
            t.start()

if __name__ == '__main__':
    start_server() 