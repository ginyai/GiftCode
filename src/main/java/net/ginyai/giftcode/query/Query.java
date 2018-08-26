package net.ginyai.giftcode.query;

import java.util.UUID;

public class Query implements Comparable<Query>{
    private long startTime;
    private UUID source;
    private String code;

    public Query(long startTime, UUID source, String code) {
        this.startTime = startTime;
        this.source = source;
        this.code = code;
    }

    @Override
    public int compareTo(Query o) {
        return Long.compare(startTime,o.startTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public UUID getSource() {
        return source;
    }

    public String getCode() {
        return code;
    }
}
