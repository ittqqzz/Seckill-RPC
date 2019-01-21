# SecKill-SOA

## 简介

简易在线秒杀 web 应用程序——升级版

用户可以在秒杀开启后进行秒杀活动；秒杀结束，阻止用户秒杀；用户成功秒杀到商品后阻止其二次秒杀；

演示：

![kCOT2j.gif](https://s2.ax1x.com/2019/01/20/kCOT2j.gif)

## 特性

项目分为【前端】和【后端】

前端处理与用户打交道的工程，后端专门处理数据交互

由于是对之前的 [Seckill-Monolithic-Architecure](https://github.com/ittqqzz/Seckill-Monolithic-Architecure) 项目进行升级，所以本项目由三个模块组成：

1. seckill 也就是之前的版本，虽然进行了大量修改，但还是一个经典的 SSM 结构
2. seckillback 不关心与用户的交互，专心处理数据的模块
3. seckillapi 为seckill 以及 seckillback 提供公共类或接口

当上游数据顺利到达下游后，seckill 会将消息发送到 MQ，seckillback 处理队列消息，进行创建订单与扣减库存，所以需要使用到 RabbitMQ 提供消息中间件的服务，以及 Dubbo 提供 RPC 调用服务

运行流程图：

![kiZKFe.png](https://s2.ax1x.com/2019/01/21/kiZKFe.png)

项目整体结构图：

![kiZMJH.png](https://s2.ax1x.com/2019/01/21/kiZMJH.png)

为确保消息 100% 落地，额外添加 brokerDB ，通过定时检查该库 status 字段为非 1 的数据行并进行补偿操作，以避免后端消费 MQ 失败。

## 使用的技术

数据库：Redis、MySQL

Web框架：Spring MVC

持久层框架：Mybatis

依赖管理：Maven

版本控制：Git

容器：Spring

分布式服务框架：Dubbo

消息中间件：RabbitMQ

工具：lombok、Protostuff

## 安装

用 IDEA open seckill 工程，然后按下 ctrl + alt + shift + s 打开 Project Structure ，点击 Modules，点击 “+” 号，再点击 import Module，将剩下的两个模块导入

在 sql 包下，执行 seckill.sql 与 seckillback.sql，建立数据库，然后在 seckill 模块下的 jdbc.properties 文件里面修改前端要用到的数据库连接参数，以及 seckillback 模块下的 application.properties 文件里面修改后端用到的数据库

启动 Redis ，默认 host： 127.0.0.1，port ：6379；如需修改请在 spring-dao.xml 文件里面修改

启动 RabbitMQ，使用默认参数

启动 Zookeeper，使用默认参数

项目启动过程：

1. 直接启动 seckillback 模块里面的 SeckillmqreceiveApplication
2. 再为 seckill 模块添加 Tomcat，部署项目并启动
3. 最后访问连接：http://localhost:8080/seckill/list



