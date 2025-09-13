# MallPay 电商系统

[![Java](https://img.shields.io/badge/Java-1.8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.1.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-5.7+-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-3.0+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目简介

MallPay 是一个基于 Spring Boot 的现代化电商系统，采用微服务架构设计。系统包含商城核心服务和支付服务两个主要模块，提供完整的电商功能，包括用户管理、商品管理、购物车、订单处理和支付功能。

## 🏗️ 系统架构

```
mallpay/
├── mall/          # 商城核心服务 (端口: 9090)
│   ├── 用户管理
│   ├── 商品管理
│   ├── 购物车功能
│   ├── 订单管理
│   └── 收货地址管理
└── pay/           # 支付服务
    ├── 支付创建
    ├── 支付通知
    ├── 支付查询
    └── 支付回调
```

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 2.1.7
- **持久化**: MyBatis 2.1.0
- **数据库**: MySQL 5.7+
- **缓存**: Redis 3.0+
- **消息队列**: RabbitMQ 3.6+
- **支付SDK**: best-pay-sdk 1.3.0
- **模板引擎**: FreeMarker
- **分页插件**: PageHelper
- **工具库**: Lombok, Gson

### 支付平台
- 微信支付
- 支付宝支付

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 3.0+
- RabbitMQ 3.6+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/mallpay.git
   cd mallpay
   ```

2. **数据库配置**
   - 创建数据库 `mallpay`
   - 导入数据库脚本（位于各模块的 `sql` 目录）
   - 修改 `application.yml` 中的数据库连接配置

3. **Redis 配置**
   - 启动 Redis 服务
   - 修改 `application.yml` 中的 Redis 连接配置

4. **RabbitMQ 配置**
   - 启动 RabbitMQ 服务
   - 修改 `application.yml` 中的 RabbitMQ 连接配置

5. **构建项目**
   ```bash
   # 构建 mall 模块
   cd mall
   mvn clean package
   
   # 构建 pay 模块
   cd ../pay
   mvn clean package
   ```

6. **启动服务**
   ```bash
   # 启动支付服务
   java -jar pay/target/pay.jar
   
   # 启动商城服务
   java -jar mall/target/mall.jar
   ```

### 开发环境启动

```bash
# 启动 mall 模块
cd mall
mvn spring-boot:run

# 启动 pay 模块
cd pay
mvn spring-boot:run
```

## 📚 API 文档

### 用户相关 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/user/register` | 用户注册 |
| POST | `/user/login` | 用户登录 |
| GET | `/user` | 获取用户信息 |
| POST | `/user/logout` | 退出登录 |

### 商品相关 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/products` | 商品列表（支持分页和分类筛选） |
| GET | `/products/{productId}` | 商品详情 |

### 购物车相关 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/carts` | 添加商品到购物车 |
| PUT | `/carts/{productId}` | 更新购物车商品数量 |
| DELETE | `/carts/{productId}` | 删除购物车商品 |
| PUT | `/carts/selectAll` | 全选/取消全选 |
| GET | `/carts` | 获取购物车列表 |

### 订单相关 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/orders` | 创建订单 |
| GET | `/orders` | 订单列表 |
| GET | `/orders/{orderNo}` | 订单详情 |
| PUT | `/orders/{orderNo}/cancel` | 取消订单 |

### 收货地址相关 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/shippings` | 添加收货地址 |
| DELETE | `/shippings/{shippingId}` | 删除收货地址 |
| PUT | `/shippings/{shippingId}` | 更新收货地址 |
| GET | `/shippings` | 获取收货地址列表 |

### 支付相关 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/pay/create` | 创建支付订单 |
| POST | `/pay/notify` | 支付异步通知 |
| GET | `/pay/queryByOrderId` | 查询支付记录 |
| GET | `/pay/payReturn` | 支付成功返回页面 |

## 🗄️ 数据库设计

### 核心数据表

- `mall_user` - 用户表
- `mall_product` - 商品表
- `mall_category` - 分类表
- `mall_order` - 订单表
- `mall_order_item` - 订单明细表
- `mall_shipping` - 收货地址表
- `pay_info` - 支付信息表

详细的数据库设计请参考 [mallpay-project-overview.md](mallpay-project-overview.md)

## 🔧 配置说明

### 应用配置

- **Mall 模块端口**: 9090
- **环境配置**: 支持 test、prod 多环境
- **数据库**: MySQL，支持连接池配置
- **缓存**: Redis，用于缓存和Session存储
- **消息队列**: RabbitMQ，用于异步消息处理

### 关键配置文件

- `application.yml` - 主配置文件
- `application-test.yml` - 测试环境配置
- `application-prod.yml` - 生产环境配置

## 🔄 业务流程

### 购物流程
1. 用户浏览商品列表
2. 查看商品详情
3. 添加商品到购物车
4. 在购物车中调整商品数量
5. 选择商品进行结算
6. 填写收货地址
7. 创建订单
8. 选择支付方式
9. 完成支付

### 支付流程
1. 用户选择支付方式（微信/支付宝）
2. 系统创建支付订单
3. 调用支付平台API
4. 用户完成支付
5. 支付平台异步通知
6. 系统更新订单状态
7. 通过MQ通知商城系统

## 🚀 部署指南

### Docker 部署（推荐）

```bash
# 构建镜像
docker build -t mallpay-mall ./mall
docker build -t mallpay-pay ./pay

# 运行容器
docker run -d -p 9090:9090 mallpay-mall
docker run -d -p 8080:8080 mallpay-pay
```

### 传统部署

1. 确保所有依赖服务已启动（MySQL、Redis、RabbitMQ）
2. 按照启动顺序启动服务：Pay 模块 → Mall 模块
3. 访问 `http://localhost:9090` 测试服务

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn integration-test
```

## 📈 性能优化

- 数据库连接池优化
- Redis 缓存策略
- 分页查询优化
- 异步消息处理
- 接口响应时间监控

## 🔮 扩展功能

### 计划中的功能
- 商品评价系统
- 优惠券系统
- 积分系统
- 物流跟踪
- 商家管理后台
- 数据统计分析

### 技术优化计划
- 分布式锁
- 数据库读写分离
- 接口限流和熔断
- 分布式事务处理

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📝 开发规范

- 代码风格遵循阿里巴巴Java开发手册
- 使用 Lombok 简化代码
- 统一使用 ResponseVo 作为接口返回格式
- 异常统一处理
- RESTful API 设计

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 作者

- **开发团队** - *初始工作* - [MallPay](https://github.com/your-username/mallpay)

## 🙏 致谢

- Spring Boot 社区
- MyBatis 社区
- 所有贡献者

## 📞 联系我们

如果您有任何问题或建议，请通过以下方式联系我们：

- 提交 Issue
- 发送邮件至：xhhayy@qq.com
- 项目主页：https://github.com/your-username/mallpay

---

⭐ 如果这个项目对您有帮助，请给我们一个星标！
