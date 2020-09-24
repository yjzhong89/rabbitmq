# RabbitMQ

## MQ的优势和劣势

### 优势

应用解耦：提高系统容错性和可维护性；

异步提速：提升用户体验和系统吞吐量；

削峰填谷：提高系统稳定性。

### 劣势

系统可用性降低：系统引入的外部依赖越多，系统稳定性越差，一旦MQ宕机，就会对业务造成影响。

系统复杂性提高：MQ的加入大大增加了系统的复杂度，以前系统间是同步的远程调用，现在是通过MQ进行异步调用，需要保证消息不被重复消费，处理消息丢失情况以及保证消息传递的顺序性。

### 使用条件

1. 生产者不需要从消费者处获得反馈。引入消息队列之前的直接调用，其接口的返回值应该为空，这才让下层动作还没做，上层却当成动作做完了继续往后走，即所谓异步成为了可能；
2. 允许短暂的不一致性；
3. 确实有效果。即解耦、提速、削峰这些方面的收益，超过加入MQ、管理MQ的成本。

### 消息队列对比

![消息队列对比](.\images\消息队列对比.png)

## RabbitMQ简介

### AMQP

AMQP，即高级消息队列协议，是一个网络协议，是应用层协议的一个开放标准，为面向消息的中间件设计。基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件不同产品，不同的开发语言等条件的限制。

### 基础架构

![RabbitMQ基础架构1](.\images\RabbitMQ基础架构1.png)

### 相关角色

- Broker：接收和分发消息的应用。RabbitMQ Server就是Message Broker；

- Virtual host：出于多租户和安全因素设计的，把AMQP的基本组件划分到一个虚拟的分组中，类似于网络中的namespace概念。当多个不同的用户使用同一个RabbitMQ server提供的服务时，可以划分出多个vhost，每个用户在自己的vhost创建exchange/queue等；

- Connection：publisher/consumer和broker之间的TCP连接；

- Channel：如果每一次访问RabbitMQ都建立一个Connection，在消息量大的时候建立TCP Connection的开销是巨大的，效率也较低。Channel是在connection内部建立的逻辑连接，如果应用程序支持多线程，通过每个thread创建单独的channel进行通讯，AMQP method包含了channel id帮助客户端和message broker识别channel，所以channel之间是完全隔离的。Channel作为轻量级的Connection极大减少了操作系统建立TCP connection的开销；
- Exchange：message到达broker的第一站，根据分发规则，匹配查询表中的routing key，分发消息到queue中去。常用的类型有：direct(point-to-point)，topic(publish-subscribe)以及fanout(multicast)；
- Queue：消息最终被送到这里等待consumer取走；
- Binding：exchange和queue之间的虚拟连接，binding中可以包含routing key。Binding信息被保存到exchange中的查询表中，用于message的分发依据。

### 工作模式

RabbitMQ提供了6中工作模式：简单模式、work queues、Publish/Subscribe发布与订阅模式、Routing路由模式、Topics主题模式、RPC远程调用模式。

### JMS

JMS即Java消息服务应用程序接口，是一个Java平台中关于面向消息中间件的api。

AMQP是一种协议，类比HTTP；而JMS是API规范接口，类比JDBC。

## RabbitMQ安装

### 安装依赖环境

在线安装依赖环境：

```shell
yum install build-essential openssl openssl-devel unixODBC unixODBC-devel make gcc gcc-c++ kernel-devel m4 ncurses-devel tk tc xz
```

### 安装Erlang

采用官网的方式安装

```sh
# In /etc/yum.repos.d/rabbitmq_erlang.repo
[rabbitmq_erlang]
name=rabbitmq_erlang
baseurl=https://packagecloud.io/rabbitmq/erlang/el/7/$basearch
repo_gpgcheck=1
gpgcheck=1
enabled=1
# PackageCloud's repository key and RabbitMQ package signing key
gpgkey=https://packagecloud.io/rabbitmq/erlang/gpgkey
       https://dl.bintray.com/rabbitmq/Keys/rabbitmq-release-signing-key.asc
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
metadata_expire=300

[rabbitmq_erlang-source]
name=rabbitmq_erlang-source
baseurl=https://packagecloud.io/rabbitmq/erlang/el/7/SRPMS
repo_gpgcheck=1
gpgcheck=0
enabled=1
# PackageCloud's repository key and RabbitMQ package signing key
gpgkey=https://packagecloud.io/rabbitmq/erlang/gpgkey
       https://dl.bintray.com/rabbitmq/Keys/rabbitmq-release-signing-key.asc
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
metadata_expire=300

# 然后执行以下命令
yum clean all
yum makecache
yum list | grep erlang
yum install erlang.x86_64
```

### 安装RabbitMQ

上传rabbitmq-server-3.8.8-1.el7.noarch.rpm。

```sh
# 安装
yum install -y socat

# 安装
rpm -ivh rabbitmq-server-3.8.8-1.el7.noarch.rpm
```

### 开启管理界面及配置

```sh
# 开启管理界面
rabbitmq-plugins enable rabbitmq_management
# 修改默认配置信息(在3.8.8中没有默认配置文件)
vim /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.5/ebin/rabbit.app 
# 比如修改密码、配置等等，例如：loopback_users 中的 <<"guest">>,只保留guest
```

### 启动服务

```sh
service rabbitmq-server start # 启动服务
service rabbitmq-server stop # 停止服务
service rabbitmq-server restart # 重启服务
```

设置配置文件

```sh
vim /etc/rabbitmq/rabbitmq.config

# 添加以下内容
[{rabbit, [{loopback_users, []}]}].
```

## RabbitMQ的工作模式

### 简单模式

![工作模式1](.\images\工作模式1.png)

- P：生产者，也就是要发送消息的程序
- C：消费者：消息的接收者，会一直等待消息到来
- queue：消息队列，图中红色部分。类似一个邮箱，可以缓存消息；生产者向其中投递消息，消费者从其中取出消息。

### 工作队列模式

![工作模式2](.\images\工作模式2.png)

- Work Queues：与入门程序的简单模式相比，多了一个或一些消费端，多个消费端共同消费同一个队列中的消息；
- 在一个队列中如果有多个消费者，那么消费者之间对于同一个消息的关系是竞争的关系；

- 应用场景：对于任务过重或任务较多情况使用工作队列可以提高任务处理的速度。

### 发布订阅模式

![工作模式3](.\images\工作模式3.png)

在订阅模型中，多了一个 Exchange 角色，而且过程略有变化：

- P：生产者，也就是要发送消息的程序，但是不再发送到队列中，而是发给X（交换机）；

- C：消费者，消息的接收者，会一直等待消息到来；

- Queue：消息队列，接收消息、缓存消息；

- Exchange：交换机（X）。一方面，接收生产者发送的消息。另一方面，知道如何处理消息，例如递交给某个特别队列、递交给所有队列、或是将消息丢弃。到底如何操作，取决于Exchange的类型。Exchange有常见以下3种类型：

  1. Fanout：广播，将消息交给所有绑定到交换机的队列；

  2. Direct：定向，把消息交给符合指定routing key 的队列；

  3. Topic：通配符，把消息交给符合routing pattern（路由模式） 的队列。

**Exchange**（交换机）只负责转发消息，不具备存储消息的能力，因此如果没有任何队列与 Exchange 绑定，或者没有符合路由规则的队列，那么消息会丢失！

交换机需要与队列进行绑定，绑定之后；一个消息可以被多个消费者都收到。

发布订阅模式与工作队列模式的区别：

- 工作队列模式不用定义交换机，而发布/订阅模式需要定义交换机；

- 发布/订阅模式的生产方是面向交换机发送消息，工作队列模式的生产方是面向队列发送消息(底层使用默认交换机)；

- 发布/订阅模式需要设置队列和交换机的绑定，工作队列模式不需要设置，实际上工作队列模式会将队列绑 定到默认的交换机。 

### 路由模式

![工作模式4](.\images\工作模式4.png)

其中：

- P：生产者，向 Exchange 发送消息，发送消息时，会指定一个routing key；

- X：Exchange（交换机），接收生产者的消息，然后把消息递交给与 routing key 完全匹配的队列；

- C：消费者，其所在队列指定了需要 routing key 为 error 的消息；

- C：消费者，其所在队列指定了需要 routing key 为 info、error的消息。

模式说明

- 队列与交换机的绑定，不能是任意绑定了，而是要指定一个 RoutingKey(路由key)；

- 消息的发送方在向 Exchange 发送消息时，也必须指定消息的 RoutingKey；

- Exchange 不再把消息交给每一个绑定的队列，而是根据消息的 Routing Key 进行判断，只有队列的Routingkey 与消息的 Routing key 完全一致，才会接收到消息。

### 通配符模式

![工作模式5](.\images\工作模式5.png)



模式说明

- Topic 类型与 Direct 相比，都是可以根据 RoutingKey 把消息路由到不同的队列。只不过 Topic 类型Exchange 可以让队列在绑定 Routing key 的时候使用**通配符**！

- Routingkey 一般都是有一个或多个单词组成，多个单词之间以”.”分割，例如： item.insert；

- 通配符规则：# 匹配一个或多个词，* 匹配不多不少恰好1个词，例如：item.# 能够匹配 item.insert.abc 或者 item.insert，item.* 只能匹配 item.insert。

Topic 主题模式可以实现 Pub/Sub 发布与订阅模式和 Routing 路由模式的功能，只是 Topic 在配置routing key 的时候可以使用通配符，显得更加灵活。

### 小结

1. 简单模式：一个生产者、一个消费者，不需要设置交换机（使用默认的交换机）；
2. 工作队列模式Work Queue：一个生产者、多个消费者(竞争关系)，不需要设置交换机(使用默认的交换机)：
3. 发布订阅模式Publish/subscribe：需要设置类型为 fanout 的交换机，并且交换机和队列进行绑定，当发送消息到交换机后，交换机会将消息发送到绑定的队列；
4. 路由模式Routing：需要设置类型为 direct 的交换机，交换机和队列进行绑定，并且指定 routing key，当发送消息到交换机后，交换机会根据 routing key 将消息发送到对应的队列；
5. 通配符模式Topic：需要设置类型为 topic 的交换机，交换机和队列进行绑定，并且指定通配符方式的 routing key，当发送消息到交换机后，交换机会根据 routing key 将消息发送到对应的队列。

## RabbitMQ高级特性

### 消息的可靠性投递

在使用RabbitMQ的时候，作为消息发送方希望杜绝任何消息丢失或者投递失败场景。RabbitMQ为用户提供了两种方式用来控制消息的投递可靠性模式。

- confirm 确认模式

- return 退回模式

RabbitMQ整个消息投递的路径为：

producer ---> rabbitmq broker ---> exchange ---> queue ---> consumer

- 消息从producer ---> exchange则会返回一个confirmCallback；

- 消息从exchange ---> queue投递失败则会返回一个returnCallback。

可以利用这两个callback来控制消息的可靠性投递。

配置方式

- 设置ConnectionFactory的publisher-confirms="true" 开启确认模式；

- 使用rabbitTemplate.setConfirmCallback设置回调函数。当消息发送到exchange后回调confirm方法。在方法中判断ack，如果为true，则发送成功，如果为false，则发送失败，需要处理；
- 设置ConnectionFactory的publisher-returns="true" 开启 退回模式；
- 使用rabbitTemplate.setReturnCallback设置退回函数，当消息从exchange路由到queue失败后，如果设置了rabbitTemplate.setMandatory(true)参数，则会将消息退回给producer。并执行回调函数returnedMessage。

在RabbitMQ中也提供了事务机制，但是性能较差。可以使用channel下列方法，完成事务控制：

1. txSelect(), 用于将当前channel设置成transaction模式；

2. txCommit()，用于提交事务；

3. txRollback(),用于回滚事务。

### 客户端确认

ack指Acknowledge，表示消费端收到消息后的确认方式。

消费端一共有三种确认方式：

- 自动确认：acknowledge="none"；

- 手动确认：acknowledge="manual"；

- 根据异常情况确认：acknowledge="auto"。

其中自动确认是指，当消息一旦被Consumer接收到，则自动确认收到，并将相应 message 从 RabbitMQ 的消息缓存中移除。但是在实际业务处理中，很可能消息接收到，业务处理出现异常，那么该消息就会丢失。如果设置了手动确认方式，则需要在业务处理成功后，调用channel.basicAck()，手动签收，如果出现异常，则调用channel.basicNack()方法，让其自动重新发送消息。

### 消费端限流

在rabbit:listener-container中配置 prefetch属性设置消费端一次拉取多少消息。

消费端的确认模式一定为手动确认：acknowledge="manual"。

### TTL

TTL全称Time To Live(存活时间/过期时间)，当消息到达存活时间后，还没有被消费，会被自动清除。RabbitMQ可以对消息设置过期时间，也可以对整个队列(Queue)设置过期时间。

- 设置队列过期时间使用参数：x-message-ttl，单位：ms(毫秒)，会对整个队列消息统一过期；

- 设置消息过期时间使用参数：expiration。单位：ms(毫秒)，当该消息在队列**头部**时（消费时），会单独判断这一消息是否过期；

- 如果两者都进行了设置，以时间短的为准。

### 死信队列

死信队列，英文缩写：DLX，Dead Letter Exchange(死信交换机)，当消息成为Dead message后，可以被重新发送到另一个交换机，这个交换机就是DLX。

队列绑定死信交换机：给队列设置参数x-dead-letter-exchange 和 x-dead-letter-routing-key。

![死信队列](.\images\死信队列.png)

消息成为死信的三种情况：

1. 队列消息长度到达限制；
2. 消费者拒接消费消息，basicNack/basicReject，并且不把消息重新放入原目标队列，requeue=false；
3. 原队列存在消息过期设置，消息到达超时时间未被消费。

小结

1. 死信交换机和死信队列和普通的没有区别；
2. 当消息成为死信后，如果该队列绑定了死信交换机，则消息会被死信交换机重新路由到死信队；
3. 消息成为死信的三种情况。

### 延迟队列

延迟队列，即消息进入队列后不会立即被消费，只有到达指定时间后，才会被消费。

需求：

1. 下单后，30分钟未支付，取消订单，回滚库存；
2. 新用户注册成功7天后，发送短信问候。

实现方式：

1. 定时器；
2. 延迟队列。

在RabbitMQ中并未提供延迟队列功能，但是可以使用TTL+死信队列组合实现延迟队列的效果。

### 日志与监控

RabbitMQ默认日志存放路径： /var/log/rabbitmq/rabbit@xxx.log

日志包含了RabbitMQ的版本号、Erlang的版本号、RabbitMQ服务节点名称、cookie的hash值、RabbitMQ配置文件地址、内存限制、磁盘限制、默认账户guest的创建以及权限配置等等。

监控命令

```sh
查看队列
# rabbitmqctl list_queues

查看exchanges
# rabbitmqctl list_exchanges

查看用户
# rabbitmqctl list_users

查看连接
# rabbitmqctl list_connections

查看消费者信息
# rabbitmqctl list_consumers

查看环境变量
# rabbitmqctl environment

查看未被确认的队列
# rabbitmqctl list_queues  name messages_unacknowledged

查看单个队列的内存使用
# rabbitmqctl list_queues name memory

查看准备就绪的队列
# rabbitmqctl list_queues name messages_ready
```

### 消息追踪

在使用任何消息中间件的过程中，难免会出现某条消息异常丢失的情况。对于RabbitMQ而言，可能是因为生产者或消费者与RabbitMQ断开了连接，而它们与RabbitMQ又采用了不同的确认机制；也有可能是因为交换器与队列之间不同的转发策略；甚至是交换器并没有与任何队列进行绑定，生产者又不感知或者没有采取相应的措施；另外RabbitMQ本身的集群策略也可能导致消息的丢失。这个时候就需要有一个较好的机制跟踪记录消息的投递过程，以此协助开发和运维人员进行问题的定位。

在RabbitMQ中可以使用Firehose和rabbitmq_tracing插件功能来实现消息追踪。

#### Firehose

firehose的机制是将生产者投递给rabbitmq的消息，rabbitmq投递给消费者的消息按照指定的格式发送到默认的exchange上。这个默认的exchange的名称为amq.rabbitmq.trace，它是一个topic类型的exchange。发送到这个exchange上的消息的routing key为 publish.exchangename 和 deliver.queuename。其中exchangename和queuename为实际exchange和queue的名称，分别对应生产者投递到exchange的消息，和消费者从queue上获取的消息。

注意：打开 trace 会影响消息写入功能，适当打开后请关闭。

```sh
# 开启Firehose命令
rabbitmqctl trace_on

# 关闭Firehose命令
rabbitmqctl trace_off
```

#### RabbitMQ_Tracing

rabbitmq_tracing和Firehose在实现上如出一辙，只不过rabbitmq_tracing的方式比Firehose多了一层GUI的包装，更容易使用和管理。

```sh
# 启用插件
rabbitmq-plugins enable rabbitmq_tracing
```

## RabbitMQ应用问题

### 消息可靠性保障-消息补偿

![消息补偿](.\images\消息补偿.png)

### 消息幂等性保障-乐观锁机制

幂等性指一次和多次请求某一个资源，对于资源本身应该具有同样的结果。也就是说，其任意多次执行对资源本身所产生的影响均与一次执行的影响相同。

在MQ中指，消费多条相同的消息，得到与消费该消息一次相同的结果。

![幂等性保障](.\images\幂等性保障.png)

通过version来保证队列中相同的数据只会被消费一次。

## RabbitMQ集群搭建

摘要：实际生产应用中都会采用消息队列的集群方案，如果选择RabbitMQ那么有必要了解下它的集群方案原理

一般来说，如果只是为了学习RabbitMQ或者验证业务工程的正确性那么在本地环境或者测试环境上使用其单实例部署就可以了，但是出于MQ中间件本身的可靠性、并发性、吞吐量和消息堆积能力等问题的考虑，在生产环境上一般都会考虑使用RabbitMQ的集群方案。

### 集群方案的原理

RabbitMQ这款消息队列中间件产品本身是基于Erlang编写，Erlang语言天生具备分布式特性（通过同步Erlang集群各节点的magic cookie来实现）。因此，RabbitMQ天然支持Clustering。这使得RabbitMQ本身不需要像ActiveMQ、Kafka那样通过ZooKeeper分别来实现HA方案和保存集群的元数据。集群是保证可靠性的一种方式，同时可以通过水平扩展以达到增加消息吞吐量能力的目的。


### 单机多实例部署

由于某些因素的限制，有时候你不得不在一台机器上去搭建一个rabbitmq集群，这个有点类似zookeeper的单机版。真实生成环境还是要配成多机集群的。有关怎么配置多机集群的可以参考其他的资料，这里主要论述如何在单机中配置多个rabbitmq实例。

主要参考官方文档：https://www.rabbitmq.com/clustering.html

首先确保RabbitMQ运行没有问题

```shell
[root@super ~]# rabbitmqctl status
Status of node rabbit@super ...
[{pid,10232},
 {running_applications,
     [{rabbitmq_management,"RabbitMQ Management Console","3.6.5"},
      {rabbitmq_web_dispatch,"RabbitMQ Web Dispatcher","3.6.5"},
      {webmachine,"webmachine","1.10.3"},
      {mochiweb,"MochiMedia Web Server","2.13.1"},
      {rabbitmq_management_agent,"RabbitMQ Management Agent","3.6.5"},
      {rabbit,"RabbitMQ","3.6.5"},
      {os_mon,"CPO  CXC 138 46","2.4"},
      {syntax_tools,"Syntax tools","1.7"},
      {inets,"INETS  CXC 138 49","6.2"},
      {amqp_client,"RabbitMQ AMQP Client","3.6.5"},
      {rabbit_common,[],"3.6.5"},
      {ssl,"Erlang/OTP SSL application","7.3"},
      {public_key,"Public key infrastructure","1.1.1"},
      {asn1,"The Erlang ASN1 compiler version 4.0.2","4.0.2"},
      {ranch,"Socket acceptor pool for TCP protocols.","1.2.1"},
      {mnesia,"MNESIA  CXC 138 12","4.13.3"},
      {compiler,"ERTS  CXC 138 10","6.0.3"},
      {crypto,"CRYPTO","3.6.3"},
      {xmerl,"XML parser","1.3.10"},
      {sasl,"SASL  CXC 138 11","2.7"},
      {stdlib,"ERTS  CXC 138 10","2.8"},
      {kernel,"ERTS  CXC 138 10","4.2"}]},
 {os,{unix,linux}},
 {erlang_version,
     "Erlang/OTP 18 [erts-7.3] [source] [64-bit] [async-threads:64] [hipe] [kernel-poll:true]\n"},
 {memory,
     [{total,56066752},
      {connection_readers,0},
      {connection_writers,0},
      {connection_channels,0},
      {connection_other,2680},
      {queue_procs,268248},
      {queue_slave_procs,0},
      {plugins,1131936},
      {other_proc,18144280},
      {mnesia,125304},
      {mgmt_db,921312},
      {msg_index,69440},
      {other_ets,1413664},
      {binary,755736},
      {code,27824046},
      {atom,1000601},
      {other_system,4409505}]},
 {alarms,[]},
 {listeners,[{clustering,25672,"::"},{amqp,5672,"::"}]},
 {vm_memory_high_watermark,0.4},
 {vm_memory_limit,411294105},
 {disk_free_limit,50000000},
 {disk_free,13270233088},
 {file_descriptors,
     [{total_limit,924},{total_used,6},{sockets_limit,829},{sockets_used,0}]},
 {processes,[{limit,1048576},{used,262}]},
 {run_queue,0},
 {uptime,43651},
 {kernel,{net_ticktime,60}}]
```

停止rabbitmq服务

```shell
[root@super sbin]# service rabbitmq-server stop
Stopping rabbitmq-server: rabbitmq-server.

```

启动第一个节点：

```shell
[root@super sbin]# RABBITMQ_NODE_PORT=5673 RABBITMQ_NODENAME=rabbit1 rabbitmq-server start

              RabbitMQ 3.6.5. Copyright (C) 2007-2016 Pivotal Software, Inc.
  ##  ##      Licensed under the MPL.  See http://www.rabbitmq.com/
  ##  ##
  ##########  Logs: /var/log/rabbitmq/rabbit1.log
  ######  ##        /var/log/rabbitmq/rabbit1-sasl.log
  ##########
              Starting broker...
 completed with 6 plugins.
```

启动第二个节点：

> web管理插件端口占用,所以还要指定其web插件占用的端口号。

```shell
[root@super ~]# RABBITMQ_NODE_PORT=5674 RABBITMQ_SERVER_START_ARGS="-rabbitmq_management listener [{port,15674}]" RABBITMQ_NODENAME=rabbit2 rabbitmq-server start

              RabbitMQ 3.6.5. Copyright (C) 2007-2016 Pivotal Software, Inc.
  ##  ##      Licensed under the MPL.  See http://www.rabbitmq.com/
  ##  ##
  ##########  Logs: /var/log/rabbitmq/rabbit2.log
  ######  ##        /var/log/rabbitmq/rabbit2-sasl.log
  ##########
              Starting broker...
 completed with 6 plugins.

```

结束命令：

```shell
rabbitmqctl -n rabbit1 stop
rabbitmqctl -n rabbit2 stop
```

rabbit1操作作为主节点：

```shell
[root@super ~]# rabbitmqctl -n rabbit1 stop_app  
Stopping node rabbit1@super ...
[root@super ~]# rabbitmqctl -n rabbit1 reset	 
Resetting node rabbit1@super ...
[root@super ~]# rabbitmqctl -n rabbit1 start_app
Starting node rabbit1@super ...
[root@super ~]# 
```

rabbit2操作为从节点：

```shell
[root@super ~]# rabbitmqctl -n rabbit2 stop_app
Stopping node rabbit2@super ...
[root@super ~]# rabbitmqctl -n rabbit2 reset
Resetting node rabbit2@super ...
[root@super ~]# rabbitmqctl -n rabbit2 join_cluster rabbit1@'super' ###''内是主机名换成自己的
Clustering node rabbit2@super with rabbit1@super ...
[root@super ~]# rabbitmqctl -n rabbit2 start_app
Starting node rabbit2@super ...

```

查看集群状态：

```
[root@super ~]# rabbitmqctl cluster_status -n rabbit1
Cluster status of node rabbit1@super ...
[{nodes,[{disc,[rabbit1@super,rabbit2@super]}]},
 {running_nodes,[rabbit2@super,rabbit1@super]},
 {cluster_name,<<"rabbit1@super">>},
 {partitions,[]},
 {alarms,[{rabbit2@super,[]},{rabbit1@super,[]}]}]
```

### 集群管理

相关命令

```sh
# 将节点加入指定集群中。在这个命令执行前需要停止RabbitMQ应用并重置节点。
rabbitmqctl join_cluster {cluster_node} [–ram]

# 显示集群的状态。
rabbitmqctl cluster_status

# 修改集群节点的类型。在这个命令执行前需要停止RabbitMQ应用。
rabbitmqctl change_cluster_node_type {disc|ram}

# 将节点从集群中删除，允许离线执行。
rabbitmqctl forget_cluster_node [–offline]

# 在集群中的节点应用启动前咨询clusternode节点的最新信息，并更新相应的集群信息。这个和join_cluster不同，它不加入集群。考虑这样一种情况，节点A和节点B都在集群中，当节点A离线了，节点C又和节点B组成了一个集群，然后节点B又离开了集群，当A醒来的时候，它会尝试联系节点B，但是这样会失败，因为节点B已经不在集群中了。
rabbitmqctl update_cluster_nodes {clusternode}

# 取消队列queue同步镜像的操作。
rabbitmqctl cancel_sync_queue [-p vhost] {queue}

# 设置集群名称。集群名称在客户端连接时会通报给客户端。Federation和Shovel插件也会有用到集群名称的地方。集群名称默认是集群中第一个节点的名称，通过这个命令可以重新设置。
rabbitmqctl set_cluster_name {name}
```

### RabbitMQ镜像集群配置

> 上面已经完成RabbitMQ默认集群模式，但并不保证队列的高可用性，尽管交换机、绑定这些可以复制到集群里的任何一个节点，但是队列内容不会复制。虽然该模式解决一项目组节点压力，但队列节点宕机直接导致该队列无法应用，只能等待重启，所以要想在队列节点宕机或故障也能正常应用，就要复制队列内容到集群里的每个节点，必须要创建镜像队列。
>
> 镜像队列是基于普通的集群模式的，然后再添加一些策略，所以你还是得先配置普通集群，然后才能设置镜像队列，我们就以上面的集群接着做。

**设置的镜像队列可以通过开启的网页的管理端Admin->Policies，也可以通过命令。**

> rabbitmqctl set_policy my_ha "^" '{"ha-mode":"all"}'

> - Name:策略名称
> - Pattern：匹配的规则，如果是匹配所有的队列，是^.
> - Definition:使用ha-mode模式中的all，也就是同步所有匹配的队列。问号链接帮助文档。

### 负载均衡-HAProxy

HAProxy提供高可用性、负载均衡以及基于TCP和HTTP应用的代理，支持虚拟主机，它是免费、快速并且可靠的一种解决方案,包括Twitter，Reddit，StackOverflow，GitHub在内的多家知名互联网公司在使用。HAProxy实现了一种事件驱动、单一进程模型，此模型支持非常大的并发连接数。

#### 安装HAProxy

```shell
# 下载依赖包
yum install gcc vim wget
# 上传haproxy源码包
# 解压
tar -zxvf haproxy-1.6.5.tar.gz -C /usr/local
# 进入目录、进行编译、安装
cd /usr/local/haproxy-1.6.5
make TARGET=linux31 PREFIX=/usr/local/haproxy
make install PREFIX=/usr/local/haproxy
# 赋权
groupadd -r -g 149 haproxy
useradd -g haproxy -r -s /sbin/nologin -u 149 haproxy
# 创建haproxy配置文件
mkdir /etc/haproxy
vim /etc/haproxy/haproxy.cfg
```


#### 配置HAProxy

配置文件路径：/etc/haproxy/haproxy.cfg

```shell
#logging options
global
	log 127.0.0.1 local0 info
	maxconn 5120
	chroot /usr/local/haproxy
	uid 99
	gid 99
	daemon
	quiet
	nbproc 20
	pidfile /var/run/haproxy.pid

defaults
	log global
	
	mode tcp

	option tcplog
	option dontlognull
	retries 3
	option redispatch
	maxconn 2000
	contimeout 5s
   
     clitimeout 60s

     srvtimeout 15s	
#front-end IP for consumers and producters

listen rabbitmq_cluster
	bind 0.0.0.0:5672
	
	mode tcp
	#balance url_param userid
	#balance url_param session_id check_post 64
	#balance hdr(User-Agent)
	#balance hdr(host)
	#balance hdr(Host) use_domain_only
	#balance rdp-cookie
	#balance leastconn
	#balance source //ip
	
	balance roundrobin
	
        server node1 127.0.0.1:5673 check inter 5000 rise 2 fall 2
        server node2 127.0.0.1:5674 check inter 5000 rise 2 fall 2

listen stats
	bind 192.168.17.144:8100
	mode http
	option httplog
	stats enable
	stats uri /rabbitmq-stats
	stats refresh 5s
```

#### 启动HAproxy负载

```shell
/usr/local/haproxy/sbin/haproxy -f /etc/haproxy/haproxy.cfg
//查看haproxy进程状态
ps -ef | grep haproxy

访问如下地址对mq节点进行监控
http://172.16.98.133:8100/rabbitmq-stats
```

代码中访问mq集群地址，则变为访问haproxy地址:5672