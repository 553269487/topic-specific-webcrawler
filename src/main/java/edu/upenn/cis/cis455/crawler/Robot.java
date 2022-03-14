package edu.upenn.cis.cis455.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Robot {
    private static Logger logger = LogManager.getLogger(Robot.class);
    private String robotUrl;
    private boolean ready;
    private Set<String> visitedSet;
    private Set<String> allowSet;
    private Set<String> disallowSet;
    private int delay;
    private long vistedTime;
    private String[] agents;

    public Robot(String url){
        robotUrl = url + "/robots.txt";
        ready = false;
        visitedSet = new HashSet<>();
        allowSet = new HashSet<>();
        disallowSet = new HashSet<>();
        agents = new String[]{"*", "cis455crawler"};
        try {
            readRobot_txt();
        } catch (IOException e) {
            logger.error("read robot.txt fail");
            e.printStackTrace();
        }

    }


    public void readRobot_txt() throws IOException {
        URL url = new URL(robotUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "cis455crawler");
        connection.connect();
        int status = connection.getResponseCode();
        if(status == 200){
//            logger.info("robot.txt found");
            InputStream inputStream = connection.getInputStream();
            String robotTxt = new String(inputStream.readAllBytes());
            robotTxt = robotTxt.toLowerCase();
            String tmp[] = robotTxt.split("user-agent:");
            for(int i = 0; i < tmp.length; i++){
                tmp[i] = tmp[i].trim();
            }
            for(String agent : agents){
                for(String s : tmp){
                    if(s.startsWith(agent + "\n") || s.startsWith(agent + "\r\n")){
                        ready = true;
                        String[] rows = s.split("\r\n|\r|\n");
                        for(String row : rows){
                            String[] configs = row.split(":");
                            if(configs == null || configs.length != 2){
                                continue;
                            }
                            String key = configs[0].trim(), val = configs[1].trim();
                            if(key.equals("allow")){
                                allowSet.add(val);
                            }
                            if(key.equals("disallow")){
                                disallowSet.add(val);
                            }
                            if(key.equals("crawl-delay")){
                                delay = Integer.valueOf(val);
                            }
                        }
                    }

                }
            }
//            logger.info("agent scanned");


        } else if (status == 301 || status == 302) {
            robotUrl = connection.getHeaderField("Location");
            logger.info("Robot.txt moved to {}", robotUrl);
            readRobot_txt();
        }else{
            logger.warn("Robot not found");
        }
    }

    public boolean isPathAllowed(String pathUrl){
        boolean allowed = allowSet.contains(pathUrl);
        boolean notAllowed_or_Visted = visitedSet.contains(pathUrl) || disallowSet.contains(pathUrl);
        visitedSet.add(pathUrl);
        if(allowed) return true;
        if(notAllowed_or_Visted) return false;
        //if no specific, return true;
        return true;


    }

    public boolean isReady() {
        return ready;
    }

    public boolean needDelay(){
        if(!ready){
            logger.info("not ready so delay");
            return true;
        }
        if(System.currentTimeMillis() < vistedTime + delay * 1000){
            return true;
        }
        vistedTime = System.currentTimeMillis();
        return false;
    }

    public String getRobotUrl() {
        return robotUrl;
    }

    public Set<String> getVisitedSet() {
        return visitedSet;
    }

    public Set<String> getAllowSet() {
        return allowSet;
    }

    public Set<String> getDisallowSet() {
        return disallowSet;
    }

    public int getDelay() {
        return delay;
    }

    public long getVistedTime() {
        return vistedTime;
    }

    public String[] getAgents() {
        return agents;
    }

    public static void main(String[] args) {
        Robot robot1 = new Robot("https://youtube.com");
        Robot robot2 = new Robot("https://e-hentai.org");

        logger.info("test");
    }

}
