package com.example.sink;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.AsyncHbaseEventSerializer;
import org.hbase.async.AtomicIncrementRequest;
import org.hbase.async.PutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple serializer to be used with the AsyncHBaseSink
 * that returns puts from an event, by writing the event
 * body into it. The headers are discarded. It also updates a row in hbase
 * which acts as an event counter.
 * <p/>
 * Takes optional parameters:<p>
 * <tt>rowPrefix:</tt> The prefix to be used. Default: <i>default</i><p>
 * <tt>incrementRow</tt> The row to increment. Default: <i>incRow</i><p>
 * <tt>suffix:</tt> <i>uuid/random/timestamp.</i>Default: <i>uuid</i><p>
 * <p/>
 * Mandatory parameters: <p>
 * <tt>cf:</tt>Column family.<p>
 * Components that have no defaults and will not be used if absent:
 * <tt>payloadColumn:</tt> Which column to put payload in. If it is not present,
 * event data will not be written.<p>
 * <tt>incrementColumn:</tt> Which column to increment. If this is absent, it
 * means no column is incremented.
 */

public class AccessLogAsyncHbaseEventSerializer implements AsyncHbaseEventSerializer {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogAsyncHbaseEventSerializer.class);

    private byte[] table;
    private byte[] cf;

    private byte[][] columnNames;
    private Event currentEvent;

    private final List<PutRequest> puts = new ArrayList<PutRequest>();
    private byte[] currentRowKey;
    private final List<AtomicIncrementRequest> incs = new ArrayList<AtomicIncrementRequest>();
    private final byte[] eventCountCol = "eventCount".getBytes();

    @Override
    public void initialize(byte[] table, byte[] cf) {
        this.table = table;
        this.cf = cf;
    }

    /**
     *  解析channel发送过来的数据，构造puts，并返回
     * @return
     */

    @Override
    public List<PutRequest> getActions() {
        // Split the event body and get the values for the columns

        String eventStr = new String(currentEvent.getBody());
        String[] cols = logTokenize(eventStr);

        String req = cols[4];
        String reqPath = req.split(" ")[1];
        int pos = reqPath.indexOf("?");
        if (pos > 0) {
            reqPath = reqPath.substring(0, pos);
        }
        if (reqPath.length() > 1 && reqPath.trim().endsWith("/")) {
            reqPath = reqPath.substring(0, reqPath.length() - 1);
        }

        String req_ts_str = cols[3];
        Long currTime = System.currentTimeMillis();
        String currTimeStr = null;
        if (req_ts_str != null && !req_ts_str.equals("")) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.US);
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                currTimeStr = df2.format(df.parse(req_ts_str));
                currTime = df.parse(req_ts_str).getTime();
            } catch (ParseException e) {

                logger.error("parse req time error,using system.current time.");

            }

        }
        long revTs = Long.MAX_VALUE - currTime;
        currentRowKey = (Long.toString(revTs) + reqPath).getBytes();
        logger.info("currentRowKey: " ,new String(currentRowKey));

        puts.clear();
        for (int i = 0; i < cols.length; i++) {
            PutRequest putReq = new PutRequest(table, currentRowKey, cf, columnNames[i], cols[i].getBytes());
            puts.add(putReq);
        }

        //add column
        PutRequest reqPathPutReq = new PutRequest(table, currentRowKey, cf, "req_path".getBytes(), reqPath.getBytes());
        puts.add(reqPathPutReq);

        PutRequest reqTsPutReq = new PutRequest(table, currentRowKey, cf, "req_ts".getBytes(), currTimeStr.getBytes());
        puts.add(reqTsPutReq);

        return puts;
    }

    /**
     *  解析日志并返回数组
     * @param eventStr  被解析的字符串
     * @return
     */

    public String[] logTokenize(String eventStr) {

        String logEntryPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+|-) \"([^\"]+)\" \"([^\"]+)\"";

        Pattern p = Pattern.compile(logEntryPattern);

        Matcher matcher = p.matcher(eventStr);

        if (!matcher.matches())

        {

            System.err.println("Bad log entry (or problem with RE?):");

            System.err.println(eventStr);

            return null;

        }

        String[] columns = new String[matcher.groupCount()];

        for (int i = 0; i < matcher.groupCount(); i++)

        {

            columns[i] = matcher.group(i + 1);

        }

        return columns;

    }

    public List<AtomicIncrementRequest> getIncrements() {

        incs.clear();
        incs.add(new AtomicIncrementRequest(table, "totalEvents".getBytes(), cf, eventCountCol));

        return incs;
    }

    @Override
    public void cleanUp() {
        table = null;
        cf = null;
        currentEvent = null;
        columnNames = null;
        currentRowKey = null;
    }

    @Override
    public void configure(Context context) {

        String cols = context.getString("columns");
        String[] colsNames = cols.split(",");
        columnNames = new byte[colsNames.length][];
        int i = 0;
        for (String name : colsNames) {
            columnNames[i++] = name.getBytes();
        }

    }

    @Override
    public void setEvent(Event event) {
        this.currentEvent = event;
    }

    @Override
    public void configure(ComponentConfiguration conf) {
        // TODO Auto-generated method stub
    }

}
