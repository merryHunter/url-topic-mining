import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import detection.Location;
import detection.Quad;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.Hashtable;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;

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
            System.out.println("Mongo init.");
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
        Quad quad = new Quad(new Location(46.049945, 11.121257), 2);
        quad.setId(10L);
//        quad.addUrl(1L);
//        quad.addUrl(1L);
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

    @Test
    public void testSavingUrlReference(){
        morphia = new Morphia();
        morphia.map(Quad.class);
        Datastore datastore = morphia.createDatastore(mongoClient, "morphia_test");
        MongoDatabase db = mongoClient.getDatabase("morphia_test");
        MongoCollection urlCollection = db.getCollection("output");
        Document urlObject = (Document) urlCollection.find().first();   // retrieve url document
        System.out.println(urlObject.toJson());
//        String quadUrl = (String)urlObject.get("urls");
//        System.out.println(quadUrl);

        Query<Quad> queryQuad = datastore.createQuery(Quad.class);
        List<Quad> quadList = queryQuad.asList();

        Quad q = quadList.get(0);                                       // retrieve quad
        q.addUrl((ObjectId)urlObject.get("_id"));                       // add url document objectId
        System.out.println(q);
        datastore.save(q);                                              // save quad
        quadList = queryQuad.asList();
        q = quadList.get(0);                                            // retrieve quad
        List<ObjectId> url = q.getUrls();                               // get quad's url objectIds
        ObjectId u = url.get(0);

        //retrieve url with quad's url objectId
        urlObject = (Document) urlCollection.find(eq("_id", u) ).first();
        System.out.println(urlObject.toJson());
    }
    }
