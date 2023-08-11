# TcpReverseProxy

> 此应用是针对TCP消息做的反向代理

----------------------------------------------------------------
## 功能说明

- 动态创建TCP消息代理服务器向被代理发送消息
- 与代理服务器建立连接的远程客户端保持长链接
- 自动重连被代理服务器
- 当被代理服务器不可用时，存储远程客户端发来的TCP消息，当被代理服务器能够建立连接之后补发消息
- 远程客户端无感目标服务器的重启、切换
## 应用涉及组件说明
    
- 数据库（MySQL）
  - 动态存储启动服务地址及转发目的服务地址
- Netty
  - 基于Netty创建TCP服务端和客户端
- Redis
  - 存储一些因为管道或网络原因导致没发出去的消息

## 使用说明：

通过数据库配置达到动态生成TCP代理

涉及表：`proxy_config`、`tcp_proxy_mapping`,建表语句:`src\main\resources\ddl\gateway.sql`

- 生效规则：启动该服务的IP在`tcp_proxy_mapping`有配置，且指定环境与`proxy_config`配置的环境一致

- 配置表中插入两条数据，一条是默认启动环境、一条是运行时环境

```mysql
INSERT INTO `proxy_config` VALUES (1, 'DEFAULT_ENV', 'sit', '默认环境');
INSERT INTO `proxy_config` VALUES (2, 'RUNTIME_ENV', 'sit', '运行时环境');
```

- 对`tcp_proxy_mapping`插入数据，示例：
```mysql
#在IP为10.6.xx.xx的服务器上开启端口为20112的netty服务，对所有发送到这个服务的TCP消息，指定使用8998端口向IP为25.xx.xx.40、端口为20115的服务发送消息
INSERT INTO `tcp_proxy_mapping` VALUES (null, '10.6.xx.xx', '20112', '8998','25.xx.xx.40', '20115', 'sit')
#在IP为10.6.xx.xx的服务器上开启端口为20113的netty服务，对所有发送到这个服务的TCP消息，随机采用端口向IP为25.xx.xx.40、端口为20115的服务发送消息
INSERT INTO `tcp_proxy_mapping` VALUES (null, '10.6.xx.xx', '20113', '','25.xx.xx.40', '20115', 'sit');

```
- `tcp_proxy_mapping`表中字段`local_client_port`**额外说明**:
  - `local_client_port` = `具体端口` ：意味着指定使用此端口向目标服务器发送消息、当目标服务器断开此连接之后会自动重连
  - `local_client_port` = `-` ：意味着使用随机端口向目标服务器发送消息，但是目标断开此连接后不会自动重连
  - `local_client_port` = `''`|`null`:意味着使用随机端口向目标服务器发送消息，并会自动重连


 


