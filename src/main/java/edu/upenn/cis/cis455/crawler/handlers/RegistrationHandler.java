package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegistrationHandler implements Route {
    private static Logger logger = LogManager.getLogger(RegistrationHandler.class);
    StorageInterface db;
    public RegistrationHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String username = request.queryParams("username");
        String password = request.queryParams("password");
        if(username == null || username.isEmpty() || password.isEmpty()|| password == null){
            logger.warn("username or password cant be empty");
            response.redirect("register.html");
            return Spark.halt(400);

        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(md == null){
            logger.warn("no messgeDigest");
            response.redirect("register.html");
            return Spark.halt(400);
        }

        password = new String(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        int flag = db.addUser(username,password);
        if(flag == 0){
            response.status(200);
            response.body("Registered" + username + password);
        }else {
            response.status(400);
            response.body("User already in the db");
        }
        return response.body();

    }
}
