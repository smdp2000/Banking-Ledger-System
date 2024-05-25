package dev.codescreen.bankledger.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ping {
    private String serverTime;

    public Ping() {
        this.serverTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
}
