package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.Entity.UrlObj;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.StorageDB;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Stub class for a thread worker that handles Web requests
 */
public class Worker implements Runnable {
    private final static Logger logger = LogManager.getLogger(Worker.class);

    private BlockingQueue<String> blockingQueue;
    private Crawler crawler;
    private StorageDB db;

    private String Id;
    private volatile boolean run = true;



    public Worker(BlockingQueue blockingQueue, StorageDB db, Crawler master) {
        this.blockingQueue = blockingQueue;
        this.db = db;
        this.crawler = master;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        logger.debug("Worker: " + this.Id + " Working");
        crawler.setWorking(true);
        while(run){
//            crawler.setWorking(false);
            String frontUrl = null;
            long Timestamp = System.currentTimeMillis();
            while(run){
                try {
//                    logger.info("waiting for new url");
                    frontUrl = blockingQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(System.currentTimeMillis()- Timestamp > 30 * 1000){
                    logger.info("timeout, wait for url but nobody comes");
                    break;
                }
                if(frontUrl!=null || crawler.isDone()) break;;
            }
            logger.debug("new link poll()");
            if(frontUrl == null){

                break;
            }


            logger.info("pop new link out of the blockingqueue:{}", frontUrl);
            URLInfo urlInfo = new URLInfo(frontUrl);
            String hostUrl = urlInfo.getHost();
            String pathUrl = urlInfo.getPath();

            //if crawled enough document
            if(crawler.isDone()){
                logger.info("crawler is done");
                break;
            }

            //check if the robot is all set to crawl
            if(!crawler.isRobotReady(hostUrl)) {
                logger.info("Robot not ready, no good robot");
                continue;
            }

            //check crawled delay?
            boolean flag = crawler.isCrawlerDelay(hostUrl);
            if(flag == true) logger.info("delayed" );
            while(flag){
                flag = crawler.isCrawlerDelay(hostUrl);
            }
            logger.info("not delayed" );


            //if all above passes, we check if the path is visited or disallowed
            if(!crawler.isPathAllowed(hostUrl,pathUrl)){
                logger.info("Path not allowed");
                continue;
            }

            logger.debug("check why stuck");
            //get access time;
            UrlObj urlObj = db.getUrlObj(pathUrl);
            long lastAccessTime = 0;
            if(urlObj != null) lastAccessTime = urlObj.getLastAccessTime();

            HttpURLConnection connection = buildConnect(hostUrl, "HEAD");
            connection.setIfModifiedSince(lastAccessTime);
            int status = -1;
            String document = null;
            boolean canJsoup = false;
            int contentLength;
            String contentType;

            try {
                status = connection.getResponseCode();
                logger.info("status: {}", status);
            } catch (IOException e) {
                logger.error("connection responsecode error");
                e.printStackTrace();
            }

            if(status == 200){
                contentLength = connection.getContentLength();
                contentType = connection.getContentType();
                if(contentType.equals("text/html")) canJsoup = true;
                if(!crawler.isDocValid(contentType, contentLength)){
                    connection.disconnect();
                    db.removeUrlObj(pathUrl);
                    logger.info("Invalid content");
                    continue;
                }
                connection.disconnect();
                connection = buildConnect(pathUrl, "GET");
                try {
                    status = connection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(status != 200){
                    logger.error("downloading fail, the code status is {}", status);
                    db.removeUrlObj(pathUrl);
                    continue;
                }
                logger.info(pathUrl + ": downloading");
                InputStream inputStream = null;
                try {
                    inputStream = connection.getInputStream();
                } catch (IOException e) {
                    logger.error("input stream empty");
                    e.printStackTrace();
                    continue;
                }
                try {
                    document = new String(inputStream.readAllBytes());
                } catch (IOException e) {
                    logger.error("inputstream to string fail");
                    e.printStackTrace();
                    continue;
                }

                if(db.containsDocument(document)){
                    logger.info("document duplicate");
                }else {
                    crawler.incCount();
                    logger.info("crawling new doc, doc Count: {}",crawler.getCrawledCount().get());
                }

                String hash = db.addDocument(pathUrl,document,contentType);
                db.addUrlObj(new UrlObj(pathUrl, hash , connection.getLastModified()));
                logger.info("write int hash-doc and url-hash");
            }
            else if(status == 304){
                logger.info(pathUrl + ": not modified");

                if(db.getDocument(pathUrl) != null){
                    logger.info("found document");
                    document = db.getDocument(pathUrl).getDocContent();
                    contentType = db.getDocument(pathUrl).getType();
                    if(contentType.equals("text/html")){
                        logger.info("can have new link");
                        canJsoup = true;
                    }
                }else{
                    logger.info("no document");
                    continue;
                }
            }else{
                logger.warn("Robot not found");
            }

            connection.disconnect();
            logger.info("busyworker count:{}",crawler.getBusyWorkers().get());
            if(!canJsoup || crawler.isSeen(document) ) continue;
            Document res = Jsoup.parse(document);
            res.setBaseUri(pathUrl);
            ArrayList<Element> outLinks = res.getElementsByAttribute("href");
            for(Element link : outLinks){
                String newUrl = link.absUrl("href");
                try {
                    blockingQueue.put(newUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("put new link to the blockingqueue：{}",newUrl);
            }

        }
        crawler.setWorking(false);
        logger.info("Worker {} stop running", Id);
        crawler.notifyThreadExited();


    }

    public HttpURLConnection buildConnect(String url, String Method){
        URL urlnet = null;
        try {
            urlnet = new URL(url);
        } catch (MalformedURLException e) {
            logger.error("fail to build urlnet");
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) urlnet.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty("User-Agent", "cis455crawler");
        try {
            connection.setRequestMethod(Method);
        } catch (ProtocolException e) {
            logger.error("method invalid");
            e.printStackTrace();
        }
        return connection;


    }
    public  void shutdown() {
        run = false;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

}