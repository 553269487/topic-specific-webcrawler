package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.Entity.DocObj;
import edu.upenn.cis.cis455.Entity.UrlObj;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.StorageDB;
import edu.upenn.cis.cis455.storage.StorageFactory;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.*;

public class DbTest extends TestCase {
    private StorageDB db;
    private String directory;
    @Before
    public void initial(){
        directory = "./database";
        db = (StorageDB) StorageFactory.getDatabaseInstance(directory);
    }

    @Test
    public void test1(){
        boolean flag = db.getCorpusSize() > 2;
        assertTrue(flag);

    }

    @Test
    public void test2(){
        String url = "https://crawltest.cis.upenn.edu/12345";
        URLInfo urlInfo = new URLInfo(url);
        String path = urlInfo.getPath();
        DocObj obj = db.getDocument(path);
        assertNull(obj);
    }

}
