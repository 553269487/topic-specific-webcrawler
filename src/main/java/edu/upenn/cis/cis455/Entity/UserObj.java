package edu.upenn.cis.cis455.Entity;

import java.io.Serializable;

public class UserObj implements Serializable, Entity{
    private String username;
    private String password;
    public UserObj(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
