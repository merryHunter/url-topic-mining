import ch.hsr.geohash.GeoHash;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import detection.Location;
import detection.Quad;
import detection.QuadManagerImpl;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import util.GeolocationUtil;

import java.util.Hashtable;
import java.util.LinkedList;
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
    public void initMongo() {
        try {
            mongoClient = new MongoClient("localhost", 27017);
            System.out.println("Mongo init.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testMorphiaOnQuadWrite() {
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
    public void testMorphiaOnQuadRead() {
        morphia = new Morphia();
        morphia.map(Quad.class);
        Datastore datastore = morphia.createDatastore(mongoClient, "morphia_test");
        Query<Quad> query = datastore.createQuery(Quad.class);
        List<Quad> quadList = query.asList();
        System.out.println(quadList.get(0).getStats().get("moose"));
        System.out.println(quadList.get(0).getGeoHash());
    }

    @Test
    public void testSavingUrlReference() {
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
//        q.addUrl((ObjectId) urlObject.get("_id"));                       // add url document objectId
        System.out.println(q);
        datastore.save(q);                                              // save quad
        quadList = queryQuad.asList();
        q = quadList.get(0);                                            // retrieve quad
//        List<ObjectId> url = q.getUrls();                               // get quad's url objectIds
//        ObjectId u = url.get(0);

        //retrieve url with quad's url objectId
//        urlObject = (Document) urlCollection.find(eq("_id", u)).first();
        System.out.println(urlObject.toJson());
    }

    @Test
    public void onTestReadUrlPerformance() {
        boolean objectid = true; // false - URL objectId, true - URL string;
        morphia = new Morphia();
        morphia.map(Quad.class);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("morphia_test");
        datastore = morphia.createDatastore(mongoClient, "morphia_test");
        MongoCollection URLs = mongoDatabase.getCollection("output");
        if (objectid) {//test string preformance
            int count = 0;
            System.out.println("Started...");
            long startTimeWriteQuads = System.nanoTime();
            for (Object o : URLs.find()) { // .batchSize(128)
                Document d = (Document) o;
                double lat = (double) d.get("lat");
                double lon = (double) d.get("lon");
                String urlHash = GeoHash.geoHashStringWithCharacterPrecision(lat,lon,GeolocationUtil.GEOHASH_PRECISION);
                Query<Quad> queryQuad = datastore.createQuery(Quad.class).filter("geoHash ==", urlHash);
                List<Quad> quadList = queryQuad.asList();
                Quad q = null;
                if (quadList.size() > 0)
                    q = quadList.get(0);
                if (q != null) {
                    q.addUrl((String) d.get("urls"));
                    datastore.save(q);
                } else {
                    System.out.println("No quads match geohash: " +
                            urlHash + " " +
                            Double.toString(lat) + " " +
                            Double.toString(lon));
                }
                count++;
                if (count == 1000) {
                    long stopTimeWriteQuads = System.nanoTime() - startTimeWriteQuads;
                    System.out.println("Time elapsed for writing quads with full URL String: " + Long.toString(stopTimeWriteQuads));
                    break;
                }
            }
            //collect quads to read urls
            long startTimeReadUrls = System.nanoTime();
            Query<Quad> queryQuad = datastore.createQuery(Quad.class).filter("geoHash exists", true);
            List<List<String>> s = new LinkedList<>();
            int urlCount = 0;
            for (Quad q: queryQuad.asList()) {
//                for (ObjectId uid:  q.getUrls()) {
//                    Document urlObject = (Document)URLs.find().filter(eq("_id", uid)).first();
                    s.add(q.getUrls());
//                }
                urlCount += q.getUrls().size();
            }
            long stopTimeReadUrls = System.nanoTime() - startTimeReadUrls;
            System.out.println("Time elapsed for querying quads and counting belonging URLS: " + Long.toString(stopTimeReadUrls));
            System.out.println(urlCount);
            System.out.println(s.size());
        }else{ //test objectif performance
            int count = 0;
            System.out.println("Started...");
            long startTimeWriteQuads = System.nanoTime();
            for (Object o : URLs.find()) { // .batchSize(128)
                Document d = (Document) o;
                double lat = (double) d.get("lat");
                double lon = (double) d.get("lon");
                String urlHash = GeoHash.geoHashStringWithCharacterPrecision(lat,lon,GeolocationUtil.GEOHASH_PRECISION);
                Query<Quad> queryQuad = datastore.createQuery(Quad.class).filter("geoHash ==", urlHash);
                List<Quad> quadList = queryQuad.asList();
                Quad q = null;
                if (quadList.size() > 0)
                    q = quadList.get(0);
                if (q != null) {
//                    q.addUrl((ObjectId) d.get("_id"));
                    datastore.save(q);
                } else {
                    System.out.println("No quads match geohash: " +
                            urlHash + " " +
                            Double.toString(lat) + " " +
                            Double.toString(lon));
                }
                count++;
                if (count == 1000) {
                    long stopTimeWriteQuads = System.nanoTime() - startTimeWriteQuads;
                    System.out.println("Time elapsed for writing quads with only URL's ObjectId: " + Long.toString(stopTimeWriteQuads));
                    break;
                }
            }
            //collect quads to read urls
            long startTimeReadUrls = System.nanoTime();
            Query<Quad> queryQuad = datastore.createQuery(Quad.class).filter("geoHash exists", true);
            List<String> s = new LinkedList<>();
            for (Quad q: queryQuad.asList()) {
//                for (ObjectId uid:  q.getUrls()) {
//                    Document urlObject = (Document)URLs.find().filter(eq("_id", uid)).first();
//                    s.add((String)urlObject.get("urls"));
//                }
            }
            long stopTimeReadUrls = System.nanoTime() - startTimeReadUrls;
            System.out.println("Time elapsed for querying quads and counting belonging URLS: " + Long.toString(stopTimeReadUrls));
            System.out.println(s.size());
        }

    }


}
