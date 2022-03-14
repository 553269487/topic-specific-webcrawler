package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.Entity.DocObj;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.StorageDB;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.*;

public class CrawlerTest extends TestCase {
    private StorageDB db;
    private String directory;
    private Crawler crawler;
    private String rootUrl;
    @Before
    public void initial(){
        directory = "./database";
        rootUrl = "www.google.com";
        db = new StorageDB(directory);
        crawler = new Crawler(rootUrl,db,1024*1024*5, 1);
        crawler.start();
    }

    @Test
    public void test1(){
        boolean flag = crawler.isCrawlerDelay("www.google.com");
        assertFalse(flag);

    }

    @Test
    public void test2(){
        assertTrue(crawler.getBusyWorkers().get() > 0);
    }

}
