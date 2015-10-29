package com.example.utils;

import static org.junit.Assert.*;

/**
 * Created by sponge on 15-10-28.
 */
public class AccessTomcatLogParserTest {

    @org.junit.Test
    public void testParse() throws Exception {
        AccessTomcatLog atl = null;
        atl = AccessTomcatLogParser.parse("172.30.121.163 - - [28/Oct/2015:10:38:19 +0800] \"POST /dm/mall/Goods!qryGoods HTTP/1.1\" 200 264 \"http://dn6:8080/dm/view/mall/offer_list.jsp?skey=&t1=1&t2=\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/45.0.2454.101 Chrome/45.0.2454.101 Safari/537.36\"");
        System.out.println(atl);
    }
}