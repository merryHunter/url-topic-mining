import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import detection.Location;
import detection.Quad;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.Hashtable;
import java.util.List;

/**
 * @author Ivan Chernukha on 06.02.17.
 */

public class MorphiaTest {
    MongoClient mongoClient = null;
    Morphia morphia = null;
    Datastore datastore = null;

    @Before
    public void initMongo(){
        try{
            mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase db = mongoClient.getDatabase("urlsdb");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testMorphiaOnQuadWrite(){
        morphia = new Morphia();
        morphia.map(Quad.class);
        Datastore datastore = morphia.createDatastore(mongoClient, "morphia_test");
        Hashtable<String, Integer> stats = new Hashtable<>();
        stats.put("moose", 3);
        stats.put("Obama", 1);
        stats.put("Trento", 10);
        Quad quad = new Quad(new Location(45.111, 50.333), new Location(66.234,34.234));
        quad.set_id(9);
        quad.addUrl(1L);
        quad.addUrl(1L);
        quad.setStats(stats);
        datastore.save(quad);
    }

    @Test
    public void testMorphiaOnQuadRead(){
        morphia = new Morphia();
        morphia.map(Quad.class);
        Datastore datastore = morphia.createDatastore(mongoClient, "morphia_test");
        Query<Quad> query = datastore.createQuery(Quad.class);
        List<Quad> quadList = query.asList();
        System.out.println(quadList.get(0).getStats().get("moose"));
        System.out.println(quadList.get(0).getGeoHash());
    }
}
