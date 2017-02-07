/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import util.MongoUtil;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

public class QuadManager implements IQuadManager{

    /** */
    private MongoDatabase mongoDatabase;

    /** All URLs. */
    private MongoCollection URLs;

    /** All quads partitioned over the world. */
    private MongoCollection quads;
//    private Dataset<String> URLs;

    /** Mapped geohash to all quads ids. */
    // !???? must be not quads, but indexes to them!!!
    private MultivaluedHashMap<String, Integer> quadHashMap;

    private static final String DATABASE_NAME = "urlsdb";

    private static final String URL_COLLECTION = "output";

    public QuadManager(){
        MongoUtil.getOrCreateMongoClient();
        mongoDatabase = MongoUtil.getDatabase(DATABASE_NAME);
        URLs = mongoDatabase.getCollection(URL_COLLECTION);
    }

    public void partitionMapIntoQuads(){
        /** */
    }


    @Override
    public void partitionMapIntoQuads(Location topleft, Location bottomright, int S) {

    }

    public MultivaluedHashMap<String, Integer> createQuadHashMap(){
        /** Compute geohashes for quads and create hashmap. */

        return null;
    }
    public Quad selectQuadByUrlLocation(List<Quad> q, Location urllocation){
        /** Select which quad among given contains @param l */

        return null;
    }

    @Override
    public void partitionUrls() {

    }

    public MultivaluedHashMap<String, Integer> getQuadHashMap(){
        return quadHashMap;
    }

}
