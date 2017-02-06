import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.Test;

/**
 * @author Ivan Chernukha on 06.02.17.
 */

public class TestMongoDB {

    @Test
    public void testMongoDBConnection(){
        try{
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase db = mongoClient.getDatabase("urlsdb");
            MongoCollection urls = db.getCollection("outputone");
            System.out.println(urls.count());

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
