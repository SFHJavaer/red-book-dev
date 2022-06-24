#### SpringBoot+Uniapp2022全新仿抖音短视频



#### APP模块：

​		1、用户模块
​		2、视频模块
​		3、粉丝模块
​		4、留言评论模块
​		5、消息模块

#### 项目架构图：

![qRTCJ.png](https://s1.328888.xyz/2022/05/14/qRTCJ.png)



#### 开发流程：

###### 1）根据前端界面构建后端项目：
数据库的选型，进行服务端通信测试

工程构建，使用逆向工程生成实体类和Mapper

使用knife4j实现接口文档 

###### 2）开发用户业务模块
使用腾讯云短信完成登录模块的一键登录功能（未注册会直接进行注册）
使用Redis进行验证码的存储，Hibernate参数校验扩展
MinIO实现用户修改头像与背景图

###### 3）开发短视频业务模块 
使用UniCloud云端存储视频
实现短视频的刷新、查询、分页、点赞、评论，转公开或私密

###### 4）开发粉丝业务模块
实现关注、粉丝、互粉等业务的实现
###### 5）开发评论业务模块
实现评论的发表、查询、删除、点赞等业务
###### 6）开发消息业务模块（MongoDB）
实现点赞评论和关注等消息的发送和入库

MongoDB分页查询系统消息列表

###### 7）RabbitMQ 异步解耦

编写交换机队列

使用生产者消费者来实现异步解耦

###### 8）使用Nacos进行一定数量的定量入库（动态配置中心）

Nacos作为分布式配置中心，部署Nacos完成阈值入库业务

###### 9）发布项目 部署云端

#### 效果图：

<img src="https://s2.loli.net/2022/06/24/OU3FaQBlfd8Hurm.jpg" style="zoom:15%;" /><img src="https://s2.loli.net/2022/06/24/4BsWExympNbkMor.jpg" style="zoom:25%;" /><img src="https://s2.loli.net/2022/06/24/ZQtjMXzmqkIHcs1.jpg" style="zoom:25%;" /><img src="https://s2.loli.net/2022/06/24/WwgJB2hkLiPIqZS.jpg" style="zoom:25%;" />

