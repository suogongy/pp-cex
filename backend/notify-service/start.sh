#!/bin/bash

# 通知服务启动脚本

echo "启动通知服务..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境"
    exit 1
fi

# 检查jar包是否存在
JAR_FILE="target/notify-service-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 未找到jar包 $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

# 设置JVM参数
JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=dev"

# 启动服务
echo "启动参数: $JAVA_OPTS"
echo "jar包: $JAR_FILE"

java $JAVA_OPTS -jar $JAR_FILE