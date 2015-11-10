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
>flume 允许用户通过多级配置，数据流事件需要通过多个agent最终达到终点。也可以配置多source对应一个sink，一个source对应多个sink。下面介绍几种常见模型

####多agent串联
>![multi agent](image/UserGuide_image_mult_agent.png)

>此结构可以用于不同系统中单一数据采集

####多数据源采集
>![multi source](image/UserGuide_image_multi_source.png)

>采集多数据源时，在每个源上配置一个agent，通过interceptor来给每个源打上标志。在通过一个agent上传到hdfs

####单一数据源多目的地
>![multi source](image/UserGuide_image_mult_sink.png)

>同一份数据需要向不同系统发送数据

###可靠性
>flume 提供端到端的可靠行保障。event通过source缓存到channel，然后sink获取channel中的数据。这个过程flume通过事物来控制，保障一个event没有成功地被sink送入下一个目的地之前，是不会从channel中移除的。

###可恢复性
>当配置为文件channel时，agent从启动文件中未处理的数据不会丢失。


##架构设计原则
>flume非常灵活，可以允许很多中部署场景。如果你打算在生产系统中部署flume。请花点时间考虑下你需要解决的问题。怎样利用flume更好地解决问题。

1. flume 是否能很好地解决你的问题
>如果你想采集日志到hdfs系统，flume很适合。对于其他案例，下面有些建议。
>flume用于传输不间断的事物性数据。对于一次所传输的数据不能超过内存或磁盘大小

2. 你是否对flume深入了解
>在确定使用flume之前，很有必要详细查看下官方使用手册。对flume深入理解后。能设计出更好解决方案

3. channel的使用技巧
>channel 可用户缓存数据。对于生产系统产生的数据，如果在客户端不能即使处理的情况下。channel可以充当缓存的作用。
>所以channel最好设计成文件channel。在新的1.6.0上支持kafka，如果条件允许这个是个不错的选择。

4. flume 高可用必要性讨论
>传输生产系统的实时数据的系统。是不允许有数据丢失的。flume数据传输不能任意中断，面对这种情况，我们如果需要检查主机，或部署其他应用，需要停止flume，怎么办？
>在flume中我们可以通过在生产系统方配置大容量channel来缓存数据。
>构造ha传输系统

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
    
    #sinks group
    source_apache.sinkgroups.g1.sinks = sink1 sink2
    # load_balance type
    source_apache.sinkgroups.g1.processor.type = load_balance
    source_apache.sinkgroups.g1.processor.backoff   = true
    source_apache.sinkgroups.g1.processor.selector  = random

    
> 此配置文件配置两个sink，两个channel，分别发向dn3，dn4主机,随机选择发送目的主机

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
4.	如果配置内存 channel，在实际测试过程中会丢失数据

### 通过flume sdk构建高可用传输系统

####配置文件
    client.type = default_loadbalance
    hosts = dn1 dn2 dn3 dn4 dn5
    hosts.dn1 = dn1:4547
    hosts.dn2 = dn2:4547
    hosts.dn3 = dn3:4547
    hosts.dn4 = dn4:4547
    hosts.dn5 = dn5:4547

    backoff = true
    connect-timeout = 20000
    request-timeout = 20000

####程序代码
	 CommandLineParser parser = new BasicParser();
        Options options = new Options();

        options.addOption("h", "help", false, "send file use flume sdk");
        options.addOption("c", "conf", true, "configuration file ");
        options.addOption("f", "file", true, "The file to send");

        CommandLine commandLine = parser.parse(options, args);
        String configFile = null;
        String sendFile = null;
        if (commandLine.hasOption('h')) {
            logger.info("send a file content by flume");
            System.exit(0);
        }

        if (commandLine.hasOption('c')) {
            configFile = commandLine.getOptionValue('c');
        }
        if (commandLine.hasOption('f')) {
            sendFile = commandLine.getOptionValue('f');
        }

        if (configFile == null || sendFile == null ) {
            logger.error("Must have conf and file ");
            System.exit(-2);
        }


        logger.info("Configuration: " + configFile);
        logger.info("file: " + sendFile);

        RpcClient client = RpcClientFactory.getInstance(new File(configFile));

        InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(sendFile)));
        BufferedReader bufferedReader = new BufferedReader(reader);
        long num = 1;
        String line = null;
        Map<String, String> headers = new HashMap<String, String>();
        while( (line = bufferedReader.readLine()) != null ) {
            long now = System.currentTimeMillis();
            headers.put("timestamp", Long.toString(now));
            Event event = EventBuilder.withBody(line, Charset.forName("UTF-8"), headers);
            try {
                client.append(event);
            } catch (EventDeliveryException e) {
                e.printStackTrace();
            }

            if (num % 10000 == 0) {
                logger.info("Send " + num + " lines");
            }
            num ++;
        }

        reader.close();
        client.close();
    }

>详细代码参考 [MyRpcClientFacade.java](https://github.com/super-sponge/flume-demo/blob/master/src/main/java/com/example/client/MyRpcClientFacade.java)

#### 程序调用方式
>参考[sendData](https://github.com/super-sponge/flume-demo/tree/master/src/main/scripts/sendData.sh)

####说明
>flume 提供failover 与 loadbalance 配置，生产环境中建议使用loadbalance
>在实际测试中配置channel为内存时，如程序异常会发生数据重复。主机停机为测试。
>在实际测试中采用文件channel时，在程序发生异常，数据不重复，不丢失
>在实际测试中，添加接受主机的数量，对传输不会有明显提高。


## 常见问题
### OOM 问题
	flume 报错：
    java.lang.OutOfMemoryError: GC overhead limit exceeded
    或者：
    java.lang.OutOfMemoryError: Java heap space
    Exception in thread "SinkRunner-PollingRunner-DefaultSinkProcessor" java.lang.OutOfMemoryError: Java heap space

>Flume 启动时的最大堆内存大小默认是 20M，线上环境很容易 OOM，因此需要你在 flume-env.sh 中添加 JVM 启动参数: 

	JAVA_OPTS="-Xms8192m -Xmx8192m -Xss256k -Xmn2g -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-UseGCOverheadLimit"
>然后在启动 agent 的时候一定要带上 -c conf 选项，否则 flume-env.sh 里配置的环境变量不会被加载生效。

###小文件写入 HDFS 延时的问题
>flume 的 sink 已经实现了几种最主要的持久化触发器：
>比如按大小、按间隔时间、按消息条数等等，针对你的文件过小迟迟没法写入 HDFS 持久化的问题，
>那是因为你此时还没有满足持久化的条件，比如你的行数还没有达到配置的阈值或者大小还没达到等等，
>可以针对上面 3.2 小节的配置微调下，例如：
	
    agent1.sinks.log-sink1.hdfs.rollInterval = 20
>当迟迟没有新日志生成的时候，如果你想很快的 flush，那么让它每隔 20s flush 持久化一下，agent 会根据多个条件，优先执行满足条件的触发器。

    # Number of seconds to wait before rolling current file (in 600 seconds)
    agent.sinks.sink.hdfs.rollInterval=600

    # File size to trigger roll, in bytes (256Mb)
    agent.sinks.sink.hdfs.rollSize = 268435456

    # never roll based on number of events
    agent.sinks.sink.hdfs.rollCount = 0

    # Timeout after which inactive files get closed (in seconds)
    agent.sinks.sink.hdfs.idleTimeout = 3600

    agent.sinks.HDFS.hdfs.batchSize = 1000

### 数据重复写入，丢失问题
>Flume的HDFSsink在数据写入/读出Channel时，都有Transcation的保证。当Transaction失败时，会>回滚，然后重试。但由于HDFS不可修改文件的内容，假设有1万行数据要写入HDFS，而在写入5000行时，网>络出现问题导致写入失败，Transaction回滚，然后重写这10000条记录成功，就会导致第一次写入的>5000>行重复。这些问题是 HDFS 文件系统设计上的特性缺陷，并不能通过简单的Bugfix来解决。我们只能>关闭批量写入，单条事务保证，或者启用监控策略，两端对数。
>
>Memory和exec的方式可能会有数据丢失，file 是 end to end 的可靠性保证的，但是性能较前两者要>差。
>
>end to end、store on failure 方式 ACK 确认时间设置过短（特别是高峰时间）也有可能引发数据的重复写入。

###在 Flume 中如何修改、丢弃、按预定义规则分类存储数据？
> 使用interceptor

## Reference

>[flume blog](http://my.oschina.net/leejun2005/blog/288136)
>[flume mailbox](http://mail-archives.apache.org/mod_mbox/flume-user)


    

