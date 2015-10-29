package com.example.utils;

import java.util.Date;

/**
 * Created by sponge on 15-10-28.
 */
public class AccessTomcatLog {
    private String clientIp;

    private String clientIndentity;

    private String remoteUser;

    private Date dateTime;

    private String request;

    private String httpStatusCode;

    private String bytesSent;

    private String referer;

    private String userAgent;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientIndentity() {
        return clientIndentity;
    }

    public void setClientIndentity(String clientIndentity) {
        this.clientIndentity = clientIndentity;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(String bytesSent) {
        this.bytesSent = bytesSent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "AccessTomcatLog{" +
                "clientIp='" + clientIp + '\'' +
                ", clientIndentity='" + clientIndentity + '\'' +
                ", remoteUser='" + remoteUser + '\'' +
                ", dateTime=" + dateTime +
                ", request='" + request + '\'' +
                ", httpStatusCode='" + httpStatusCode + '\'' +
                ", bytesSent='" + bytesSent + '\'' +
                ", referer='" + referer + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
