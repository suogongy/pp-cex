-- =============================================
-- Nacos配置初始化脚本
-- 用于user-service的Nacos配置中心配置
-- =============================================

-- 创建命名空间（如果不存在）
-- 注意：Nacos 2.2+版本需要在控制台手动创建命名空间
-- 命名空间ID: test
-- 命名空间名称: 测试环境
-- 描述: 用户服务测试环境配置

-- =============================================
-- 用户服务专用配置
-- Data ID: user-service.yaml
-- Group: user-service
-- =============================================
INSERT INTO config_info (data_id, group_id, content, md5, gmt_create, gmt_modified, src_user, src_ip, app_name, tenant_id, c_desc, c_use, effect, type, c_schema, encrypted_data_key)
VALUES (
    'user-service.yaml',
    'user-service',
    '# 用户服务专用配置\nspring:\n  application:\n    name: user-service\n\n# 数据源配置\nspring:\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ppcex_user}?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true\n    username: ${MYSQL_USER:cex_user}\n    password: ${MYSQL_PASSWORD:cex123}\n    hikari:\n      minimum-idle: 10\n      maximum-pool-size: 50\n      connection-timeout: 30000\n      idle-timeout: 600000\n      max-lifetime: 1800000\n      connection-test-query: SELECT 1\n      pool-name: UserHikariCP\n\n# JPA配置\nspring:\n  jpa:\n    hibernate:\n      ddl-auto: none\n    show-sql: false\n    properties:\n      hibernate:\n        dialect: org.hibernate.dialect.MySQLDialect\n        format_sql: true\n\n# Redisson配置\nspring:\n  redisson:\n    address: redis://${REDIS_HOST:localhost}:${REDIS_PORT:6379}\n    password: ${REDIS_PASSWORD:}\n    database: 0\n    timeout: 10000\n    connectionPoolSize: 64\n    connectionMinimumIdleSize: 24\n    slaveConnectionMinimumIdleSize: 24\n    slaveConnectionPoolSize: 64\n    masterConnectionPoolSize: 64\n    idleConnectionTimeout: 10000\n    pingConnectionInterval: 30000\n    pingTimeout: 1000\n    connectTimeout: 10000\n    retryAttempts: 3\n    retryInterval: 1500\n    reconnectionTimeout: 3000\n    failedAttempts: 3\n    subscriptionsPerConnection: 5\n    subscriptionConnectionMinimumIdleSize: 1\n    subscriptionConnectionPoolSize: 50\n    monitorInterval: 3000\n\n# 业务配置\ncex:\n  user:\n    # 密码加密盐值\n    password-salt: ${PASSWORD_SALT:default-salt-value}\n    # 默认密码\n    default-password: ${DEFAULT_PASSWORD:User123456}\n    # 注册奖励\n    register-reward: 0\n    # 邀请奖励\n    invite-reward: 0\n    # KYC审核时间（小时）\n    kyc-audit-hours: 24\n    # 最大登录失败次数\n    max-login-fail-times: 5\n    # 登录锁定时间（分钟）\n    login-lock-minutes: 30\n    # 谷歌验证器配置\n    google-auth:\n      enabled: ${GOOGLE_AUTH_ENABLED:true}\n      issuer: ${GOOGLE_AUTH_ISSUE:CEX}\n      window-size: 3\n      code-digits: 6\n    # 邮箱验证\n    email-verification:\n      enabled: ${EMAIL_VERIFICATION_ENABLED:true}\n      expire-minutes: 30\n    # 手机验证\n    phone-verification:\n      enabled: ${PHONE_VERIFICATION_ENABLED:true}\n      expire-minutes: 10\n\n# JWT配置\njwt:\n  secret: ${JWT_SECRET:your-jwt-secret-key-at-least-32-bytes-long-for-security}\n  expiration: 86400000 # 24小时\n  refresh-expiration: 604800000 # 7天\n\n# 环境标识\nenv:\n  name: test\n  version: 1.0.0',
    MD5('# 用户服务专用配置\nspring:\n  application:\n    name: user-service\n\n# 数据源配置\nspring:\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ppcex_user}?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true\n    username: ${MYSQL_USER:cex_user}\n    password: ${MYSQL_PASSWORD:cex123}\n    hikari:\n      minimum-idle: 10\n      maximum-pool-size: 50\n      connection-timeout: 30000\n      idle-timeout: 600000\n      max-lifetime: 1800000\n      connection-test-query: SELECT 1\n      pool-name: UserHikariCP\n\n# JPA配置\nspring:\n  jpa:\n    hibernate:\n      ddl-auto: none\n    show-sql: false\n    properties:\n      hibernate:\n        dialect: org.hibernate.dialect.MySQLDialect\n        format_sql: true\n\n# Redisson配置\nspring:\n  redisson:\n    address: redis://${REDIS_HOST:localhost}:${REDIS_PORT:6379}\n    password: ${REDIS_PASSWORD:}\n    database: 0\n    timeout: 10000\n    connectionPoolSize: 64\n    connectionMinimumIdleSize: 24\n    slaveConnectionMinimumIdleSize: 24\n    slaveConnectionPoolSize: 64\n    masterConnectionPoolSize: 64\n    idleConnectionTimeout: 10000\n    pingConnectionInterval: 30000\n    pingTimeout: 1000\n    connectTimeout: 10000\n    retryAttempts: 3\n    retryInterval: 1500\n    reconnectionTimeout: 3000\n    failedAttempts: 3\n    subscriptionsPerConnection: 5\n    subscriptionConnectionMinimumIdleSize: 1\n    subscriptionConnectionPoolSize: 50\n    monitorInterval: 3000\n\n# 业务配置\ncex:\n  user:\n    # 密码加密盐值\n    password-salt: ${PASSWORD_SALT:default-salt-value}\n    # 默认密码\n    default-password: ${DEFAULT_PASSWORD:User123456}\n    # 注册奖励\n    register-reward: 0\n    # 邀请奖励\n    invite-reward: 0\n    # KYC审核时间（小时）\n    kyc-audit-hours: 24\n    # 最大登录失败次数\n    max-login-fail-times: 5\n    # 登录锁定时间（分钟）\n    login-lock-minutes: 30\n    # 谷歌验证器配置\n    google-auth:\n      enabled: ${GOOGLE_AUTH_ENABLED:true}\n      issuer: ${GOOGLE_AUTH_ISSUE:CEX}\n      window-size: 3\n      code-digits: 6\n    # 邮箱验证\n    email-verification:\n      enabled: ${EMAIL_VERIFICATION_ENABLED:true}\n      expire-minutes: 30\n    # 手机验证\n    phone-verification:\n      enabled: ${PHONE_VERIFICATION_ENABLED:true}\n      expire-minutes: 10\n\n# JWT配置\njwt:\n  secret: ${JWT_SECRET:your-jwt-secret-key-at-least-32-bytes-long-for-security}\n  expiration: 86400000 # 24小时\n  refresh-expiration: 604800000 # 7天\n\n# 环境标识\nenv:\n  name: test\n  version: 1.0.0'),
    NOW(),
    NOW(),
    NULL,
    '127.0.0.1',
    'user-service',
    'test',
    '用户服务专用配置',
    NULL,
    NULL,
    'yaml',
    NULL,
    NULL
);

-- =============================================
-- 公共配置
-- Data ID: common-config.yaml
-- Group: user-service
-- =============================================
INSERT INTO config_info (data_id, group_id, content, md5, gmt_create, gmt_modified, src_user, src_ip, app_name, tenant_id, c_desc, c_use, effect, type, c_schema, encrypted_data_key)
VALUES (
    'common-config.yaml',
    'user-service',
    '# 公共配置\nspring:\n  main:\n    allow-bean-definition-overriding: true\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n    time-zone: UTC\n\n# 监控配置\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: health,info,metrics,prometheus\n  endpoint:\n    health:\n      show-details: always\n  metrics:\n    export:\n      prometheus:\n        enabled: true\n  tracing:\n    sampling:\n      probability: 1.0\n\n# 日志配置\nlogging:\n  level:\n    root: INFO\n    com.ppcex.user: ${LOG_LEVEL:INFO}\n    com.ppcex.common: INFO\n    org.springframework.security: DEBUG\n    org.springframework.web: DEBUG\n  pattern:\n    console: \"%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n\"\n    file: \"%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n\"\n  file:\n    name: logs/user-service.log\n    max-size: 100MB\n    max-history: 30\n\n# API文档配置\nspringdoc:\n  swagger-ui:\n    path: /swagger-ui.html\n    tags-sorter: alpha\n    operations-sorter: alpha\n  api-docs:\n    path: /v3/api-docs\n    enabled: true\n    show-actuator: true\n    default-produces-media-type: application/json\n    default-consumes-media-type: application/json\n\nknife4j:\n  enable: true\n  openapi:\n    title: 用户服务API文档\n    description: \"用户服务接口文档\"\n    email: admin@example.com\n    concat: admin\n    url: https://www.example.com\n    version: v1.0\n    license: Apache 2.0\n    license-url: https://www.apache.org/licenses/LICENSE-2.0\n    terms-of-service-url: https://www.example.com/terms\n  setting:\n    language: zh_cn\n    enable-version: true\n    enable-swagger-models: true\n    swagger-model-name: 实体类列表',
    MD5('# 公共配置\nspring:\n  main:\n    allow-bean-definition-overriding: true\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n    time-zone: UTC\n\n# 监控配置\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: health,info,metrics,prometheus\n  endpoint:\n    health:\n      show-details: always\n  metrics:\n    export:\n      prometheus:\n        enabled: true\n  tracing:\n    sampling:\n      probability: 1.0\n\n# 日志配置\nlogging:\n  level:\n    root: INFO\n    com.ppcex.user: ${LOG_LEVEL:INFO}\n    com.ppcex.common: INFO\n    org.springframework.security: DEBUG\n    org.springframework.web: DEBUG\n  pattern:\n    console: \"%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n\"\n    file: \"%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n\"\n  file:\n    name: logs/user-service.log\n    max-size: 100MB\n    max-history: 30\n\n# API文档配置\nspringdoc:\n  swagger-ui:\n    path: /swagger-ui.html\n    tags-sorter: alpha\n    operations-sorter: alpha\n  api-docs:\n    path: /v3/api-docs\n    enabled: true\n    show-actuator: true\n    default-produces-media-type: application/json\n    default-consumes-media-type: application/json\n\nknife4j:\n  enable: true\n  openapi:\n    title: 用户服务API文档\n    description: \"用户服务接口文档\"\n    email: admin@example.com\n    concat: admin\n    url: https://www.example.com\n    version: v1.0\n    license: Apache 2.0\n    license-url: https://www.apache.org/licenses/LICENSE-2.0\n    terms-of-service-url: https://www.example.com/terms\n  setting:\n    language: zh_cn\n    enable-version: true\n    enable-swagger-models: true\n    swagger-model-name: 实体类列表'),
    NOW(),
    NOW(),
    NULL,
    '127.0.0.1',
    'user-service',
    'test',
    '公共配置',
    NULL,
    NULL,
    'yaml',
    NULL,
    NULL
);

-- =============================================
-- Redis配置
-- Data ID: redis-config.yaml
-- Group: user-service
-- =============================================
INSERT INTO config_info (data_id, group_id, content, md5, gmt_create, gmt_modified, src_user, src_ip, app_name, tenant_id, c_desc, c_use, effect, type, c_schema, encrypted_data_key)
VALUES (
    'redis-config.yaml',
    'user-service',
    '# Redis配置\nspring:\n  data:\n    redis:\n      host: ${REDIS_HOST:localhost}\n      port: ${REDIS_PORT:6379}\n      username: ${REDIS_USER:cex_user}\n      password: ${REDIS_PASSWORD:redis123}\n      database: 0\n      timeout: 10000ms\n      lettuce:\n        pool:\n          max-active: 200\n          max-wait: -1ms\n          max-idle: 10\n          min-idle: 0\n\n# Redis缓存配置\nspring:\n  cache:\n    type: redis\n    redis:\n      time-to-live: 600000 # 10分钟\n      cache-null-values: false\n      use-key-prefix: true\n      key-prefix: \"user:\"\n\n# Redis session配置\nspring:\n  session:\n    store-type: redis\n    redis:\n      namespace: user:session\n    timeout: 1800 # 30分钟',
    MD5('# Redis配置\nspring:\n  data:\n    redis:\n      host: ${REDIS_HOST:localhost}\n      port: ${REDIS_PORT:6379}\n      username: ${REDIS_USER:cex_user}\n      password: ${REDIS_PASSWORD:redis123}\n      database: 0\n      timeout: 10000ms\n      lettuce:\n        pool:\n          max-active: 200\n          max-wait: -1ms\n          max-idle: 10\n          min-idle: 0\n\n# Redis缓存配置\nspring:\n  cache:\n    type: redis\n    redis:\n      time-to-live: 600000 # 10分钟\n      cache-null-values: false\n      use-key-prefix: true\n      key-prefix: \"user:\"\n\n# Redis session配置\nspring:\n  session:\n    store-type: redis\n    redis:\n      namespace: user:session\n    timeout: 1800 # 30分钟'),
    NOW(),
    NOW(),
    NULL,
    '127.0.0.1',
    'user-service',
    'test',
    'Redis配置',
    NULL,
    NULL,
    'yaml',
    NULL,
    NULL
);

-- =============================================
-- MySQL配置
-- Data ID: mysql-config.yaml
-- Group: user-service
-- =============================================
INSERT INTO config_info (data_id, group_id, content, md5, gmt_create, gmt_modified, src_user, src_ip, app_name, tenant_id, c_desc, c_use, effect, type, c_schema, encrypted_data_key)
VALUES (
    'mysql-config.yaml',
    'user-service',
    '# MySQL配置\nspring:\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ppcex_user}?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true\n    username: ${MYSQL_USER:cex_user}\n    password: ${MYSQL_PASSWORD:cex123}\n    hikari:\n      minimum-idle: 10\n      maximum-pool-size: 50\n      connection-timeout: 30000\n      idle-timeout: 600000\n      max-lifetime: 1800000\n      connection-test-query: SELECT 1\n      pool-name: UserHikariCP\n\n# 数据库初始化配置\nspring:\n  sql:\n    init:\n      mode: never\n      schema-locations: classpath:db/schema.sql\n      data-locations: classpath:db/data.sql\n      username: ${MYSQL_USER:cex_user}\n      password: ${MYSQL_PASSWORD:cex123}\n\n# MyBatis Plus配置\nmybatis-plus:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.ppcex.user.entity\n  configuration:\n    map-underscore-to-camel-case: true\n    cache-enabled: true\n    lazy-loading-enabled: true\n    multiple-result-sets-enabled: true\n    use-column-label: true\n    use-generated-keys: false\n    auto-mapping-behavior: partial\n    default-statement-timeout: 25000\n  global-config:\n    db-config:\n      id-type: auto\n      table-underline: true\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0',
    MD5('# MySQL配置\nspring:\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:ppcex_user}?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true\n    username: ${MYSQL_USER:cex_user}\n    password: ${MYSQL_PASSWORD:cex123}\n    hikari:\n      minimum-idle: 10\n      maximum-pool-size: 50\n      connection-timeout: 30000\n      idle-timeout: 600000\n      max-lifetime: 1800000\n      connection-test-query: SELECT 1\n      pool-name: UserHikariCP\n\n# 数据库初始化配置\nspring:\n  sql:\n    init:\n      mode: never\n      schema-locations: classpath:db/schema.sql\n      data-locations: classpath:db/data.sql\n      username: ${MYSQL_USER:cex_user}\n      password: ${MYSQL_PASSWORD:cex123}\n\n# MyBatis Plus配置\nmybatis-plus:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.ppcex.user.entity\n  configuration:\n    map-underscore-to-camel-case: true\n    cache-enabled: true\n    lazy-loading-enabled: true\n    multiple-result-sets-enabled: true\n    use-column-label: true\n    use-generated-keys: false\n    auto-mapping-behavior: partial\n    default-statement-timeout: 25000\n  global-config:\n    db-config:\n      id-type: auto\n      table-underline: true\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0'),
    NOW(),
    NOW(),
    NULL,
    '127.0.0.1',
    'user-service',
    'test',
    'MySQL配置',
    NULL,
    NULL,
    'yaml',
    NULL,
    NULL
);

-- =============================================
-- RocketMQ配置
-- Data ID: rocketmq-config.yaml
-- Group: user-service
-- =============================================
INSERT INTO config_info (data_id, group_id, content, md5, gmt_create, gmt_modified, src_user, src_ip, app_name, tenant_id, c_desc, c_use, effect, type, c_schema, encrypted_data_key)
VALUES (
    'rocketmq-config.yaml',
    'user-service',
    '# RocketMQ配置\nrocketmq:\n  name-server: ${ROCKETMQ_NAME_SERVER_ADDR:localhost:9876}\n  producer:\n    group: user-producer-group\n    send-message-timeout: 3000\n    retry-times-when-send-failed: 3\n    max-message-size: 4194304\n    compress-message-body-threshold: 4096\n  consumer:\n    # 用户消息消费者\n    user-consumer:\n      group: user-consumer-group\n      topic: user-topic\n      selector-expression: \"*\"\n      consume-thread-min: 20\n      consume-thread-max: 64\n      pull-batch-size: 32\n    # 用户注册消费者\n    register-consumer:\n      group: register-consumer-group\n      topic: register-topic\n      selector-expression: \"*\"\n      consume-thread-min: 10\n      consume-thread-max: 20\n      pull-batch-size: 16\n    # 用户登录消费者\n    login-consumer:\n      group: login-consumer-group\n      topic: login-topic\n      selector-expression: \"*\"\n      consume-thread-min: 10\n      consume-thread-max: 20\n      pull-batch-size: 16\n\n# 消息轨迹配置\nrocketmq:\n  enable-msg-trace: true\n  customized-trace-topic: rmq_sys_trace_topic\n  access-key: ${ROCKETMQ_ACCESS_KEY:}\n  secret-key: ${ROCKETMQ_SECRET_KEY:}\n\n# ACL配置\nrocketmq:\n  acl:\n    enable: false\n    access-key: ${ROCKETMQ_ACCESS_KEY:}\n    secret-key: ${ROCKETMQ_SECRET_KEY:}',
    MD5('# RocketMQ配置\nrocketmq:\n  name-server: ${ROCKETMQ_NAME_SERVER_ADDR:localhost:9876}\n  producer:\n    group: user-producer-group\n    send-message-timeout: 3000\n    retry-times-when-send-failed: 3\n    max-message-size: 4194304\n    compress-message-body-threshold: 4096\n  consumer:\n    # 用户消息消费者\n    user-consumer:\n      group: user-consumer-group\n      topic: user-topic\n      selector-expression: \"*\"\n      consume-thread-min: 20\n      consume-thread-max: 64\n      pull-batch-size: 32\n    # 用户注册消费者\n    register-consumer:\n      group: register-consumer-group\n      topic: register-topic\n      selector-expression: \"*\"\n      consume-thread-min: 10\n      consume-thread-max: 20\n      pull-batch-size: 16\n    # 用户登录消费者\n    login-consumer:\n      group: login-consumer-group\n      topic: login-topic\n      selector-expression: \"*\"\n      consume-thread-min: 10\n      consume-thread-max: 20\n      pull-batch-size: 16\n\n# 消息轨迹配置\nrocketmq:\n  enable-msg-trace: true\n  customized-trace-topic: rmq_sys_trace_topic\n  access-key: ${ROCKETMQ_ACCESS_KEY:}\n  secret-key: ${ROCKETMQ_SECRET_KEY:}\n\n# ACL配置\nrocketmq:\n  acl:\n    enable: false\n    access-key: ${ROCKETMQ_ACCESS_KEY:}\n    secret-key: ${ROCKETMQ_SECRET_KEY:}'),
    NOW(),
    NOW(),
    NULL,
    '127.0.0.1',
    'user-service',
    'test',
    'RocketMQ配置',
    NULL,
    NULL,
    'yaml',
    NULL,
    NULL
);

-- =============================================
-- 生产环境配置示例（需要手动创建）
-- Data ID: user-service-prod.yaml
-- Group: user-service
-- Description: 生产环境配置模板
-- =============================================

-- 配置历史记录查询
-- SELECT * FROM config_info WHERE tenant_id = 'test' AND group_id = 'user-service' ORDER BY gmt_create DESC;

-- 配置变更历史查询
-- SELECT * FROM config_history WHERE tenant_id = 'test' AND group_id = 'user-service' ORDER BY gmt_modified DESC;