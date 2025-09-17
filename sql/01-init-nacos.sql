-- Nacos数据库初始化脚本
-- 在MySQL启动时自动执行

CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建nacos用户并授权（可选，如果使用现有用户）
-- CREATE USER IF NOT EXISTS 'nacos'@'%' IDENTIFIED BY 'nacos123';
-- GRANT ALL PRIVILEGES ON nacos.* TO 'nacos'@'%';
-- FLUSH PRIVILEGES;