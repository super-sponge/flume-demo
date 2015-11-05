#Flume 学习指导
##Flume 介绍
###概述
*	Flume 是一个分布的，可靠的，高可用的高性能的日志收集系统。他主要用于从不同的数据源高效地收集，聚合和移动数据到中心存储。
*	Flume 不仅仅限于日志收集。他的数据源（source）是可以定制的.flume 可以传输大规模事件型数据，包括网络流量数据，社交媒体数据，电子邮件（email）等其他可事务化数据。
*	Flume 是apache软件基金会顶级项目
        
###系统需求
1.	Java运行环境,1.6 或更高(建议使用1.7.x)
2.	内存: 在配置channel，source和sink时需要充足的内存
3.	磁盘空间: 在配置channel或sink时需冲足的磁盘空间
4.	目录权限: agent需要读／写权限


##架构
###数据流模型
>如图flume agent 由source channel 和sink三部分组成
>![data flow model](image/UserGuide_image_flume_model.png)
>flume source从外部数据源头消费数据，通过channel传输到sink（也可以是另一个source），通过sink把数据传输到目标系统。如上图web Server 的日志通过source传输到channel然后通过sink传输到hdfs
>> 名词解释
>> source	收集数据
>> channel	数据缓存渠道
>> sink		从channel中取出数据发送到下一个目的地
>> 事件	   数据传输的最小单位，包括事件头和事件体

###复杂数据流模型
>flume 允许用户通过多级配置，数据流事件需要通过多个agent最终达到终点。也可以配置多source对应一个sink，一个source对应多个sink。下面我们会详细介绍

###可靠性
>flume 提供端到端的可靠行保障。event通过source缓存到channel，然后sink获取channel中的数据。这个过程flume通过事物来控制，保障一个event没有成功地被sink送入下一个目的地之前，是不会从channel中移除的。

###可恢复性
>当配置为文件channel时，agent从启动文件中未处理的数据不会丢失。


##安装与配置
1. 配置java环境
2. 解压flume的包
3. 配置flume配置文件
4. 启动flume agent


##实际案例

### 接收网络数据并打印到控制台
#### 配置文件
	# flume agent 中的组件名称
	a1.sources = r1
	a1.sinks = k1
	a1.channels = c1

    #配置source
    a1.sources.r1.type = netcat
    a1.sources.r1.bind = localhost
    a1.sources.r1.port = 44444

    # 配置sink（直接打印到终端）
    a1.sinks.k1.type = logger

    # 使用内存channel
    a1.channels.c1.type = memory
    a1.channels.c1.capacity = 1000
    a1.channels.c1.transactionCapacity = 100

    # 把source和sink连接到channel
    a1.sources.r1.channels = c1
    a1.sinks.k1.channel = c1
    
#### 备注
1. 把配置信息保持到flume的conf目录下面，文件名为example.conf
2. flume的配置文件中参照示例书写，分为组件名称，source，sink，channel和link区域

#### 启动
>bin/flume-ng agent  --conf-file ./conf/example.conf --name a1 -Dflume.root.logger=INFO,console
>
>在另一个终端输入 telnet localhost 44444 并输入字符串，观察启动命令的那个终端，看信息是否打印出来。

####扩展
>如果要把输入的信息保持到文件怎么处理？ 
>查看[文件链接](http://flume.apache.org/FlumeUserGuide.html#file-roll-sink "file-roll-sink"). 更改sink区域的内容,详细查看下面配置文件（agent会监控配置文件，如果发生改动会自动读入）
  
	# flume agent 中的组件名称
    a1.sources = r1
    a1.sinks = k1
    a1.channels = c1

    #配置source
    a1.sources.r1.type = netcat
    a1.sources.r1.bind = localhost
    a1.sources.r1.port = 44444

    # 配置sink（直接打印到终端）
    #a1.sinks.k1.type = logger
    a1.sinks.k1.type = file_roll 
    a1.sinks.k1.sink.directory = /tmp/flume 
    a1.sinks.k1.sink.rollInterval = 300 

    # 使用内存channel
    a1.channels.c1.type = memory
    a1.channels.c1.capacity = 1000
    a1.channels.c1.transactionCapacity = 100

    # 把source和sink连接到channel
    a1.sources.r1.channels = c1
    a1.sinks.k1.channel = c1

### 收集apache日志并上传到hdfs

####配置文件
#####采集apache日志配置文件

    source_apache.channels=m1 m2
    source_apache.sinks=sink1 sink2
    source_apache.sources=apache_server    
    
    source_apache.sources.apache_server.type=exec
    source_apache.sources.apache_server.command=tail -F /home/tomcat/app/apache-tomcat-hlw/logs/localhost_access_log.txt
    source_apache.sources.apache_server.selector.type=replicating
    source_apache.sources.apache_server.interceptors=i1
    source_apache.sources.apache_server.interceptors.i1.type=timestamp

	source_apache.channels.m1.type=memory
    source_apache.channels.m1.capacity=1000
    source_apache.channels.m1.transactionCapacity=100
    source_apache.channels.m2.type=memory
    source_apache.channels.m2.capacity=1000
    source_apache.channels.m2.transactionCapacity=100    

	source_apache.sinks.sink1.type=avro
    source_apache.sinks.sink1.hostname=dn3
    source_apache.sinks.sink1.port=4547    
    source_apache.sinks.sink2.type=avro
    source_apache.sinks.sink2.hostname=dn4
    source_apache.sinks.sink2.port=4547    
    
	source_apache.sources.apache_server.channels=m1 m2
	source_apache.sinks.sink1.channel=m1
	source_apache.sinks.sink2.channel=m2
    
> 此配置文件配置两个sink，两个channel，分别发想dn3，dn4主机

#####接收采集apache的日志并上传到hdfs
    collector.sources=AvroIn
    collector.channels=mc1
    collector.sinks=hdfsout

    collector.sources.AvroIn.bind=0.0.0.0
    collector.sources.AvroIn.port=4547
    collector.sources.AvroIn.type=avro
    collector.sources.AvroIn.interceptors=i1
    collector.sources.AvroIn.interceptors.i1.type=host
    collector.sources.AvroIn.interceptors.i1.useIP=false

    collector.channels.mc1.capacity=1000
    collector.channels.mc1.type=memory


    collector.sinks.hdfsout.type=hdfs
    collector.sinks.hdfsout.hdfs.path=/outdata/apachelog/%y-%m-%d
    collector.sinks.hdfsout.hdfs.filePrefix = %{host}-event-
    collector.sinks.hdfsout.hdfs.fileType=DataStream
    collector.sinks.hdfsout.hdfs.writeFormat=Text
    collector.sinks.hdfsout.hdfs.minBlockReplicas=1
    collector.sinks.hdfsout.hdfs.rollInterval = 300
    collector.sinks.hdfsout.hdfs.rollSize = 0
    collector.sinks.hdfsout.hdfs.rollCount=0
    collector.sinks.hdfsout.hdfs.idleTimeout=0

    collector.sources.AvroIn.channels=mc1
    collector.sinks.hdfsout.channel=mc1

>dn3,dn4都使用上面的配置文件，通过interceptor中加入host，在构造文件名称时加以区分。

####启动
>启动命令参考上面实例

####备注
1.	本实例中采集apache日志过程中，启动flume的用户必须要有对日子的读和执行权限。
2.	本实例演示一个数据源向两个（或多个，可以自由扩展）系统提供数据。
3.	在生产系统中日志采集过程中channel应配置文件channel，以防止数据丢失。