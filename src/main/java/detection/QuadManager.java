/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import org.apache.spark.sql.Dataset;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

public class QuadManager implements IQuadManager{

    /** All URLs. */
    private Dataset<String> URLs;

    /** Mapped geohash to all quads ids. (for the world?)*/
    // !???? must be not quads, but indexes to them!!!
    protected MultivaluedHashMap<String, Integer> quadHashMap;

    /** All quads partitioned over the ...???. */
    // TODO: transform to another data structure? (db?)
    protected Quad[] quads;

    public QuadManager(){
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
