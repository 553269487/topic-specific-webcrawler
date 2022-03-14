package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.Entity.DocObj;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.net.HttpURLConnection;

public class LookupHandler implements Route {
    private static Logger logger = LogManager.getLogger(LookupHandler.class);
    private StorageInterface db;
    public LookupHandler(StorageInterface db){
        this.db = db;
    }
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String url = request.queryParams("url");
        logger.info("request url:{}", url);
        if(url == null || url.isBlank() || url.isEmpty()){
            logger.info("null lookup");

            Spark.halt(HttpURLConnection.HTTP_BAD_REQUEST,"Bad url Bad Request <p> Go away!");
            response.status(400);
            return null;
        }
        logger.info("get document");
        URLInfo urlInfo = new URLInfo(url);
        String pathUrl = urlInfo.getPath();
        DocObj docObj= db.getDocument(pathUrl);
        if(docObj == null){
            logger.info("get document null");
            Spark.halt(404, "File not Found :)");
            response.status(404);
            response.body("File not Found but its body message");
            return null;
        }

        logger.info("get document {}",docObj.getType());
        response.status(200);
        response.type(docObj.getType());
        logger.info(docObj.getType());
        response.body(docObj.getDocContent());
        return response.body();

    }
}
