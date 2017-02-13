/**
 * @author Ivan Chernukha on 06.02.17.
 */
package detection;

import org.bson.types.ObjectId;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Hashtable;
import java.util.List;

public interface IQuadManager {

    /** At the first query to the system, the method assigns an id
     * to each quad and set its location. */
    void partitionMapIntoQuads(Location topleft, Location bottomright, int S);

    /** Resulting quad is the quad where queried URL belongs to.*/
    Quad selectQuadByUrlLocation(List<Quad> q, Location urllocation);

    /** Partition URLs over all quads. */
    void partitionUrls();

//    List<String> getTopics(Location topleft, Location bottomright, int S);

//    void computeTopicStatsByQuad(Quad q);
}
