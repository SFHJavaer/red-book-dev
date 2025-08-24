# 红书短视频平台 (Red Book Dev)

一个基于Spring Boot + Spring Cloud的全栈仿抖音短视频社交平台项目，支持视频上传、点赞、评论、关注、私信等核心功能。

## 🚀 项目特色

- **现代化架构**: 采用Spring Cloud微服务架构，支持分布式部署
- **高性能设计**: Redis缓存、RabbitMQ异步处理、MongoDB消息存储
- **丰富功能**: 短视频、社交互动、用户管理、实时消息等
- **技术前沿**: 集成Nacos配置中心、MinIO对象存储、Knife4j API文档
- **生产就绪**: 完整的开发、测试、生产环境配置

## 📱 功能模块

### 核心功能
- ✅ 用户注册登录 (支持短信验证码)
- ✅ 短视频上传、播放、点赞、评论
- ✅ 用户关注、粉丝管理
- ✅ 私信聊天系统
- ✅ 视频搜索和推荐

### 技术特性
- ✅ 分布式配置管理 (Nacos)
- ✅ 服务注册与发现 (Nacos)
- ✅ 消息队列异步处理 (RabbitMQ)
- ✅ 缓存优化 (Redis)
- ✅ 对象存储 (MinIO)
- ✅ 文档自动生成 (Knife4j)

## 🛠 技术架构

### 后端技术栈
- **框架**: Spring Boot 2.5.4
- **微服务**: Spring Cloud 2020.0.4 + Spring Cloud Alibaba 2.2.6
- **数据库**: MySQL 8.0 + MongoDB
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **对象存储**: MinIO
- **ORM**: MyBatis + 通用Mapper + PageHelper

### 项目结构
```
red-book-dev/
├── book-api/           # API接口层 (8080)
├── book-common/        # 公共模块
├── book-model/         # 数据模型
├── book-mapper/        # 数据访问层
├── book-service/       # 业务逻辑层
└── mybatis-generator/  # 代码生成器
```

### 架构图
![项目架构图](https://s1.328888.xyz/2022/05/14/qRTCJ.png)

## 🔧 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis
- MongoDB
- RabbitMQ
- Nacos 2.0+
- MinIO

### 本地开发

1. **克隆项目**
```bash
git clone https://github.com/your-repo/red-book-dev.git
cd red-book-dev
```

2. **配置数据库**
```sql
-- 创建数据库
CREATE DATABASE red_book_dev DEFAULT CHARACTER SET utf8mb4;

-- 导入表结构 (使用mybatis-generator生成)
```

3. **配置Nacos**
- 下载并启动Nacos 2.0+
- 在Nacos控制台添加配置: `imooc-red-book-dev-dev.yml`

4. **修改配置**
```yaml
# book-api/src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/red_book_dev?useSSL=false
    username: your_username
    password: your_password

nacos:
  server-addr: localhost:8848

redis:
  host: localhost
  port: 6379

mongodb:
  uri: mongodb://localhost:27017/red_book_dev

rabbitmq:
  host: localhost
  port: 5672

minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
```

5. **启动项目**
```bash
# 启动各个模块
cd book-common && mvn clean install
cd ../book-model && mvn clean install
cd ../book-mapper && mvn clean install
cd ../book-service && mvn clean install
cd ../book-api && mvn spring-boot:run
```

6. **访问接口**
- API文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

## 📚 API文档

项目使用Knife4j生成API文档，启动后访问: http://localhost:8080/doc.html

主要接口模块：
- `/vlog` - 短视频相关接口
- `/userInfo` - 用户信息接口
- `/fans` - 粉丝管理接口
- `/comment` - 评论接口
- `/msg` - 消息接口

## 🚀 部署说明

### Docker部署
```dockerfile
# 构建镜像
docker build -t red-book-api .

# 运行容器
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  red-book-api
```

### 生产环境配置
1. 使用Nacos配置中心管理配置
2. 配置MySQL主从复制
3. 设置Redis集群
4. 配置MinIO分布式存储
5. 使用Nginx反向代理和负载均衡

## 📊 开发流程

### 1. 环境搭建
- 数据库设计和逆向工程
- 微服务架构搭建
- API文档工具集成

### 2. 用户模块开发
- 短信验证码登录
- Redis缓存优化
- MinIO文件上传

### 3. 视频模块开发
- 视频上传和存储
- 点赞评论功能
- 视频搜索和推荐

### 4. 社交功能开发
- 关注粉丝系统
- 消息推送机制
- 实时通信

### 5. 性能优化
- RabbitMQ异步处理
- Redis缓存策略
- 数据库读写分离

### 6. 运维部署
- 容器化部署
- 监控告警
- CI/CD流程

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -m 'Add new feature'`
4. 推送分支: `git push origin feature/new-feature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- 项目主页: https://github.com/your-repo/red-book-dev
- 问题反馈: 请提交 Issue
- 技术交流: 欢迎提交 PR

---

**注意**: 项目部署在学校服务器上，需要登录VPN才能正常获取APP流量
