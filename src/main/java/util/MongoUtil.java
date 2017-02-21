/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util;


import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;

public class MongoUtil {

    private static final Logger logger = Logger.getLogger(MongoUtil.class);

    private static final int PORT = 27017;

    public static final String HOST= "169.254.208.12";

    private static MongoClient mongoClient = null;

    public static MongoClient getOrCreateMongoClient(){
        if(mongoClient == null) {
            try {
                mongoClient = new MongoClient(HOST, PORT);
            } catch (Exception e){
                logger.error(e.getMessage());
            }
        }
        return mongoClient;
    }

    public static MongoDatabase getDatabase(String dbname) {
        return mongoClient.getDatabase(dbname);
    }
}
