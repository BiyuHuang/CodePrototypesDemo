# MCP Server (Model Control Protocol)

本项目是一个基于 Python 的简单 MCP 服务器示例，支持 REGISTER、HEARTBEAT、STATUS、COMMAND 四种 JSON 命令。

## 功能
- 基于 TCP 的多线程服务器，监听 5000 端口
- 支持客户端注册、心跳、状态查询、命令下发
- 消息格式为 JSON

## 启动方法
```bash
python mcp_server.py
```

## 消息格式示例
所有消息均为 JSON 格式，编码为 UTF-8。

### REGISTER
```json
{"cmd": "REGISTER", "client_id": "client1"}
```

### HEARTBEAT
```json
{"cmd": "HEARTBEAT", "client_id": "client1"}
```

### STATUS
```json
{"cmd": "STATUS", "client_id": "client1"}
```

### COMMAND
```json
{"cmd": "COMMAND", "client_id": "client1", "command": "do_something"}
```

## 测试方法
可使用 `telnet` 或自定义 Python 客户端进行测试。

### Python 客户端示例
```python
import socket, json
s = socket.socket()
s.connect(('127.0.0.1', 5000))
msg = {"cmd": "REGISTER", "client_id": "client1"}
s.send(json.dumps(msg).encode('utf-8'))
print(s.recv(1024))
s.close()
```
