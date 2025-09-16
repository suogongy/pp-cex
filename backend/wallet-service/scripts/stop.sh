#!/bin/bash

# 钱包服务停止脚本

# 查找钱包服务进程
PID=$(ps aux | grep "wallet-service-1.0.0.jar" | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "钱包服务未运行"
    exit 0
fi

echo "正在停止钱包服务，进程ID: $PID"

# 优雅停止
kill -15 $PID

# 等待进程停止
sleep 5

# 检查进程是否还存在
if ps -p $PID > /dev/null; then
    echo "强制停止进程"
    kill -9 $PID
fi

echo "钱包服务已停止"