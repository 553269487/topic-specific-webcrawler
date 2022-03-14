package edu.upenn.cis.cis455.Entity;

import java.io.Serializable;

public class DocObj implements Serializable, Entity {
    private String url;
    private String docContent;
    private String type;

    public DocObj(String url, String docContent, String type) {
        this.url = url;
        this.docContent = docContent;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getDocContent() {
        return docContent;
    }
}
