package detection;

import ch.hsr.geohash.GeoHash;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import util.GeolocationUtil;
import util.HtmlUtil;
import util.MongoUtil;
import util.sequential.LDATopicDetector;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * Created by Dmytro on 06/02/2017.
 */
public class QuadManagerImpl implements IQuadManager{

    static final Logger logger = Logger.getLogger(QuadManagerImpl.class);

    protected static final String DATABASE_NAME = "morphia_test";

    protected static final String URL_COLLECTION = "output";

    protected static final String QUAD_COLLECTION = "quad";

    private static final int MINIMAL_SIDE = 16;

    public static volatile long topLevelQuadCount = 1L;

    //TODO: TEMP delete after debug
    public static List<Location> topLevelQuadUpperLeftCorners = new ArrayList<>();
    public static HashMap<String, TopLevelQuad> tempTopLevelQuadStorage = new HashMap<>();
    public static MultivaluedMap<String, TopLevelQuad> tempMultivaluedQuadStorage = new MultivaluedHashMap<>();
    //TODO:

    /** */
    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    /** All quads partitioned over the world. */
    private MongoCollection quads;

    /** All URLs. */
    private MongoCollection URLs;

    private Morphia morphia;

    private Datastore quadDataStore;

    private int countUrlNotMatchingQuads = 0;

    public QuadManagerImpl(){
        mongoClient = MongoUtil.getOrCreateMongoClient();
        mongoDatabase = MongoUtil.getDatabase(DATABASE_NAME);
        morphia = new Morphia();
        morphia.map(Quad.class);
        quadDataStore = morphia.createDatastore(mongoClient, DATABASE_NAME);
        logger.info("QuadManagerImpl initialized");
    }

    /**
     * Create map grid represented by <code>{@link Quad}</code>.
     * @param S: is the side length of the square
     */
    @Override
    public void partitionMapIntoQuads(Location topleft, Location bottomright, int S) {
        logger.info("partitionMapIntoQuads into quads started.");
        //while zoomLevel <= 11
        int quadSide = 2048; //початковий розмір квадратіка
        //поки не кінець світу
        //створили the Daddy
        TopLevelQuad firstQuad = new TopLevelQuad(topleft, quadSide);
        /**
         * using the static variable topLevelQuadCount below to keep track of how many quads have already been named
         */
        firstQuad.setId(QuadManagerImpl.topLevelQuadCount);
        quadDataStore.save(firstQuad);
        recursivePartitionQuadIntoChildren(topleft, quadSide, QuadManagerImpl.topLevelQuadCount); //рекурсивно обробляємо дітей
        /**
         * new quad was created along with all it's kids (parent ID which relies on this variable won't be used anymore)
         */
        QuadManagerImpl.topLevelQuadCount++;

        recursiveTraverseTopLevelQuads(firstQuad);
//        //начінаємо від того квада ходити в усі чотири сторони по часовій стрілці починаючи від правого
//        Location nextQuadLocation = firstQuad.calcTopRight(); //go to the right quad
//
//        //TODO: перевірка чи такий вже існує
//        //
//        TopLevelQuad topLevelQuad = new TopLevelQuad(nextQuadLocation, quadSide);
//        firstQuad.setId(QuadManagerImpl.topLevelQuadCount);
//        recursivePartitionQuadIntoChildren(nextQuadLocation, quadSide, QuadManagerImpl.topLevelQuadCount);
//        QuadManagerImpl.topLevelQuadCount++;
//        //TODO: save()
//        //

        //в цій верхній функції спробувати пройтися по всіх сусідах, і для кожного сусіда заходити в дітей.
        logger.info("partitionMapIntoQuads into quads finished.");
    }

    /**
     * @param pivot : pivot quad around which we'll traverse up, right, down and left
     */
    //TODO: у фінальній версії це не статік і прайват
    public static void recursiveTraverseTopLevelQuads(TopLevelQuad pivot) {
        //TODO: зробити нормальну перевірку на кінець світу
        if (pivot.getTopleft().getLatitude() > 50 || pivot.getTopleft().getLatitude() < -50 || pivot.getTopleft().getLongitude() > 140 || pivot.getTopleft().getLongitude() < -140 )
            return;

        //STEP 1 - начінаємо від даного квада ходити в усі чотири сторони по часовій стрілці починаючи від правого
        Location nextQuadLocation = pivot.calcTopRight(); //go to the right quad from pivot
        TopLevelQuad newQuad;


        //TODO: перевірка чи такий вже існує
        newQuad = new TopLevelQuad(nextQuadLocation, pivot.getqSide());
        if (!topLevelQuadExists(newQuad)) {
            newQuad.setId(QuadManagerImpl.topLevelQuadCount);
            QuadManagerImpl.topLevelQuadCount++;
            tempMultivaluedQuadStorage.add(newQuad.getGeoHash(), newQuad);
            recursiveTraverseTopLevelQuads(newQuad);
        } else {
            TopLevelQuad existing = getExistingTopLevelQuad(newQuad);
            existing.leftNeighbor = pivot; //we came from the left side
        }

        //STEP 2 - йдемо ВНИЗ
        nextQuadLocation = pivot.calcBottomLeft();
        newQuad = new TopLevelQuad(nextQuadLocation, pivot.getqSide());
        if (!topLevelQuadExists(newQuad)) {
            newQuad.setId(QuadManagerImpl.topLevelQuadCount);
            QuadManagerImpl.topLevelQuadCount++;
            tempMultivaluedQuadStorage.add(newQuad.getGeoHash(), newQuad);
            recursiveTraverseTopLevelQuads(newQuad);
        } else {
            TopLevelQuad existing = getExistingTopLevelQuad(newQuad);
            existing.upperNeighbor = pivot; //we came from the upper side
        }

        //STEP 3 - йдемо ВЛІВО
        nextQuadLocation = pivot.calcLeftNeighborStartingCoord();
        newQuad = new TopLevelQuad(nextQuadLocation, pivot.getqSide());
        if (!topLevelQuadExists(newQuad)) {
            newQuad.setId(QuadManagerImpl.topLevelQuadCount);
            QuadManagerImpl.topLevelQuadCount++;
            tempMultivaluedQuadStorage.add(newQuad.getGeoHash(), newQuad);
            recursiveTraverseTopLevelQuads(newQuad);
        } else {
            TopLevelQuad existing = getExistingTopLevelQuad(newQuad);
            existing.rightNeighbor = pivot; //we came from the right side
        }

        //STEP 4 - йдемо ВГОРУ
        nextQuadLocation = pivot.calcUpperNeighborStartingCoord();
        newQuad = new TopLevelQuad(nextQuadLocation, pivot.getqSide());
        if (!topLevelQuadExists(newQuad)) {
            newQuad.setId(QuadManagerImpl.topLevelQuadCount);
            QuadManagerImpl.topLevelQuadCount++;
            tempMultivaluedQuadStorage.add(newQuad.getGeoHash(), newQuad);
            recursiveTraverseTopLevelQuads(newQuad);
        } else {
            TopLevelQuad existing = getExistingTopLevelQuad(newQuad);
            existing.bottomNeighbor = pivot; //we came from the bottom side
        }

    }

    private static TopLevelQuad getExistingTopLevelQuad(TopLevelQuad newQuad) {
        List<TopLevelQuad> quadsPerKey = tempMultivaluedQuadStorage.get(newQuad.getGeoHash());

        for (TopLevelQuad quad : quadsPerKey) {
            Location topleft = quad.getTopleft();
            Location bottomright = quad.getBottomright();
            // longitude - x
            // latitude - y
            if (newQuad.getCenter().getLatitude() <= topleft.getLatitude() &&
                    newQuad.getCenter().getLatitude() >= bottomright.getLatitude() &&
                    newQuad.getCenter().getLongitude() >= topleft.getLongitude() &&
                    newQuad.getCenter().getLongitude() <= bottomright.getLongitude()) {
                return quad;
            }
        }
        return null;
    }

    private static boolean topLevelQuadExists(TopLevelQuad newQuad) {
        List<TopLevelQuad> quadsPerKey = tempMultivaluedQuadStorage.get(newQuad.getGeoHash());

        if (quadsPerKey == null) //it's null, not empty when it wasn't put in the map yet
            return false;
        else {
            for(TopLevelQuad quad : quadsPerKey) {
                Location topleft = quad.getTopleft();
                Location bottomright = quad.getBottomright();
                // longitude - x
                // latitude - y
                if (newQuad.getCenter().getLatitude() <= topleft.getLatitude() &&
                        newQuad.getCenter().getLatitude() >= bottomright.getLatitude() &&
                        newQuad.getCenter().getLongitude() >= topleft.getLongitude() &&
                        newQuad.getCenter().getLongitude() <= bottomright.getLongitude()){
                    return true;
                }
            }
        }
        return false;
    }

    //TODO: private
    private void recursivePartitionQuadIntoChildren(Location topleft,
                                                    int parentQuadSide,
                                                    long parentQuadId) {
        if (parentQuadSide == Quad.QUAD_SIDE)
            return;

        //creating subquad 0
        Quad newQuad0 = new Quad(topleft, parentQuadSide/2);
        newQuad0.setId(parentQuadId*10L+0); //shift father ID by 1 digit (розряд)
        quadDataStore.save(newQuad0);
        recursivePartitionQuadIntoChildren(newQuad0.getTopleft(), newQuad0.getqSide(), newQuad0.getId());

        //creating subquad 1
        Quad newQuad1 = new Quad(newQuad0.calcTopRight(), parentQuadSide/2);
        newQuad1.setId(parentQuadId*10L+1);
        quadDataStore.save(newQuad1);
        recursivePartitionQuadIntoChildren(newQuad1.getTopleft(), newQuad1.getqSide(), newQuad1.getId());

        //creating subquad 2
        Quad newQuad2 = new Quad(newQuad0.calcBottomLeft(), parentQuadSide/2);
        newQuad2.setId(parentQuadId*10L+2);
        quadDataStore.save(newQuad2);
        recursivePartitionQuadIntoChildren(newQuad2.getTopleft(), newQuad2.getqSide(), newQuad2.getId());

        //creating subquad 3
        Quad newQuad3 = new Quad(newQuad0.getBottomright(), parentQuadSide/2);
        newQuad3.setId(parentQuadId*10L+3);
        quadDataStore.save(newQuad3);
        recursivePartitionQuadIntoChildren(newQuad3.getTopleft(), newQuad3.getqSide(), newQuad3.getId());
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
     * Distribute URLs over created map grid
     * (see <code>partitionMapIntoQuads</code>).
     */
    @Override
    public void partitionUrls(){
        logger.info("partitionUrls started");
        int count = 0;                  // count processed urls
        URLs = mongoDatabase.getCollection(URL_COLLECTION);
        int nSameQuadGeohash = 0;
        int m = 0;
        try {
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
                Query<Quad> queryQuad = quadDataStore
                        .createQuery(Quad.class)
                        .filter("geoHash ==", urlHash);
                List<Quad> quadList = queryQuad.asList();
                nSameQuadGeohash += quadList.size();
                if(quadList.size() > m){
                    m = quadList.size();
                }
                logger.info("Same geoHash retrieved:" + Integer.toString(quadList.size()));
                Quad q = selectQuadByUrlLocation(quadList, new Location(lat, lon));
                if (q != null) {
                    String s = (String) d.get("urls");
                    q.addUrlsAll(s.split("\\|"));
                    quadDataStore.save(q);
                } else {
                    logger.info("No quads match geohash: " +
                            urlHash + " " +
                            Double.toString(lat) + " " +
                            Double.toString(lon));
                    countUrlNotMatchingQuads++;
                }
                count++;
                if (count % 1000 == 0) {
                    logger.info("Processed " + Integer.toString(count) + " urls." +
                            "Avg number of same quad geohash: " + Integer.toString(nSameQuadGeohash / count));
                }
            }
            logger.info("Number of urls location without match quads: "
                    + Float.toString(countUrlNotMatchingQuads));
            logger.info("partitionUrls finished");
            logger.info(m);
        }catch (Exception e){
            logger.error(e.getMessage());
            logger.error("partitionUrls interrupted by mongodb error!");
        }
    }


    @Override
    //TODO: change signature of the function from hashtable to ...
    public Hashtable<Long, String> getTopics(Location topleft, double distanceToBottomRight, int S) {
        int qSide = GeolocationUtil.getQuadSideClosestToGivenStep(S);
        int level = GeolocationUtil.getLevel(qSide);
        if (qSide < Quad.QUAD_SIDE){
            //TODO:
            return null;
        }
        String geoHashTopLeft = GeoHash.geoHashStringWithCharacterPrecision(
                                    topleft.getLatitude(),
                                    topleft.getLongitude(),
                                    GeolocationUtil.GEOHASH_PRECISION);
        Query<Quad> queryQuad = quadDataStore
                .createQuery(Quad.class).filter("geoHash ==", geoHashTopLeft);
        //TODO: too many quads for a geoHash (~20) !! What should we do?
        Quad quadTopLeft = queryQuad.asList().get(0);
        long topQuadId = quadTopLeft.getId();
        while(level > 3) {          // 2^3 == 8 == QUAD.QUAD_SIDE
            topQuadId /= 10;
            level--;
        }
        queryQuad = quadDataStore
                .createQuery(Quad.class).filter("qId ==", topQuadId);
        Quad topQuad = queryQuad.asList().get(0);
        System.out.println(topQuad);
        //how many quads on a diagonal
        int nDiagonal = (int)(distanceToBottomRight / Quad.QUAD_DIAGONAL);
        List<Quad> quadsInsideGivenArea = getQuadsInsideGivenArea(topQuad, nDiagonal);
        for(Quad q: quadsInsideGivenArea) {
            calculateStatsForQuad(q, qSide);
            //TODO:on the current top level too many values - how to select only top-n for viewving?

        }
        return null;
    }


    //TODO: check if quad already contains computed stats before computing!!!
    private void calculateStatsForQuad(Quad quad, int qSide) {
        // if current quad side is the minimal one, so no quads inside this
        if (quad.getqSide() == Quad.QUAD_SIDE)
            return;
        //get quads inside current
        List<Quad> quadsInsideCurrent = getQuadsInsideQuad(quad.getId());
        for(Quad q: quadsInsideCurrent) {
            calculateStatsForQuad(q, qSide);
        }
        //calculate stats
        Hashtable<String, Integer> table = new Hashtable<>();
        for(Quad q: quadsInsideCurrent) {
            if(q.getStats() != null)
                table.putAll(q.getStats());
        }
        if( !table.isEmpty()) {
            quad.setStats(table);
            quadDataStore.save(quad);
        }
    }


    private List<Quad> getQuadsInsideQuad(long quadId){
        Query<Quad> q = quadDataStore.createQuery(Quad.class);
        q.or(
                q.criteria("qId").equal(quadId*10 + 0),
                q.criteria("qId").equal(quadId*10 + 1),
                q.criteria("qId").equal(quadId*10 + 2),
                q.criteria("qId").equal(quadId*10 + 3)
        );
        return q.asList();
    }

    /**
     * Collect quads the same side side as the given quad, on the same
     * zoom level.
     * @param topLeftQuad
     * @param nDiagonal : how many quads on the diagonal
     * @return
     */
    private List<Quad> getQuadsInsideGivenArea(Quad topLeftQuad, int nDiagonal){
        List<Quad> quads = new LinkedList<>();
        quads.add(topLeftQuad);
        return quads;
    }


    @Override
    public void computeTopicStatsSmallestQuads(){
        Query<Quad> queryQuad = quadDataStore
                .createQuery(Quad.class)
                .filter("urls exists", true);
        List<Quad> quadList = queryQuad.asList();
        int size = quadList.size();
        int nUrls = 0;
        for (int i = 0; i < size; i++){
            Quad q = quadList.get(i);
            try {
                int urlSize = q.getUrls().size();
                nUrls += urlSize;
//                logger.info("Number of urls:" + Integer.toString(urlSize) + " quad id: " + q.getId());
                q.setStats(LDATopicDetector.getTopicStatsByUrls(q.getUrls(),
                                            HtmlUtil.PAGE_TYPE.URL_LOCATION));
                quadDataStore.save(q);
            }catch (Exception e){
                logger.error("Unable to detect topics!");
                logger.error(e.getMessage());
            }
            if (i % 100 == 0){
                logger.info("Processed quads urls: " + Integer.toString(i));
            }
        }
        logger.info("Average number of urls in quad: " +
                Integer.toString(nUrls / quadList.size()));
    }


    /**
     * Following methods refer to the baseline algorithm,
     * which detects topics be RERUNNING in each quad. So instead of
     * computing topics stats for smallest quads, directly compute stats for
     * urls inside quads.
     */

    public Hashtable<Quad, Set<String>> getTopicsByRerun(
            Location topleft, double distanceToBottomRight, int S){
        int qSide = GeolocationUtil.getQuadSideClosestToGivenStep(S);
        int level = GeolocationUtil.getLevel(qSide);
        if (qSide < Quad.QUAD_SIDE){
            //TODO:
            return null;
        }
        String geoHashTopLeft = GeoHash.geoHashStringWithCharacterPrecision(
                topleft.getLatitude(),
                topleft.getLongitude(),
                GeolocationUtil.GEOHASH_PRECISION);
        Query<Quad> queryQuad = quadDataStore
                .createQuery(Quad.class).filter("geoHash ==", geoHashTopLeft);
        Quad quadTopLeft = queryQuad.asList().get(0);
        long topQuadId = quadTopLeft.getId();
        while(level > 3) {          // 2^3 == 8 == QUAD.QUAD_SIDE
            topQuadId /= 10;
            level--;
        }
        queryQuad = quadDataStore
                .createQuery(Quad.class).filter("qId ==", topQuadId);
        Quad topQuad = queryQuad.asList().get(0);
        System.out.println(topQuad);
        //how many quads on a diagonal
        int nDiagonal = (int)(distanceToBottomRight / Quad.QUAD_DIAGONAL);
        List<Quad> quadsInsideGivenArea = getQuadsInsideGivenArea(topQuad, nDiagonal);
        Hashtable<Quad, Set<String>> result = new Hashtable<>();
        int nUrls = 0;
        for(Quad q: quadsInsideGivenArea) {
            List<String> urlsInsideQuad = getAllUrlsInsideQuad(q);
            int size = urlsInsideQuad.size();
            logger.info("Number of urlsInsideQuad: " +
                    Integer.toString(size ));
            nUrls += size;
            for (int i = 0; i < size; i++){
                try {
                    result.put(q, LDATopicDetector
                            .getTopicStatsByUrls(urlsInsideQuad,
                                    HtmlUtil.PAGE_TYPE.URL_LOCATION).keySet());
                }catch (Exception e){
                    logger.error("Unable to detect topics!");
                    logger.error(e.getMessage());
                }
            }
        }
        logger.info("Average number of urls in quad: " +
                Integer.toString(nUrls / quadsInsideGivenArea.size()));
        return result;
    }


    private List<String> getAllUrlsInsideQuad(Quad quad) {
        if (quad.getqSide() == Quad.QUAD_SIDE)
            return null;
        //get quads inside current
        List<Quad> quadsInsideCurrent = getQuadsInsideQuad(quad.getId());
        //recursively get quads urls
        List<String> result = new LinkedList<>();
        result.addAll(quad.getUrls());
        for (Quad q : quadsInsideCurrent){
            List<String> urls = getAllUrlsInsideQuad(q);
            if (urls != null) {
                result.addAll(urls);
            }
        }
        return result;
    }


}
