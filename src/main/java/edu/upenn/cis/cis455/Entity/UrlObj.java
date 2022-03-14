package edu.upenn.cis.cis455.Entity;

import java.io.Serializable;

public class UrlObj implements Serializable, Entity {
    private static final long serialVersionUID = 6569052224975480254L;
    private String url;
    private String hash;
    private long lastAccessTime;

    public String getUrl() {
        return url;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public String getHash() {
        return hash;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public UrlObj(String url, String hash, long lastAccessTime) {
        this.url = url;
        this.hash = hash;
        this.lastAccessTime = lastAccessTime;
    }

    public UrlObj(String url) {
        this.url = url;
    }
}
