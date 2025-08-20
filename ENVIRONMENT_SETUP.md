# 环境配置说明

## 配置文件设置

本项目为了安全考虑，敏感配置信息不会上传到GitHub。请按照以下步骤配置您的环境：

### 1. 数据库配置

#### Mall模块
```bash
# 复制配置文件模板
cp mall/src/main/resources/application-test.yml.template mall/src/main/resources/application-test.yml
cp mall/src/main/resources/application-prod.yml.template mall/src/main/resources/application-prod.yml
```

#### Pay模块
```bash
# 复制配置文件模板
cp pay/src/main/resources/application-test.yml.template pay/src/main/resources/application-test.yml
cp pay/src/main/resources/application-prod.yml.template pay/src/main/resources/application-prod.yml
```

### 2. 配置信息填写

#### 数据库配置
- **数据库地址**: 修改 `url` 中的主机地址和端口
- **用户名**: 替换 `your_db_username` 为实际数据库用户名
- **密码**: 替换 `your_db_password` 为实际数据库密码

#### Redis配置
- **主机地址**: 修改 `host` 为Redis服务器地址
- **端口**: 默认6379，如有变更请修改
- **密码**: 如果Redis设置了密码，请取消注释并填入

#### RabbitMQ配置
- **地址**: 修改 `addresses` 为RabbitMQ服务器地址
- **端口**: 默认5672
- **用户名**: 替换 `your_rabbitmq_username`
- **密码**: 替换 `your_rabbitmq_password`

#### 微信支付配置（Pay模块）
- **AppId**: 替换 `your_wx_app_id` 为微信应用ID
- **商户号**: 替换 `your_wx_mch_id` 为微信商户号
- **商户密钥**: 替换 `your_wx_mch_key` 为微信商户密钥
- **回调地址**: 修改 `notifyUrl` 和 `returnUrl` 为您的实际域名

### 3. 数据库初始化

请参考项目概述文档中的数据库模型部分，创建相应的数据库表结构。

### 4. 环境验证

配置完成后，可以通过以下命令验证环境是否正确：

```bash
# 测试Mall模块
cd mall
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 测试Pay模块
cd pay
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## 安全提醒

⚠️ **重要**: 
- 请勿将包含真实配置信息的 `application-test.yml` 和 `application-prod.yml` 文件提交到版本控制系统
- 生产环境请使用HTTPS协议
- 定期更换数据库密码和API密钥
- 确保服务器防火墙配置正确

## 故障排除

如果遇到配置问题，请检查：
1. 数据库服务是否正常运行
2. Redis服务是否可访问
3. RabbitMQ服务是否正常
4. 网络连接是否正常
5. 配置文件格式是否正确（YAML格式对缩进敏感）