package com.example.client;

import org.apache.commons.cli.*;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by sponge on 15-11-9.
 */
public class MyRpcClientFacade {

    private static final Logger logger = LoggerFactory.getLogger(MyRpcClientFacade.class);
    private RpcClient   client;
    private String host;
    private int port;

    private Properties pops = null;

    public void init(String host, int port){
        this.host = host;
        this.port = port;
        this.client = RpcClientFactory.getDefaultInstance(host, port);
        // Use the following method to create a thrift client (instead of the above line):
        // this.client = RpcClientFactory.getThriftInstance(hostname, port);
        pops = null;
    }

    public void init(Properties pops) {
        this.client = RpcClientFactory.getInstance(pops);
        this.pops = pops;
    }

    public void sendDataToFlume(String data) {
        Map<String, String> headers = new HashMap<String, String>();
        long now = System.currentTimeMillis();
        headers.put("timestamp", Long.toString(now));
        Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"), headers);

        try {
            client.append(event);
        } catch (EventDeliveryException e) {
            client.close();
            client = null;
            if (this.pops != null) {
                this.client = RpcClientFactory.getInstance(pops);
            } else {
                client = RpcClientFactory.getDefaultInstance(this.host, this.port);
            }
            e.printStackTrace();
        }
    }

    public void cleanUp() {
        client.close();
    }

    public static void main(String[] args) throws ParseException, IOException {
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

}
