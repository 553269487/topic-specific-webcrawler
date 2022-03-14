package edu.upenn.cis.cis455.enums;

public enum Dbname {
    catalogName("catLog"),
    userdbName("userdb"),
    docdbName("docdb"),
    urldbName("urldb");

    private String name;
    Dbname(String name){
        this.name = name;
    }
    public String value(){
        return this.name;
    }

}
