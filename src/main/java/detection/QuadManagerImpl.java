package detection;

import ch.hsr.geohash.GeoHash;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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

        /** начінаємо від того квада ходити в усі чотири сторони в рекурсивній функції */
        recursiveTraverseTopLevelQuads(firstQuad);


        //в цій верхній функції спробувати пройтися по всіх сусідах, і для кожного сусіда заходити в дітей.
        logger.info("partitionMapIntoQuads into quads finished.");
    }

    /**
     * @param pivot : pivot quad around which we'll traverse other quads up, right, down and left
     */
    //TODO: у фінальній версії це не статік і прайват
    public static void recursiveTraverseTopLevelQuads(TopLevelQuad pivot) {
        //STEP 1 - начінаємо від даного квада ходити в усі чотири сторони по часовій стрілці починаючи від правого
        Location nextQuadLocation = pivot.calcTopRight(); //go to the right quad from pivot
        if (isTheEndOfMap(nextQuadLocation))
            return; //exit from recursive function because the end of the map was reached
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
        if (isTheEndOfMap(nextQuadLocation))
            return;
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
        if (isTheEndOfMap(nextQuadLocation))
            return;
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
        if (isTheEndOfMap(nextQuadLocation))
            return;
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

    private static boolean isTheEndOfMap(Location location) {
        //65.953846, -25.095749 - iceland end
        //66.5 -24.57

        if (location.getLatitude() > 71 || location.getLatitude() < -35 || location.getLongitude() > 151 || location.getLongitude() < -25.56 )
            return true;
        else
            return false;
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
        if (parentQuadSide == Quad.QUAD_SIDE_MIN)
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
        MongoCollection<Document> col = mongoDatabase.getCollection("output");
//        try {
//            for (Object o : URLs.find()) { // .batchSize(128)
            try (MongoCursor<Document> cur = col.find().iterator()) {
                while (cur.hasNext()) {
                    Document d = cur.next();
                    logger.info(count);
//                Document d = (Document) o;
                    // check if there is at least one good urls,
                    // then try to put it into quads
                    //TODO: try to access saved locally urls, if no - then fetch
                    List<String> cleanedUrls = HtmlUtil.getCleanedUrlsByMimeType(
                            (String) d.get("urls"));
                    if (cleanedUrls.size() > 0) {
                        double lat = (double) d.get("lat");
                        double lon = (double) d.get("lon");
                        String urlHash = GeoHash
                                .geoHashStringWithCharacterPrecision(
                                        lat,
                                        lon,
                                        GeolocationUtil.GEOHASH_PRECISION);
                        //retrieve quads containing the same geohash
                        try {
                            Query<Quad> queryQuad = quadDataStore
                                    .createQuery(Quad.class)
                                    .filter("geoHash ==", urlHash);
                            List<Quad> quadList = queryQuad.asList();
                            nSameQuadGeohash += quadList.size();
                            if (quadList.size() > m) {
                                m = quadList.size();
                            }
//                    logger.info("Same geoHash retrieved:" + Integer.toString(quadList.size()));
                            Quad q = selectQuadByUrlLocation(quadList, new Location(lat, lon));
                            if (q != null) {
                                q.setUrls(cleanedUrls);
                                quadDataStore.save(q);
                            } else {
                                logger.info("No quads match geohash: " +
                                        urlHash + " " +
                                        Double.toString(lat) + " " +
                                        Double.toString(lon));
                                countUrlNotMatchingQuads++;
                            }
                        } catch (Exception e) {
                            logger.error("mongodb could not save "
                                    + e.getMessage());
                        }
                    }
                    count++;
                    if (count % 100 == 0) {
                        logger.info("Processed " + Integer.toString(count) + " urls." +
                                "Avg number of same quad geohash: " +
                                Integer.toString(nSameQuadGeohash / count));
                        logger.info("Total number of valid urls: " +
                                Integer.toString(HtmlUtil.getnValidUrls()));
                        if (count == 1000) {
                            break;
                        }
                    }
                }
                logger.info("Number of urls location without match quads: "
                        + Float.toString(countUrlNotMatchingQuads));

                logger.info("partitionUrls finished");
                logger.info(m);
            }
//        }
        catch (Exception e){
            logger.error(e.getMessage());
            logger.error("partitionUrls interrupted by mongodb error!");
        } finally {
                mongoClient.close();
        }
    }


    @Override
    //TODO: change signature of the function from hashtable to ...
    public Hashtable<Long, String> getTopics(Location topleft, double distanceToBottomRight, int S) {
        int qSide = GeolocationUtil.getQuadSideClosestToGivenStep(S);
        int level = GeolocationUtil.getLevel(qSide);
        if (qSide < Quad.QUAD_SIDE_MIN){
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
        while(level > 4) {          // 2^4 == 16 == QUAD.QUAD_SIDE_MIN - minimal available quad side
            topQuadId /= 10;
            level--;
        }
        queryQuad = quadDataStore
                .createQuery(Quad.class).filter("qId ==", topQuadId);
        Quad topQuad = queryQuad.asList().get(0);
//        System.out.println(topQuad);
        //how many quads on a diagonal
        int nDiagonal = (int)(distanceToBottomRight / Quad.QUAD_DIAGONAL);
        List<Quad> quadsInsideGivenArea = getQuadsInsideGivenArea(topQuad, nDiagonal);
//        logger.info(topQuad);
        for(Quad q: quadsInsideGivenArea) {
            calculateStatsForQuad(q, qSide);
            //TODO:on the current top level too many values - how to select only top-n for viewving?

        }
        return null;
    }


    private void calculateStatsForQuad(Quad quad, int qSide) {
        // if current quad side is the minimal one, so no quads inside this
        if (quad.getqSide() == Quad.QUAD_SIDE_MIN) {
            logger.info(quad);
            return;
        }
        //get quads inside current
        List<Quad> quadsInsideCurrent = getQuadsInsideQuad(quad.getId());
        for(Quad q: quadsInsideCurrent) {
            calculateStatsForQuad(q, qSide);
        }
        //calculate stats by smart aggregation of stats on previous levels
        Hashtable<String, Integer> table = new Hashtable<>();
        for(Quad q: quadsInsideCurrent) {
            if(q.getStats() != null) {
                q.getStats().forEach((k,v) -> table.merge(k, v, (v1,v2) -> v1 + v2));
            }
        }
        if( !table.isEmpty()) {
            quad.setStats(table);
            logger.info(quad);
            quadDataStore.save(quad);
        }
    }

    /**
     * Select from database only those quads, that is inside given but less
     * only on one zoom level, in other words their quad side length is less of given
     * by one magnitude of 2.
     * Example: suppose quad that has quadId has qSide equal 64. Then it will return
     * four quads inside this with qSide qual 32.
     * @param quadId
     * @return
     */
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
     * Collect quads of the same side size as the given quad, on the same
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
//                q.setStats(LDATopicDetector.getTopicStatsByUrls(q.getUrls(),
//                                            HtmlUtil.PAGE_TYPE.URL_LOCATION));
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
        if (qSide < Quad.QUAD_SIDE_MIN){
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
        while(level > 3) {          // 2^3 == 8 == QUAD.QUAD_SIDE_MIN
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
                                    HtmlUtil.PAGE_TYPE.URL_LOCATION).keySet());//TODO: add weights
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
        if (quad.getqSide() == Quad.QUAD_SIDE_MIN)
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
