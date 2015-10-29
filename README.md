# flume 演示

## flume 通过exec 获取apache日志，存入hbase
###日志获取
    更改TOMCAT_PATH/conf/server.xml 
    <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
    
                   prefix="localhost_access_log." suffix=".txt" renameOnRotate="true"
    
                   pattern="combined" />
### 编写AsyncHbaseEventSerializer
    详细参考AccessLogAsyncHbaseEventSerializer类,本类参考了SampleAsyncHbaseEventSerializer代码
### 编写配置文件
    详细查看conf下的配置文件
### 运行Flume Agent和HBase Sink    
    后台方式运行Flume Agent    
    nohup $FLUME_HOME/bin/flume-ng agent -c $FLUME_HOME /conf -f $FLUME_HOME /conf/flume-src-agent.conf -n source_agent &
    
    登录hbase 创建access_log表
        hbase shell
        create 'access_log','cb'
        
    后台方式运行HBase Sink
    nohup $FLUME_HOME/bin/flume-ng agent -c $FLUME_HOME/conf -f $FLUME_HOME//conf/flume-sink-hbase.conf -n collector &
### 在hive中创建表查看hbase数据
    CREATE EXTERNAL TABLE hbase_access_log(key string, host_name string ,remote_host string,remote_user string,event_ts string,req string,req_status string,resp_bytes int,ref string,agent string)
        STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
        WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,cb:host_name,cb:remote_host,cb:remote_user,cb:event_ts,cb:req,cb:req_status,cb:resp_bytes,cb:ref,cb:agent")
        TBLPROPERTIES("hbase.table.name"="access_log");
## 参考
* 获取详细flume请查看 [Flume] [1]
* csdn 资料 [csdn blog] [2]

[1]: http://flume.apache.org/FlumeUserGuide.html#asynchbasesink  "flume guide"
[2]: http://blog.csdn.net/yaoyasong/article/details/39400829 "csdn blog"

