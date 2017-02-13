package detection;

import ch.hsr.geohash.GeoHash;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import util.GeolocationUtil;
import util.HtmlUtil;
import util.MongoUtil;
import util.sequential.LDATopicDetector;

import java.util.List;

/**
 * Created by Dmytro on 06/02/2017.
 */
public class QuadManagerImpl implements IQuadManager{

    static final Logger logger = Logger.getLogger(QuadManagerImpl.class);

    private static final String DATABASE_NAME = "morphia_test";

    private static final String URL_COLLECTION = "output";

    private static final String QUAD_COLLECTION = "quad";

    private static final int MINIMAL_SIDE = 16;

    /** */
    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    /** All quads partitioned over the world. */
    private MongoCollection quads;

    /** All URLs. */
    private MongoCollection URLs;

    private Morphia morphia;

    private Datastore datastore;

    private int countUrlNotMatchingQuads = 0;

    public QuadManagerImpl(){
        mongoClient = MongoUtil.getOrCreateMongoClient();
        mongoDatabase = MongoUtil.getDatabase(DATABASE_NAME);
        morphia = new Morphia();
        morphia.map(Quad.class);
        datastore = morphia.createDatastore(mongoClient, DATABASE_NAME);
        logger.info("QuadManagerImpl initialized");
    }

    /**
     * Create map grid represented by <code>{@link Quad}</code>.
     * @param S: is the side length of the square
     */
    @Override
    public void partitionMapIntoQuads(Location topleft, Location bottomright, int S) {
        //while zoomLevel <= 11
        int quadSide = 2048; //початковий розмір квадратіка
        //поки не кінець світу
        //створили the Daddy
        Quad newQuad = new Quad(topleft, quadSide);
        newQuad.setId(1L);
        datastore.save(newQuad);
        //4 рази рекурсивно зайшли в дітей
        recursivePartitionMapIntoQuads(topleft, quadSide, 1); //вперше заходимо в дітей

        //в цій верхній функції спробувати пройтися по всіх сусідах, і для кожного сусіда заходити в дітей.
        logger.info("Patitioning map into quads finished.");
    }

    private void recursivePartitionMapIntoQuads(Location topleft,
                                                int fatherQuadSide,
                                                long fatherQuadId) {
        if (fatherQuadSide == MINIMAL_SIDE)
            return;

        //creating subquad 0
        Quad newQuad0 = new Quad(topleft, fatherQuadSide/2);
        newQuad0.setId(fatherQuadId*10L+0); //shift father ID by 1 digit (розряд)
        datastore.save(newQuad0);
        recursivePartitionMapIntoQuads(newQuad0.getTopleft(), newQuad0.getqSide(), newQuad0.getId());

        //creating subquad 1
        Quad newQuad1 = new Quad(newQuad0.calcTopRight(), fatherQuadSide/2);
        newQuad1.setId(fatherQuadId*10L+1);
        datastore.save(newQuad1);
        recursivePartitionMapIntoQuads(newQuad1.getTopleft(), newQuad1.getqSide(), newQuad1.getId());

        //creating subquad 2
        Quad newQuad2 = new Quad(newQuad0.calcBottomLeft(), fatherQuadSide/2);
        newQuad2.setId(fatherQuadId*10L+2);
        datastore.save(newQuad2);
        recursivePartitionMapIntoQuads(newQuad2.getTopleft(), newQuad2.getqSide(), newQuad2.getId());

        //creating subquad 3
        Quad newQuad3 = new Quad(newQuad0.getBottomright(), fatherQuadSide/2);
        newQuad3.setId(fatherQuadId*10L+3);
        datastore.save(newQuad3);
        recursivePartitionMapIntoQuads(newQuad3.getTopleft(), newQuad3.getqSide(), newQuad3.getId());
    }


    /**
     * A url location can refer to multiple quads with the same geohash.
     * Determine the quad containing the given url location by
     * rectangle vertices comparison.
     * @param quadList : List of quads with the same geohash.
     * @param urllocation : Location of a url not placed yet in any quad.
     * @return Quad containing given url.
     */
    @Override
    public Quad selectQuadByUrlLocation(List<Quad> quadList, Location urllocation) {
        if (quadList.size() == 1)
            return quadList.get(0);
         for(Quad q : quadList){
             Location topleft = q.getTopleft();
             Location bottomright = q.getBottomright();
             // longitude - x
             // latitude - y
             if (urllocation.getLatitude() <= topleft.getLatitude() &&
                     urllocation.getLatitude() >= bottomright.getLatitude() &&
                     urllocation.getLongitude() >= topleft.getLongitude() &&
                     urllocation.getLongitude() <= bottomright.getLongitude()){
                 return q;
             }
         }
         return null;
    }

    /**
     * Distributed URLs over created map grid
     * (see <code>partitionMapIntoQuads</code>).
     */
    @Override
    public void partitionUrls(){
        logger.info("partitionUrls started");
        int count = 0;                  // count processed urls
        URLs = mongoDatabase.getCollection(URL_COLLECTION);
        for (Object o : URLs.find()) { // .batchSize(128)
            Document d = (Document) o;
            double lat = (double) d.get("lat");
            double lon = (double) d.get("lon");
            String urlHash = GeoHash
                    .geoHashStringWithCharacterPrecision(
                            lat,
                            lon,
                            GeolocationUtil.GEOHASH_PRECISION);
            //retrieve quads containing the same geohash
            Query<Quad> queryQuad = datastore
                    .createQuery(Quad.class)
                    .filter("geoHash ==", urlHash);
            List<Quad> quadList = queryQuad.asList();
            Quad q = selectQuadByUrlLocation(quadList, new Location(lat, lon));
            if(q != null) {
                String s = (String) d.get("urls");
                q.addUrlsAll(s.split("\\|"));
                datastore.save(q);
            } else {
                logger.info("No quads match geohash: " +
                        urlHash + " " +
                        Double.toString(lat) + " " +
                        Double.toString(lon));
                countUrlNotMatchingQuads++;
            }
            count++;
            if (count % 1000 == 0){
                logger.info("Processed " + Integer.toString(count) + " urls.");
            }
        }
        logger.info("Number of urls location without match geohash: "
                + Float.toString(countUrlNotMatchingQuads));
        logger.info("partitionUrls finished");
    }


//    @Override
    public List<String> getTopics(Location topleft, Location bottomright, int S) {
        return null;
    }

//    @Override
    public void computeTopicStatsSmallestQuads(){
        Query<Quad> queryQuad = datastore
                .createQuery(Quad.class)
                .filter("urls exists", true);
        List<Quad> quadList = queryQuad.asList();
        int size = quadList.size();
        for (int i = 0; i < size; i++){
            Quad q = quadList.get(i);
            try {
                q.setStats(LDATopicDetector.getTopicStatsByUrls(q.getUrls(), HtmlUtil.PAGE_TYPE.URL_LOCATION));
                datastore.save(q);
            }catch (Exception e){
                logger.error("Unable to detect topics!");
                logger.error(e.getMessage());
            }
            if (i % 10 == 0){
                logger.info("Processed quads urls: " + Integer.toString(i));
            }
        }

    }
}
