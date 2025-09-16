#!/bin/bash

# User Service 停止脚本

echo "Stopping User Service..."

# 查找并停止User Service进程
PID=$(ps -ef | grep java | grep user-service | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "User Service is not running."
else
    echo "Found User Service process with PID: $PID"
    kill $PID

    # 等待进程结束
    sleep 5

    # 检查进程是否还在运行
    if ps -p $PID > /dev/null; then
        echo "Force stopping User Service..."
        kill -9 $PID
    fi

    echo "User Service stopped successfully."
fi