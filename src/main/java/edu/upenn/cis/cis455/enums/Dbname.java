package edu.upenn.cis.cis455.crawler.enums;

public enum Dbname {
    catalogName("catLog");


    private String name;
    Dbname(String name){
        this.name = name;
    }
    public String value(){
        return this.name;
    }

}
