package com.example.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sponge on 15-10-28.
 */
public class AccessTomcatLogParser {

    private static final Logger logger = LoggerFactory.getLogger(AccessTomcatLogParser.class);

    private static String pattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+|-) \"([^\"]+)\" \"([^\"]+)\"";

    private static Pattern p = Pattern.compile(pattern);

    private static Date getDateTime(String strDateTime) {
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
        try {
            Date dt = sdf.parse(strDateTime);
            return dt;
        } catch (ParseException e) {
            return null;
        }
    }

    public static AccessTomcatLog parse(String line){

        Matcher matcher = p.matcher(line);

        if (matcher.matches()){

            AccessTomcatLog accessLog = new AccessTomcatLog();

            accessLog.setClientIp(matcher.group(1));

            accessLog.setClientIndentity(matcher.group(2));

            accessLog.setRemoteUser(matcher.group(3));

            accessLog.setDateTime(getDateTime(matcher.group(4)));

            accessLog.setRequest(matcher.group(5));

            accessLog.setHttpStatusCode(matcher.group(6));

            accessLog.setBytesSent(matcher.group(7));

            accessLog.setReferer(matcher.group(8));

            accessLog.setUserAgent(matcher.group(9));

            return accessLog;

        }

        logger.warn("This line is not a valid combined log, ignored it. -- " + line);

        return null;
    }
}
