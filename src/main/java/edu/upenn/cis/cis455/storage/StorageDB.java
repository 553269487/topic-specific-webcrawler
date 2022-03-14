package edu.upenn.cis.cis455.storage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.*;
import edu.upenn.cis.cis455.Entity.DocObj;
import edu.upenn.cis.cis455.Entity.Entity;
import edu.upenn.cis.cis455.Entity.UrlObj;
import edu.upenn.cis.cis455.Entity.UserObj;
import edu.upenn.cis.cis455.enums.Dbname;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StorageDB implements StorageInterface{

    private static Logger logger = LogManager.getLogger(StorageInterface.class);

    private EnvironmentConfig environmentConfig;
    private Environment environment;
    private DatabaseConfig databaseConfig;
    private Database catalogDB;
    private StoredClassCatalog catalog;
    private Database userDB, docDB, urlDB;
    private StoredSortedMap<String, UserObj> userMap;
    private StoredSortedMap<String, DocObj>  docMap;
    private StoredSortedMap<String, UrlObj> urlMap;




    public StorageDB(String directory) {
        Config(directory);
    }

    public void Config(String directory){
        environmentConfig = new EnvironmentConfig();
        environmentConfig.setTransactional(true);
        environmentConfig.setAllowCreate(true);
        this.environment = new Environment(new File(directory), environmentConfig);
        databaseConfig = new DatabaseConfig();
        databaseConfig.setTransactional(true);
        databaseConfig.setAllowCreate(true);

        catalogDB = environment.openDatabase(null, Dbname.catalogName.value(), databaseConfig);
        catalog = new StoredClassCatalog(catalogDB);

        TupleBinding<String> key = TupleBinding.getPrimitiveBinding(String.class);
        EntryBinding<Entity> value = new SerialBinding<>(catalog, Entity.class);

        userDB = environment.openDatabase(null, Dbname.userdbName.value(), databaseConfig);
        docDB = environment.openDatabase(null, Dbname.docdbName.value(), databaseConfig);
        urlDB = environment.openDatabase(null, Dbname.urldbName.value(), databaseConfig);
        userMap = new StoredSortedMap(userDB, key, value, true);
        docMap = new StoredSortedMap(docDB, key, value, true);
        urlMap = new StoredSortedMap(urlDB, key, value, true);
    }



    @Override
    public int getCorpusSize() {
        synchronized (docDB){
            return docMap.size();
        }
    }

    public String md5Hash(String documentContents){
        MessageDigest messageDigest;
        String hashResult = "";
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            hashResult = new String(messageDigest.digest(documentContents.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            logger.error("md5 hash fail");
            e.printStackTrace();
        }
        return hashResult;

    }

    @Override
    public String addDocument(String url, String documentContents, String type) {
        synchronized (docDB){
            String hash = md5Hash(documentContents);
            docMap.putIfAbsent(hash, new DocObj(url, documentContents, type));
            return hash;
        }
    }

    @Override
    public DocObj getDocument(String url) {
        synchronized (urlDB){
            synchronized (docDB){
                UrlObj key1 = urlMap.get(url);
                if(key1 == null) return null;
                String key = key1.getHash();
                if(key == null) return null;
                return docMap.get(key);
            }
        }
    }

    public boolean containsDocument(String document){
        synchronized (docDB){
            return docMap.containsKey(md5Hash(document));
        }
    }

    @Override
    public int addUser(String username, String password) {
        synchronized (userDB){
            if(!userMap.containsKey(username)){
                logger.info("add user : " + username );
                userMap.put(username, new UserObj(username,password));
            }else{
                logger.warn("the user : {} already exsit", username);
                return -1;
            }
        }
        return 0;
    }

    public void addUrlObj(UrlObj urlObj){
        synchronized (urlDB){
            logger.info("add Url {}", urlObj.getUrl());
            urlMap.put(urlObj.getUrl(), urlObj);
        }
    }

    public UrlObj getUrlObj(String url){
        synchronized (urlDB){
            logger.info("get Url {}", url);
            return urlMap.get(url);
        }
    }

    public void removeUrlObj(String url){
        synchronized (urlDB){
            if(urlMap.containsKey(url)){
                logger.info("remove Url {}", url);
                urlMap.remove(url);
            }else {
                logger.info("no such url to remove");
            }
        }

    }

    @Override
    public boolean getSessionForUser(String username, String password) {
        synchronized (userDB){
            if(!userMap.containsKey(username)){
                logger.warn("NO SUCH USER");
                return false;
            }
            UserObj user = userMap.get(username);
            MessageDigest md = null;
            try {
                 md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if(md == null){
                logger.warn("no md");
                return false;
            }else{
                password = new String(md.digest(password.getBytes(StandardCharsets.UTF_8)));
            }

            return password.equals(user.getPassword());


        }
    }

    @Override
    public void close() throws DatabaseException {
        logger.info("start close DBs");
        catalogDB.close();
        logger.info("catLogdb close");
        userDB.close();
        logger.info("userdb close");
        docDB.close();
        logger.info("docdb close");
        urlDB.close();
        logger.info("urldb close");
    }
}
